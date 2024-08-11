/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.as.oss.fl.localcompute.impl;

import android.content.Context;
import android.net.Uri;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.fl.localcompute.LocalComputeResourceManager;
import com.google.android.as.oss.fl.localcompute.PathConversionUtils;
import com.google.android.as.oss.fl.localcompute.client.FileCopyGrpcClient;
import com.google.common.collect.ImmutableList;
import com.google.common.flogger.GoogleLogger;
import com.google.common.time.TimeSource;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dagger.hilt.android.qualifiers.ApplicationContext;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.Executor;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.io.FileUtils;

/**
 * A local compute task resource manager that provides APIs for handling the plan|input|output files
 * at different time points.
 */
@Singleton
class LocalComputeResourceManagerImpl implements LocalComputeResourceManager {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  /**
   * The marker file is located in each session's resource root folder, the same layer as
   * inputs|plans|outputs. The marker file will track the start time of the most recent training and
   * relative path of input directory.
   */
  private static final String MARKER_FILENAME = "__local_compute_marker__";

  /**
   * The freeze window used to avoid accidental resource clean-up while the particular local compute
   * task is in training.
   */
  private static final long FREEZE_WINDOW_MILLIS = Duration.ofMinutes(15).toMillis();

  /** This TTL is for the entire resource folder for a particular session. */
  private static final long RESOURCE_TTL_MILLIS = Duration.ofDays(7).toMillis();

  private final Context context;
  private final Executor executor;
  private final TimeSource timeSource;
  private final FileCopyGrpcClient fileCopyGrpcClient;

  @Inject
  LocalComputeResourceManagerImpl(
      @ApplicationContext Context context,
      @IoExecutorQualifier Executor executor,
      FileCopyGrpcClient fileCopyGrpcClient) {
    this.context = context;
    this.executor = executor;
    this.timeSource = TimeSource.system();
    this.fileCopyGrpcClient = fileCopyGrpcClient;
  }

  @VisibleForTesting
  LocalComputeResourceManagerImpl(
      Context context,
      Executor executor,
      TimeSource timeSource,
      FileCopyGrpcClient fileCopyGrpcClient) {
    this.context = context;
    this.executor = executor;
    this.timeSource = timeSource;
    this.fileCopyGrpcClient = fileCopyGrpcClient;
  }

  /**
   * At training job's scheduling, we firstly check the marker file. If it's within the freeze
   * window, we do nothing. If not, we delete the entire resource folder for this particular
   * session. Then we copy the plan file from PCC and create an empty input directory. A marker file
   * will be created and the input directory relative path will be tracked in marker file.
   *
   * @return The future is true if computation plan is copied, a marker file is created and an empty
   *     input directory is created successfully. It is false if the associated session is in
   *     training or any file operation fails.
   */
  @Override
  public ListenableFuture<Boolean> prepareResourceAtScheduling(
      String sessionName, Uri originalPlanUri, Uri originalInputDirUri) {
    String resourceRootRelativePath =
        PathConversionUtils.getResourceRootDirRelativePathForSession(sessionName);
    File resourceRoot = new File(context.getFilesDir(), resourceRootRelativePath);
    File marker = new File(resourceRoot, MARKER_FILENAME);
    return Futures.submitAsync(
        () -> {
          if (marker.exists() && isPossiblyInTraining(marker)) {
            logger.atWarning().log("A training session is possibly in progress!");
            return Futures.immediateFuture(false);
          }

          if (resourceRoot.exists()) {
            try {
              FileUtils.forceDelete(resourceRoot);
            } catch (IOException e) {
              logger.atSevere().withCause(e).log("Failed to delete the leftover resource files.");
              return Futures.immediateFuture(false);
            }
          }

          resourceRoot.mkdirs();
          Uri convertedPlanUri =
              PathConversionUtils.addPlanPathPrefix(originalPlanUri, sessionName);
          Uri convertedInputDirUri =
              PathConversionUtils.addInputPathPrefix(originalInputDirUri, sessionName);
          try {
            marker.createNewFile();
            FileUtils.writeLines(marker, ImmutableList.of(convertedInputDirUri.toString()));
          } catch (IOException e) {
            logger.atSevere().withCause(e).log("Failed to create the marker file.");
            return Futures.immediateFuture(false);
          }
          File inputDir = PathConversionUtils.convertUriToFile(context, convertedInputDirUri);
          if (!inputDir.mkdirs()) {
            logger.atSevere().log("Failed to create the input directory.");
            return Futures.immediateFuture(false);
          }
          return fileCopyGrpcClient.copyFileFromServer(originalPlanUri, convertedPlanUri);
        },
        executor);
  }

