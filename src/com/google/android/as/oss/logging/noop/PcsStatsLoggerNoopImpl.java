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

package com.google.android.as.oss.logging.noop;

import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningDiagnosisLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningSecAggClientLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningTrainingLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceUnrecognisedNetworkRequestReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceValueReported;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.common.base.Joiner;
import com.google.common.flogger.GoogleLogger;

/**
 * Provides typed access to PcsStatsLog interface.
 *
 * <p>This file defines an implementation of the PCS logging interface using logcat to log PCS
 * messages.
 *
 * <p>This code is for demonstration purposes and the implementation of the {@code PcsStatsLog}
 * interface in production builds logs messages using the (<a
 * href="https://source.android.com/docs/core/ota/modular-system/statsd">statsd system write
 * api</a>).
 */
public class PcsStatsLoggerNoopImpl implements PcsStatsLog {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  @Override
  public void logIntelligenceCountReported(IntelligenceCountReported atom) {
    logger.atInfo().log("IntelligenceCountReported: %d", atom.getCountMetricId().getNumber());
  }

  @Override
  public void logIntelligenceValueReported(IntelligenceValueReported atom) {
    logger.atInfo().log(
        "IntelligenceValueReported: %d, %d", atom.getValueMetricId().getNumber(), atom.getValue());
  }

  @Override
  public void logIntelligenceUnrecognisedNetworkRequestReported(
      IntelligenceUnrecognisedNetworkRequestReported atom) {
    logger.atInfo().log(
        "IntelligenceUnrecognisedNetworkRequestReported: %d, %s",
        atom.getConnectionType().getNumber(), atom.getConnectionKey());
  }

  @Override
  public void logIntelligenceFlTrainingLogReported(
      IntelligenceFederatedLearningTrainingLogReported atom) {
    Joiner joiner = Joiner.on(", ");
    String mergedAtom =
        joiner.join(
            atom.getFederatedComputeVersion(),
            atom.getKind().getNumber(),
            atom.getConfigName(),
            atom.getDurationMillis(),
            atom.getExampleSize(),
            atom.getRunId(),
            atom.getErrorCode().getNumber(),
            atom.getNativeHeapBytesAllocated(),
            atom.getJavaHeapTotalMemory(),
            atom.getJavaHeapFreeMemory(),
            atom.getModelIdentifier(),
            atom.getDataTransferDurationMillis(),
            atom.getBytesUploaded(),
            atom.getBytesDownloaded(),
            atom.getErrorMessage(),
            atom.getDataSource().getNumber(),
            atom.getHighWaterMarkMemoryBytes());
    logger.atInfo().log("IntelligenceFederatedLearningTrainingLogReported: %s", mergedAtom);
  }

  @Override
  public void logIntelligenceFlSecaggClientLogReported(
      IntelligenceFederatedLearningSecAggClientLogReported atom) {
    Joiner joiner = Joiner.on(", ");
    String mergedAtom =
        joiner.join(
            atom.getFederatedComputeVersion(),
            atom.getRunId(),
            atom.getConfigName(),
            atom.getModelIdentifier(),
            atom.getClientSessionId(),
            atom.getExecutionSessionId(),
            atom.getKind().getNumber(),
            atom.getDurationMillis(),
            atom.getRound().getNumber(),
            atom.getCryptoType().getNumber(),
            atom.getNumDroppedClients(),
            atom.getReceivedMessageSize(),
            atom.getSentMessageSize(),
            atom.getErrorCode().getNumber());
    logger.atInfo().log("IntelligenceFederatedLearningSecAggClientLogReported: %s", mergedAtom);
  }

  @Override
  public void logIntelligenceFlDiagLogReported(
      IntelligenceFederatedLearningDiagnosisLogReported atom) {
    logger.atInfo().log(
        "IntelligenceFederatedLearningDiagnosisLogReported: %d, %d",
        atom.getFederatedComputeVersion(), atom.getDiagCode());
  }
}
