/*
 * Copyright 2025 Google LLC
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

package com.google.android.as.oss.asr.service;

import static com.google.common.collect.ImmutableList.toImmutableList;

import com.google.android.as.oss.asr.api.LanguagepackInfo;
import com.google.android.as.oss.asr.api.ModelAvailabilityResponse;
import com.google.android.as.oss.asr.api.ModelAvailabilityResponse.ModelInfo;
import com.google.android.as.oss.asr.api.ModelDownloadResponse;
import com.google.android.as.oss.asr.api.ModelDownloadResponse.EndEvent;
import com.google.android.as.oss.asr.api.ModelDownloadResponse.ProgressEvent;
import com.google.android.as.oss.asr.api.SrsgDownloadEnd;
import com.google.android.as.oss.asr.api.SrsgDownloadProgress;
import com.google.android.as.oss.asr.api.SrsgModelAvailabilityResponse;
import com.google.android.as.oss.asr.api.SrsgModelDownloadResponse;
import com.google.android.as.oss.asr.api.SrsgModelInfo;
import com.google.common.collect.ImmutableList;
import java.util.List;

/** Helper class for converting between SRSG and SrsgProxy protos. */
final class SrsgProxyServiceHelper {

  private SrsgProxyServiceHelper() {}

  public static SrsgModelAvailabilityResponse toSrsgModelAvailabilityResponse(
      ModelAvailabilityResponse srsgResponse) {
    return SrsgModelAvailabilityResponse.newBuilder()
        .addAllDownloadedModels(toSrsgModelInfoList(srsgResponse.getDownloadedModelList()))
        .addAllDownloadableModels(toSrsgModelInfoList(srsgResponse.getDownloadableModelList()))
        .addAllDownloadingModels(toSrsgModelInfoList(srsgResponse.getDownloadingModelList()))
        .build();
  }

  private static ImmutableList<SrsgModelInfo> toSrsgModelInfoList(
      List<ModelInfo> srsgModelInfoList) {
    return srsgModelInfoList.stream()
        .map(SrsgProxyServiceHelper::toSrsgModelInfo)
        .collect(toImmutableList());
  }

  private static SrsgModelInfo toSrsgModelInfo(ModelInfo srsgModelInfo) {
    SrsgModelInfo.Builder builder = SrsgModelInfo.newBuilder();
    if (srsgModelInfo.hasLanguagepackInfo()) {
      LanguagepackInfo languagepackInfo = srsgModelInfo.getLanguagepackInfo();
      builder
          .setLocale(languagepackInfo.getLocale())
          .setVersion(String.valueOf(languagepackInfo.getVersion()))
          .setSizeBytes(languagepackInfo.getSizeBytes());
    }
    return builder.build();
  }

  public static SrsgModelDownloadResponse toSrsgModelDownloadResponse(
      ModelDownloadResponse srsgResponse) {
    SrsgModelDownloadResponse.Builder builder = SrsgModelDownloadResponse.newBuilder();
    if (srsgResponse.hasProgressEvent()) {
      builder.setProgressEvent(toSrsgDownloadProgress(srsgResponse.getProgressEvent()));
    } else if (srsgResponse.hasEndEvent()) {
      builder.setEndEvent(toSrsgDownloadEnd(srsgResponse.getEndEvent()));
    }
    return builder.build();
  }

  private static SrsgDownloadProgress toSrsgDownloadProgress(ProgressEvent srsgProgress) {
    return SrsgDownloadProgress.newBuilder()
        .setDownloadProgress(srsgProgress.getDownloadProgress())
        .setDownloadedBytes(srsgProgress.getDownloadedSize())
        .setTotalBytes(srsgProgress.getDownloadTotalSize())
        .build();
  }

  private static SrsgDownloadEnd toSrsgDownloadEnd(EndEvent srsgEndEvent) {
    return SrsgDownloadEnd.newBuilder()
        .setReason(toSrsgDownloadEndReason(srsgEndEvent.getEndReason()))
        .build();
  }

  private static SrsgDownloadEnd.Reason toSrsgDownloadEndReason(EndEvent.EndReason srsgEndReason) {
    return switch (srsgEndReason) {
      case END_REASON_COMPLETE -> SrsgDownloadEnd.Reason.SUCCESS;
      case END_REASON_DOWNLOAD_ERROR -> SrsgDownloadEnd.Reason.FAILURE;
      case END_REASON_MODEL_UNAVAILABLE -> SrsgDownloadEnd.Reason.MODEL_UNAVAILABLE;
      case END_REASON_SCHEDULED -> SrsgDownloadEnd.Reason.SCHEDULED;
      case END_REASON_NO_INTERNET_CONNECTION -> SrsgDownloadEnd.Reason.NO_INTERNET;
      // Add other mappings as needed
      default -> SrsgDownloadEnd.Reason.UNKNOWN;
    };
  }
}
