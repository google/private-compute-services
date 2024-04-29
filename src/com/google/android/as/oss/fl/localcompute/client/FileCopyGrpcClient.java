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

package com.google.android.as.oss.fl.localcompute.client;

import static com.google.android.as.oss.fl.localcompute.api.MetadataContextKeys.FILE_DESCRIPTOR_METADATA_KEY;

import android.content.Context;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.google.android.as.oss.common.ExecutorAnnotations.IoExecutorQualifier;
import com.google.android.as.oss.fl.localcompute.PathConversionUtils;
import com.google.android.as.oss.fl.localcompute.api.proto.FileCopyRequest;
import com.google.android.as.oss.fl.localcompute.api.proto.FileCopyResponse;
import com.google.android.as.oss.fl.localcompute.api.proto.FileCopyServiceGrpc;
import com.google.android.as.oss.fl.localcompute.api.proto.TraverseDirRequest;
import com.google.android.as.oss.fl.localcompute.api.proto.TraverseDirResponse;
import com.google.android.as.oss.fl.localcompute.api.proto.UriQueryRequest;
import com.google.android.as.oss.fl.localcompute.api.proto.UriQueryResponse;
import com.google.android.as.oss.fl.localcompute.client.ChannelModule.AsiGrpcChannel;
import com.google.common.flogger.GoogleLogger;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import dagger.hilt.android.qualifiers.ApplicationContext;
import io.grpc.Channel;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executor;
import javax.inject.Inject;

/** A Grpc client that handles file copy between ASI and PCS needed for local compute tasks. */
public class FileCopyGrpcClient {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private final Context context;
  private final Channel onDeviceChannel;
  private final Executor executor;

  @Inject
  FileCopyGrpcClient(
      @ApplicationContext Context context,
      @AsiGrpcChannel Channel onDeviceChannel,
      @IoExecutorQualifier Executor executor) {
    this.context = context;
    this.onDeviceChannel = onDeviceChannel;
    this.executor = executor;
  }

  public ListenableFuture<UriQueryResponse> queryFileUriByStringKey(
      String sessionName, String fileStringKey) {
    logger.atFine().log(
        "Start query file Uri by string key %s for session %s", fileStringKey, sessionName);

    return Futures.submit(
        () -> {
          FileCopyServiceGrpc.FileCopyServiceBlockingStub stub =
              FileCopyServiceGrpc.newBlockingStub(onDeviceChannel);
          UriQueryRequest request =
              UriQueryRequest.newBuilder()
                  .setSessionName(sessionName)
                  .setFileKey(fileStringKey)
                  .build();
          return stub.queryFileUri(request);
        },
        executor);
  }

  public ListenableFuture<Boolean> copyFileFromServer(Uri srcFileUri, Uri destFileUri) {
    return Futures.submit(() -> copyFileFromServerBlocking(srcFileUri, destFileUri), executor);
  }

  public ListenableFuture<Boolean> copyFileToServer(Uri srcFileUri, Uri destFileUri) {
    return Futures.submit(() -> copyFileToServerBlocking(srcFileUri, destFileUri), executor);
  }

  public ListenableFuture<Boolean> copyDirFromServer(Uri srcDirUri, Uri destDirUri) {
    return Futures.submit(
        () -> {
          FileCopyServiceGrpc.FileCopyServiceBlockingStub stub =
              FileCopyServiceGrpc.newBlockingStub(onDeviceChannel);
          TraverseDirRequest request =
              TraverseDirRequest.newBuilder().setDirUri(srcDirUri.toString()).build();
          TraverseDirResponse response = stub.traverseDir(request);

          File srcDir = PathConversionUtils.convertUriToFile(context, srcDirUri);
          for (String childFileUriStr : response.getChildFileUriList()) {
            Uri srcFileUri = Uri.parse(childFileUriStr);
            File srcFile = PathConversionUtils.convertUriToFile(context, srcFileUri);
            String srcFileRelativePath = srcDir.toPath().relativize(srcFile.toPath()).toString();
            File destFile =
                new File(
                    PathConversionUtils.convertUriToFile(context, destDirUri), srcFileRelativePath);
            Uri destFileUri = PathConversionUtils.convertFileToUri(context, destFile);
            if (!copyFileFromServerBlocking(srcFileUri, destFileUri)) {
              return false;
            }
          }
          return true;
        },
        executor);
  }

