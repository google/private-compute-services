/*
 * Copyright 2023 Google LLC
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

package com.google.android.as.oss.fl.localcompute;

import static com.google.common.base.Preconditions.checkNotNull;

import android.content.Context;
import android.net.Uri;
import com.google.fcp.client.InAppTrainerOptions;
import com.google.fcp.client.TrainingInterval;
import java.io.File;
import java.nio.file.Path;

/**
 * Provides Util methods to convert {@link android.net.Uri} of local compute resources between ASI
 * and PCS.
 */
public final class PathConversionUtils {
  public static final String APP_FILES_SCHEME = "appfiles";
  public static final String APP_CACHE_SCHEME = "appcache";

  public static final String LOCAL_COMPUTE_ROOT = "/localcompute";
  public static final String COMPUTATION_PLAN_PREFIX = "/plans";
  public static final String INPUT_DIRECTORY_PREFIX = "/inputs";
  public static final String OUTPUT_DIRECTORY_PREFIX = "/outputs";

  /**
   * Add prefix to the plan file path when being copied from ASI to PCS.
   *
   * <p>Regardless of the original scheme of the plan file Uri, we always use {@code
   * APP_FILES_SCHEME} for the copy in PCS, and embed the original scheme into the path prefix.
   */
  public static Uri addPlanPathPrefix(Uri originalPlanUri, String sessionName) {
    String scheme = originalPlanUri.getScheme();
    String originalPlanPath = originalPlanUri.getPath();
    checkNotNull(originalPlanPath);
    checkNotNull(scheme);
    String convertedPath =
        Path.of(getPlanRootDirRelativePathForSession(sessionName), scheme, originalPlanPath)
            .toString();
    return Uri.parse(APP_FILES_SCHEME + ":" + convertedPath);
  }

  /**
   * Add prefix to the input dir path when being copied from ASI to PCS.
   *
   * <p>Regardless of the original scheme of the input dir Uri, we always use {@code
   * APP_FILES_SCHEME} for the copy in PCS, and embed the original scheme into the path prefix.
   */
  public static Uri addInputPathPrefix(Uri originalInputUri, String sessionName) {
    String scheme = originalInputUri.getScheme();
    String originalInputPath = originalInputUri.getPath();
    if (originalInputPath != null && scheme != null) {
      String convertedPath =
          Path.of(getInputRootDirRelativePathForSession(sessionName), scheme, originalInputPath)
              .toString();
      return Uri.parse(APP_FILES_SCHEME + ":" + convertedPath);
    } else {
      throw new IllegalArgumentException(
          String.format("Given input Uri %s has a null path or scheme", originalInputUri));
    }
  }

  /**
   * Add prefix to the output dir path when being copied from ASI to PCS.
   *
   * <p>Regardless of the original scheme of the output dir Uri, we always use {@code
   * APP_FILES_SCHEME} for the copy in PCS, and embed the original scheme into the path prefix.
   */
  public static Uri addOutputPathPrefix(Uri originalOutputUri, String sessionName) {
    String scheme = originalOutputUri.getScheme();
    String originalOutputPath = originalOutputUri.getPath();
    checkNotNull(originalOutputPath);
    checkNotNull(scheme);
    String convertedPath =
        Path.of(getOutputRootDirRelativePathForSession(sessionName), scheme, originalOutputPath)
            .toString();
    return Uri.parse(APP_FILES_SCHEME + ":" + convertedPath);
  }

  public static Uri trimPlanPathPrefix(Uri planUri, String sessionName) {
    String planPath = planUri.getPath();
    checkNotNull(planPath);
    String planSessionPrefix = getPlanRootDirRelativePathForSession(sessionName);
    if (!planPath.startsWith(planSessionPrefix)) {
      throw new IllegalArgumentException(String.format("Given plan Uri %s is invalid", planUri));
    }
    String originalPlanPathWithScheme = planPath.substring(planSessionPrefix.length());
    return extractOriginalScheme(originalPlanPathWithScheme);
  }

  public static Uri trimInputPathPrefix(Uri inputUri, String sessionName) {
    String inputPath = inputUri.getPath();
    checkNotNull(inputPath);
    String inputDirectoryPrefix = getInputRootDirRelativePathForSession(sessionName);
    if (!inputPath.startsWith(inputDirectoryPrefix)) {
      throw new IllegalArgumentException(String.format("Given input Uri %s is invalid", inputUri));
    }
    String originalInputPathWithScheme = inputPath.substring(inputDirectoryPrefix.length());
    return extractOriginalScheme(originalInputPathWithScheme);
  }