  /**
   * At training job's cancellation, we forcefully delete the entire resource folder for this
   * particular session.
   *
   * @return The future is true if the resource is cleaned successfully. It is false if any file
   *     operation fails.
   */
  @Override
  public ListenableFuture<Boolean> cleanResourceAtCancellation(String sessionName) {
    String resourceRootRelativePath =
        PathConversionUtils.getResourceRootDirRelativePathForSession(sessionName);
    return Futures.submit(
        () -> {
          try {
            FileUtils.forceDelete(new File(context.getFilesDir(), resourceRootRelativePath));
          } catch (IOException e) {
            logger.atSevere().withCause(e).log(
                "Failed to delete the resource files at cancellation.");
            return false;
          }
          return true;
        },
        executor);
  }

  /**
   * At the beginning of training, we copy the necessary input resources from ASI based on the given
   * file string key. We also refresh the start time of most recent training in marker file.
   *
   * @return The future of the absolute path of the copied file/directory if the input resource
   *     contents are copied from remote app and marker file is refreshed successfully. Otherwise,
   *     an {@link ImmediateFailedFuture} will be returned.
   */
  @Override
  public ListenableFuture<String> copyResourceAtTraining(String sessionName, String fileStringKey) {
    String resourceRootRelativePath =
        PathConversionUtils.getResourceRootDirRelativePathForSession(sessionName);
    File resourceRoot = new File(context.getFilesDir(), resourceRootRelativePath);
    File marker = new File(resourceRoot, MARKER_FILENAME);
    return Futures.submitAsync(
        () -> {
          try {
            String inputDirUriStr = FileUtils.readLines(marker, Charset.defaultCharset()).get(0);
            String currentTimeMillis = String.valueOf(timeSource.instant().toEpochMilli());
            FileUtils.writeLines(marker, ImmutableList.of(inputDirUriStr, currentTimeMillis));
          } catch (IOException e) {
            logger.atSevere().withCause(e).log("Failed to create the marker file.");
            return Futures.immediateFailedFuture(e);
          }

          return Futures.transformAsync(
              fileCopyGrpcClient.queryFileUriByStringKey(sessionName, fileStringKey),
              response -> {
                Uri srcUri = Uri.parse(response.getResultFileUri());
                Uri destUri = PathConversionUtils.addInputPathPrefix(srcUri, sessionName);
                final String destAbsolutePath =
                    PathConversionUtils.convertUriToFile(context, destUri).getAbsolutePath();
                ListenableFuture<Boolean> copyFuture;
                if (response.getIsDirectory()) {
                  copyFuture = fileCopyGrpcClient.copyDirFromServer(srcUri, destUri);
                } else {
                  copyFuture = fileCopyGrpcClient.copyFileFromServer(srcUri, destUri);
                }
                return Futures.transformAsync(
                    copyFuture,
                    result -> {
                      if (result) {
                        return Futures.immediateFuture(destAbsolutePath);
                      } else {
                        return Futures.immediateFailedFuture(
                            new RuntimeException(
                                String.format(
                                    "Failed to copy from server by string key %s", fileStringKey)));
                      }
                    },
                    executor);
              },
              executor);
        },
        executor);
  }