  public ListenableFuture<Boolean> copyDirToServer(Uri srcDirUri, Uri destDirUri) {
    return Futures.submit(
        () -> {
          File srcDir = PathConversionUtils.convertUriToFile(context, srcDirUri);
          if (!srcDir.isDirectory()) {
            logger.atSevere().log(
                "The given source directory %s is not a directory or does not exist", srcDir);
            return false;
          }
          for (File srcFile : Files.fileTraverser().depthFirstPreOrder(srcDir)) {
            if (!srcFile.isDirectory()) {
              String srcfileRelativePath = srcDir.toPath().relativize(srcFile.toPath()).toString();
              File destFile =
                  new File(
                      PathConversionUtils.convertUriToFile(context, destDirUri),
                      srcfileRelativePath);
              Uri destFileUri = PathConversionUtils.convertFileToUri(context, destFile);
              Uri srcFileUri = PathConversionUtils.convertFileToUri(context, srcFile);
              if (!copyFileToServerBlocking(srcFileUri, destFileUri)) {
                return false;
              }
            }
          }
          return true;
        },
        executor);
  }

  private Boolean copyFileFromServerBlocking(Uri srcFileUri, Uri destFileUri) {
    File destFile = PathConversionUtils.convertUriToFile(context, destFileUri);
    try {
      if (destFile.getParentFile() != null) {
        destFile.getParentFile().mkdirs();
      }
      if (!destFile.createNewFile()) {
        logger.atWarning().log("File %s already exists, overwriting it.", destFileUri);
      }
    } catch (IOException e) {
      logger.atSevere().withCause(e).log("Failed to create the file %s", destFileUri);
      destFile.delete();
      return false;
    }

    boolean copySuccess = false;
    try (ParcelFileDescriptor destFileDescriptor =
        ParcelFileDescriptor.open(destFile, ParcelFileDescriptor.MODE_WRITE_ONLY)) {
      Metadata headers = new Metadata();
      headers.put(FILE_DESCRIPTOR_METADATA_KEY, destFileDescriptor);
      FileCopyServiceGrpc.FileCopyServiceBlockingStub stub =
          FileCopyServiceGrpc.newBlockingStub(onDeviceChannel)
              .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
      FileCopyRequest request =
          FileCopyRequest.newBuilder().setFileUri(srcFileUri.toString()).build();
      FileCopyResponse response = stub.copyToFileDescriptor(request);
      copySuccess = response.getSuccess();
    } catch (IOException e) {
      logger.atWarning().withCause(e).log("Failed to close the file descriptor.");
    } finally {
      if (!copySuccess) {
        destFile.delete();
      }
    }
    return copySuccess;
  }

  private Boolean copyFileToServerBlocking(Uri srcFileUri, Uri destFileUri) {
    File srcFile = PathConversionUtils.convertUriToFile(context, srcFileUri);
    if (!srcFile.isFile()) {
      logger.atSevere().log("This file %s is not a normal file or does not exist", srcFile);
      return false;
    }

    try (ParcelFileDescriptor srcFileDescriptor =
        ParcelFileDescriptor.open(srcFile, ParcelFileDescriptor.MODE_READ_ONLY)) {
      Metadata headers = new Metadata();
      headers.put(FILE_DESCRIPTOR_METADATA_KEY, srcFileDescriptor);
      FileCopyServiceGrpc.FileCopyServiceBlockingStub stub =
          FileCopyServiceGrpc.newBlockingStub(onDeviceChannel)
              .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
      FileCopyRequest request =
          FileCopyRequest.newBuilder().setFileUri(destFileUri.toString()).build();
      FileCopyResponse response = stub.copyFromFileDescriptor(request);
      return response.getSuccess();
    } catch (IOException e) {
      logger.atSevere().withCause(e).log("Failed to close the file descriptor.");
      return false;
    }
  }
}
