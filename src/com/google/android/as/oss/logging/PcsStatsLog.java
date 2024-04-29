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

package com.google.android.as.oss.logging;

import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningDiagnosisLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningSecAggClientLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningTrainingLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceUnrecognisedNetworkRequestReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceValueReported;

/** Logging interface for PCS logs. */
public interface PcsStatsLog {
  void logIntelligenceCountReported(IntelligenceCountReported atom);

  void logIntelligenceValueReported(IntelligenceValueReported atom);

  void logIntelligenceUnrecognisedNetworkRequestReported(
      IntelligenceUnrecognisedNetworkRequestReported atom);

  void logIntelligenceFlTrainingLogReported(IntelligenceFederatedLearningTrainingLogReported atom);

  void logIntelligenceFlSecaggClientLogReported(
      IntelligenceFederatedLearningSecAggClientLogReported atom);

  void logIntelligenceFlDiagLogReported(IntelligenceFederatedLearningDiagnosisLogReported atom);
}