  /**
   * At the result handling, we copy the output directory from PCS back to PCC and then clean the
   * input directory.
   *
   * @return The future is true if the output directory is copied to remote app and input directory
   *     is cleaned successfully. It is false if any file operation fails.
   */
  @Override
  public ListenableFuture<Boolean> cleanResourceAtResultHandling(
      String sessionName, Uri convertedInputDirUri, Uri convertedOutputDirUri) {
    File convertedInputDir = PathConversionUtils.convertUriToFile(context, convertedInputDirUri);
    Uri originalOutputDirUri =
        PathConversionUtils.trimOutputPathPrefix(convertedOutputDirUri, sessionName);
    ListenableFuture<Boolean> copyFuture;
    copyFuture = fileCopyGrpcClient.copyDirToServer(convertedOutputDirUri, originalOutputDirUri);
    return Futures.transform(
        copyFuture,
        result -> {
          try {
            FileUtils.cleanDirectory(convertedInputDir);
          } catch (IOException e) {
            logger.atSevere().withCause(e).log(
                "Failed to clean the input directory at Result Handling.");
            return false;
          }
          return result;
        },
        executor);
  }

  /**
   * At clean-up routine job, we firstly check the marker file. If it's within the freeze window, we
   * do nothing. Else if it's within TTL limit, we delete everything in the /inputs root and
   * re-create the input directory using the recorded information in marker file. If it exceeds the
   * TTL limit, we forcefully delete entire the session's resource root.
   */
  @Override
  public ListenableFuture<Void> cleanResourceAtRoutineJob() {
    File localComputeRootDir =
        new File(context.getFilesDir(), PathConversionUtils.LOCAL_COMPUTE_ROOT);
    return Futures.submit(
        () -> {
          File[] allSessionDirs = localComputeRootDir.listFiles();
          if (allSessionDirs != null) {
            for (File sessionDir : allSessionDirs) {
              File sessionMarker = new File(sessionDir, MARKER_FILENAME);
              if (!sessionMarker.exists() || isExpired(sessionMarker)) {
                try {
                  FileUtils.forceDelete(sessionDir);
                } catch (IOException e) {
                  logger.atSevere().withCause(e).log(
                      "Failed to delete session resource dir: %s", sessionDir);
                }
                continue;
              }

              if (isPossiblyInTraining(sessionMarker)) {
                logger.atWarning().log(
                    "A training session is possibly in progress for session root: %s!", sessionDir);
                continue;
              }

              File inputRootDir = new File(sessionDir, PathConversionUtils.INPUT_DIRECTORY_PREFIX);
              File outputRootDir =
                  new File(sessionDir, PathConversionUtils.OUTPUT_DIRECTORY_PREFIX);
              try {
                if (inputRootDir.exists()) {
                  FileUtils.forceDelete(inputRootDir);
                }
                if (outputRootDir.exists()) {
                  FileUtils.forceDelete(outputRootDir);
                }
              } catch (IOException e) {
                logger.atSevere().withCause(e).log(
                    "Failed to clean input or output root dir at Routine clean-up job.");
              }

              try {
                List<String> allLines =
                    Files.readAllLines(Path.of(sessionMarker.getAbsolutePath()));
                if (!allLines.isEmpty()) {
                  Uri convertedInputDirUri = Uri.parse(allLines.get(0));
                  File convertedInputDir =
                      PathConversionUtils.convertUriToFile(context, convertedInputDirUri);
                  convertedInputDir.mkdirs();
                }
              } catch (IOException e) {
                logger.atSevere().withCause(e).log(
                    "Failed to create a new empty input dir based on the marker file.");
              }
            }
          }
        },
        executor);
  }

  private boolean isPossiblyInTraining(File marker) {
    try {
      List<String> allLines = Files.readAllLines(Path.of(marker.getAbsolutePath()));
      if (allLines.size() > 1) {
        long lastTrainingStartTime = Long.parseLong(allLines.get(1));
        if (timeSource
            .instant()
            .isBefore(
                Instant.ofEpochMilli(lastTrainingStartTime).plusMillis(FREEZE_WINDOW_MILLIS))) {
          return true;
        }
      }
    } catch (IOException e) {
      logger.atWarning().withCause(e).log("Failed to read the marker file.");
    }

    return false;
  }

  private boolean isExpired(File marker) {
    return timeSource
        .instant()
        .isAfter(Instant.ofEpochMilli(marker.lastModified()).plusMillis(RESOURCE_TTL_MILLIS));
  }
}
