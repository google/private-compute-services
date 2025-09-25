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

package com.google.android.as.oss.logging.impl;

import com.google.android.as.oss.asi.common.logging.IntelligenceStatsLog;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningDiagnosisLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningSecAggClientLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningTrainingLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceUnrecognisedNetworkRequestReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceValueReported;
import com.google.android.as.oss.logging.PcsStatsLog;
import com.google.android.as.oss.logging.converter.PcsLogMessageConverter.IntelligenceCountReportedConverter;
import com.google.android.as.oss.logging.converter.PcsLogMessageConverter.IntelligenceFlDiagLogReportedConverter;
import com.google.android.as.oss.logging.converter.PcsLogMessageConverter.IntelligenceFlSecaggClientLogReportedConverter;
import com.google.android.as.oss.logging.converter.PcsLogMessageConverter.IntelligenceFlTrainingLogReportedConverter;
import com.google.android.as.oss.logging.converter.PcsLogMessageConverter.IntelligenceUnrecognisedNetworkRequestReportedConverter;
import com.google.android.as.oss.logging.converter.PcsLogMessageConverter.IntelligenceValueReportedConverter;

/**
 * Provides typed access to PcsStatsLog in production builds of PCS.
 *
 * <p>Forwards Statsd logs in PCS to {@code IntelligenceStatsLog}. This is the only allowed usage of
 * {@code IntelligenceStatsLog}, in PCS. {@code PcsStatsLog} should be used instead for logging of
 * open source messages in PCS.
 */
public class PcsStatsLoggerImpl implements PcsStatsLog {
  private final IntelligenceStatsLog intelligenceStatsLogger;

  private static final IntelligenceCountReportedConverter INTELLIGENCE_COUNT_REPORTED_CONVERTER =
      new IntelligenceCountReportedConverter();
  private static final IntelligenceValueReportedConverter INTELLIGENCE_VALUE_REPORTED_CONVERTER =
      new IntelligenceValueReportedConverter();
  private static final IntelligenceUnrecognisedNetworkRequestReportedConverter
      INTELLIGENCE_UNRECOGNISED_NETWORK_REQUEST_REPORTED_CONVERTER =
          new IntelligenceUnrecognisedNetworkRequestReportedConverter();
  private static final IntelligenceFlDiagLogReportedConverter
      INTELLIGENCE_FL_DIAG_LOG_REPORTED_CONVERTER = new IntelligenceFlDiagLogReportedConverter();
  private static final IntelligenceFlTrainingLogReportedConverter
      INTELLIGENCE_FL_TRAINING_LOG_REPORTED_CONVERTER =
          new IntelligenceFlTrainingLogReportedConverter();
  private static final IntelligenceFlSecaggClientLogReportedConverter
      INTELLIGENCE_FL_SECAGG_CLIENT_LOG_REPORTED_CONVERTER =
          new IntelligenceFlSecaggClientLogReportedConverter();

  public PcsStatsLoggerImpl(IntelligenceStatsLog intelligenceStatsLogger) {
    this.intelligenceStatsLogger = intelligenceStatsLogger;
  }

  @Override
  public void logIntelligenceCountReported(IntelligenceCountReported atom) {
    intelligenceStatsLogger.logIntelligenceCountReported(
        INTELLIGENCE_COUNT_REPORTED_CONVERTER.apply(atom));
  }

  @Override
  public void logIntelligenceValueReported(IntelligenceValueReported atom) {
    intelligenceStatsLogger.logIntelligenceValueReported(
        INTELLIGENCE_VALUE_REPORTED_CONVERTER.apply(atom));
  }

  @Override
  public void logIntelligenceUnrecognisedNetworkRequestReported(
      IntelligenceUnrecognisedNetworkRequestReported atom) {
    intelligenceStatsLogger.logIntelligenceUnrecognisedNetworkRequestReported(
        INTELLIGENCE_UNRECOGNISED_NETWORK_REQUEST_REPORTED_CONVERTER.apply(atom));
  }

  @Override
  public void logIntelligenceFlTrainingLogReported(
      IntelligenceFederatedLearningTrainingLogReported atom) {
    intelligenceStatsLogger.logIntelligenceFlTrainingLogReported(
        INTELLIGENCE_FL_TRAINING_LOG_REPORTED_CONVERTER.apply(atom));
  }

  @Override
  public void logIntelligenceFlSecaggClientLogReported(
      IntelligenceFederatedLearningSecAggClientLogReported atom) {
    intelligenceStatsLogger.logIntelligenceFlSecaggClientLogReported(
        INTELLIGENCE_FL_SECAGG_CLIENT_LOG_REPORTED_CONVERTER.apply(atom));
  }

  @Override
  public void logIntelligenceFlDiagLogReported(
      IntelligenceFederatedLearningDiagnosisLogReported atom) {
    intelligenceStatsLogger.logIntelligenceFlDiagLogReported(
        INTELLIGENCE_FL_DIAG_LOG_REPORTED_CONVERTER.apply(atom));
  }
}
