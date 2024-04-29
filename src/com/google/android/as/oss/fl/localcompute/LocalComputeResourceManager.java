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

package com.google.android.as.oss.fl.localcompute;

import android.net.Uri;
import com.google.common.util.concurrent.ListenableFuture;

/**
 * A local compute task resource manager interface that provides APIs for handling the
 * plan|input|output files at different time points.
 */
public interface LocalComputeResourceManager {

  /**
   * Prepare the local compute task resources during sheduling.
   *
   * <p>At this step, the local compute training plan should be copied from PCC and an empty input
   * directory should be created.
   *
   * @param sessionName the session name of the scheduled local compute task.
   * @param originalPlanUri the original {@link android.net.Uri} of the training plan in PCC.
   * @param originalInputDirUri the original {@link android.net.Uri} of the input directory in PCC.
   * @return a future representing if the resource preparation is successful.
   */
  public ListenableFuture<Boolean> prepareResourceAtScheduling(
      String sessionName, Uri originalPlanUri, Uri originalInputDirUri);

  /**
   * Clean the local compute task resources during cancellation.
   *
   * @param sessionName the session name of the canceled local compute task.
   * @return a future representing if the resource cleaning is successful.
   */
  public ListenableFuture<Boolean> cleanResourceAtCancellation(String sessionName);

  /**
   * Copy the local compute task resources at the beginning of training.
   *
   * @param sessionName the session name of the in-training local compute task.
   * @param fileStringKey the unique string key for filepath lookup in ASI.
   * @return a future of the absolute path of the copied file/directory if the resource copy is
   *     successful.
   */
  public ListenableFuture<String> copyResourceAtTraining(String sessionName, String fileStringKey);

  /**
   * Clean the local compute task resources at result handling.
   *
   * <p>At this step, the input directory in PCS will be cleaned up and the output directory will be
   * copied back to PCC.
   *
   * @param sessionName the session name of the in-training local compute task.
   * @param convertedInputDirUri the converted {@link android.net.Uri} of the input directory in
   *     PCS.
   * @param convertedOutputDirUri the converted {@link android.net.Uri} of the output directory in
   *     PCS.
   * @return a future representing if the resource cleaning is successful.
   */
  public ListenableFuture<Boolean> cleanResourceAtResultHandling(
      String sessionName, Uri convertedInputDirUri, Uri convertedOutputDirUri);

  /** Clean up the resources for all local compute tasks at routine job. */
  public ListenableFuture<Void> cleanResourceAtRoutineJob();
}