  public static Uri trimOutputPathPrefix(Uri outputUri, String sessionName) {
    String outputPath = outputUri.getPath();
    checkNotNull(outputPath);
    String outputDirectoryPrefix = getOutputRootDirRelativePathForSession(sessionName);
    if (!outputPath.startsWith(outputDirectoryPrefix)) {
      throw new IllegalArgumentException(
          String.format("Given output Uri %s is invalid", outputUri));
    }
    String originalOutputPathWithScheme = outputPath.substring(outputDirectoryPrefix.length());
    return extractOriginalScheme(originalOutputPathWithScheme);
  }

  public static Uri extractOriginalScheme(String originalPathWithScheme) {
    if (originalPathWithScheme.startsWith("/" + APP_FILES_SCHEME)) {
      return Uri.parse(
          APP_FILES_SCHEME + ":" + originalPathWithScheme.substring(APP_FILES_SCHEME.length() + 1));
    } else if (originalPathWithScheme.startsWith("/" + APP_CACHE_SCHEME)) {
      return Uri.parse(
          APP_CACHE_SCHEME + ":" + originalPathWithScheme.substring(APP_CACHE_SCHEME.length() + 1));
    } else {
      throw new IllegalArgumentException(
          String.format("Unsupported original scheme: %s", originalPathWithScheme));
    }
  }

  public static File convertUriToFile(Context context, Uri uri) {
    String scheme = uri.getScheme();
    String path = uri.getPath();
    checkNotNull(scheme);
    checkNotNull(path);
    if (APP_FILES_SCHEME.equals(scheme)) {
      return new File(context.getFilesDir(), path);
    } else if (APP_CACHE_SCHEME.equals(scheme)) {
      return new File(context.getCacheDir(), path);
    } else {
      throw new IllegalArgumentException(String.format("Unsupported Uri scheme: %s", scheme));
    }
  }

  public static Uri convertFileToUri(Context context, File file) {
    String filePath = file.getAbsolutePath();
    if (filePath.startsWith(context.getFilesDir().toString())) {
      return Uri.parse(
          APP_FILES_SCHEME + ":" + filePath.substring(context.getFilesDir().toString().length()));
    } else if (filePath.startsWith(context.getCacheDir().toString())) {
      return Uri.parse(
          APP_CACHE_SCHEME + ":" + filePath.substring(context.getCacheDir().toString().length()));
    } else {
      throw new IllegalArgumentException(
          String.format("Unsupported file path root directory: %s", filePath));
    }
  }

  public static InAppTrainerOptions trimLocalComputePathPrefix(InAppTrainerOptions options) {
    if (options.getPersonalizationPlan() == null) {
      return options;
    }

    InAppTrainerOptions.Builder builder =
        InAppTrainerOptions.newBuilder()
            .setAttestationMode(options.getAttestationMode())
            .setJobSchedulerJobId(
                options.getJobSchedulerJobId(), options.getAllowFallbackToAutoGeneratedJobId())
            .setSessionName(options.getSessionName());

    Uri personalizationPlan = options.getPersonalizationPlan();
    Uri inputDirectory = options.getInputDirectory();
    Uri outputDirectory = options.getOutputDirectory();
    String sessionName = options.getSessionName();
    if (personalizationPlan != null && inputDirectory != null && outputDirectory != null) {
      builder.setLocalComputationOptions(
          trimPlanPathPrefix(personalizationPlan, sessionName),
          trimInputPathPrefix(inputDirectory, sessionName),
          trimOutputPathPrefix(outputDirectory, sessionName));
    }

    TrainingInterval interval = options.getTrainingInterval();
    if (interval != null) {
      builder.setTrainingInterval(
          TrainingInterval.newBuilder()
              .setMinimumIntervalMillis(interval.getMinimumIntervalMillis())
              .setSchedulingMode(interval.getSchedulingMode())
              .build());
    }

    builder.setContextData(options.getContextData());

    return builder.build();
  }

  public static String getPlanRootDirRelativePathForSession(String sessionName) {
    return getResourceRootDirRelativePathForSession(sessionName) + COMPUTATION_PLAN_PREFIX;
  }

  public static String getInputRootDirRelativePathForSession(String sessionName) {
    return getResourceRootDirRelativePathForSession(sessionName) + INPUT_DIRECTORY_PREFIX;
  }

  public static String getOutputRootDirRelativePathForSession(String sessionName) {
    return getResourceRootDirRelativePathForSession(sessionName) + OUTPUT_DIRECTORY_PREFIX;
  }

  public static String getResourceRootDirRelativePathForSession(String sessionName) {
    return LOCAL_COMPUTE_ROOT + "/" + normalizeSessionName(sessionName);
  }

  public static String normalizeSessionName(String sessionName) {
    return sessionName.strip().replaceAll("[^A-Za-z0-9]", "_");
  }

  private PathConversionUtils() {}
}
