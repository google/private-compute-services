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

package com.google.android.as.oss.logging.converter;

import com.android.os.AtomsProto;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceCountReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningDiagnosisLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningSecAggClientLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceFederatedLearningTrainingLogReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceUnrecognisedNetworkRequestReported;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceUnrecognisedNetworkRequestReported.ConnectionType;
import com.google.android.as.oss.logging.PcsAtomsProto.IntelligenceValueReported;
import com.google.android.as.oss.logging.PcsStatsEnums;
import com.google.android.as.oss.logging.PcsStatsEnums.CountMetricId;
import com.google.android.as.oss.logging.PcsStatsEnums.SecAggClientCryptoOperationType;
import com.google.android.as.oss.logging.PcsStatsEnums.SecAggClientErrorCode;
import com.google.android.as.oss.logging.PcsStatsEnums.SecAggClientEventKind;
import com.google.android.as.oss.logging.PcsStatsEnums.SecAggClientRound;
import com.google.android.as.oss.logging.PcsStatsEnums.TrainingDataSourceType;
import com.google.android.as.oss.logging.PcsStatsEnums.TrainingErrorCode;
import com.google.android.as.oss.logging.PcsStatsEnums.TrainingEventKind;
import com.google.android.as.oss.logging.PcsStatsEnums.ValueMetricId;
import com.google.fcp.client.LogManager.LongHistogramCounter;
import com.google.fcp.client.LogManager.TimerHistogramCounter;
import com.google.common.converter.auto.AutoConverter;
import com.google.common.converter.auto.AutoEnumConverter;
import com.google.common.converter.auto.ForAutoConverter;
import com.google.intelligence.fcp.client.HistogramCounters;
import com.google.protos.federated_learning.FlEnums;
import com.google.protos.logs.proto.wireless.android.stats.intelligence.IntelligenceStatsEnums;
import java.util.function.Function;

/**
 * Implements one way converters from {@link PcsAtomsProto} to {@link AtomsProto} using [redacted]
 */
public final class PcsLogMessageConverter {

  public static final TimerHistogramCounterConverter TIMER_HISTOGRAM_COUNTER_CONVERTER =
      new TimerHistogramCounterConverter();
  public static final LongHistogramCounterConverter LONG_HISTOGRAM_COUNTER_CONVERTER =
      new LongHistogramCounterConverter();
  public static final HistogramCounterConverter HISTOGRAM_COUNTER_CONVERTER =
      new HistogramCounterConverter();

  /** Converter for {@link IntelligenceCountReported}. */
  @AutoConverter(
      from = IntelligenceCountReported.class,
      to = AtomsProto.IntelligenceCountReported.class,
      implementing = Function.class)
  public static class IntelligenceCountReportedConverter
      extends AutoConverter_PcsLogMessageConverter_IntelligenceCountReportedConverter {
    @ForAutoConverter
    static final Function<CountMetricId, IntelligenceStatsEnums.CountMetricId>
        COUNT_METRIC_ID_CONVERTER = new CountMetricIdConverter();
  }

  /** Converter for {@link CountMetricId}. */
  @AutoEnumConverter(
      from = CountMetricId.class,
      to = IntelligenceStatsEnums.CountMetricId.class,
      implementing = Function.class)
  private static class CountMetricIdConverter
      extends AutoEnumConverter_PcsLogMessageConverter_CountMetricIdConverter {}

  /** Converter for {@link IntelligenceValueReported}. */
  @AutoConverter(
      from = IntelligenceValueReported.class,
      to = AtomsProto.IntelligenceValueReported.class,
      implementing = Function.class)
  public static class IntelligenceValueReportedConverter
      extends AutoConverter_PcsLogMessageConverter_IntelligenceValueReportedConverter {
    @ForAutoConverter
    static final Function<ValueMetricId, IntelligenceStatsEnums.ValueMetricId>
        VALUE_METRIC_ID_CONVERTER = new ValueMetricIdConverter();
  }

  /** Converter for {@link ValueMetricId}. */
  @AutoEnumConverter(
      from = ValueMetricId.class,
      to = IntelligenceStatsEnums.ValueMetricId.class,
      implementing = Function.class)
  private static class ValueMetricIdConverter
      extends AutoEnumConverter_PcsLogMessageConverter_ValueMetricIdConverter {}

  /** Converter for {@link IntelligenceUnrecognisedNetworkRequestReported} */
  @AutoConverter(
      from = IntelligenceUnrecognisedNetworkRequestReported.class,
      to = AtomsProto.IntelligenceUnrecognisedNetworkRequestReported.class,
      implementing = Function.class)
  public static class IntelligenceUnrecognisedNetworkRequestReportedConverter
      extends AutoConverter_PcsLogMessageConverter_IntelligenceUnrecognisedNetworkRequestReportedConverter {
    @ForAutoConverter
    static final Function<
            ConnectionType,
            AtomsProto.IntelligenceUnrecognisedNetworkRequestReported.ConnectionType>
        CONNECTION_TYPE_CONVERTER = new ConnectionTypeConverter();
  }

  /** Converter for {@link ConnectionType}. */
  @AutoEnumConverter(
      from = ConnectionType.class,
      to = AtomsProto.IntelligenceUnrecognisedNetworkRequestReported.ConnectionType.class,
      implementing = Function.class)
  private static class ConnectionTypeConverter
      extends AutoEnumConverter_PcsLogMessageConverter_ConnectionTypeConverter {}

  /** Converter for {@link IntelligenceFederatedLearningDiagnosisLogReported}. */
  @AutoConverter(
      from = IntelligenceFederatedLearningDiagnosisLogReported.class,
      to = AtomsProto.IntelligenceFederatedLearningDiagnosisLogReported.class,
      implementing = Function.class,
      mappings = {
        @AutoConverter.Mapping(from = "federated_compute_version", to = "fc_version"),
      })
  public static class IntelligenceFlDiagLogReportedConverter
      extends AutoConverter_PcsLogMessageConverter_IntelligenceFlDiagLogReportedConverter {}

  /** Converter for {@link IntelligenceFederatedLearningTrainingLogReported}. */
  @AutoConverter(
      from = IntelligenceFederatedLearningTrainingLogReported.class,
      to = AtomsProto.IntelligenceFederatedLearningTrainingLogReported.class,
      implementing = Function.class,
      mappings = {
        @AutoConverter.Mapping(from = "federated_compute_version", to = "fc_version"),
      })
  public static class IntelligenceFlTrainingLogReportedConverter
      extends AutoConverter_PcsLogMessageConverter_IntelligenceFlTrainingLogReportedConverter {
    @ForAutoConverter
    static final Function<TrainingEventKind, FlEnums.TrainingEventKind>
        TRAINING_EVENT_KIND_CONVERTER = new TrainingEventKindConverter();

    @ForAutoConverter
    static final Function<TrainingErrorCode, FlEnums.TrainingErrorCode>
        TRAINING_ERROR_CODE_CONVERTER = new TrainingErrorCodeConverter();

    @ForAutoConverter
    static final Function<TrainingDataSourceType, FlEnums.TrainingDataSourceType>
        TRAINING_DATA_SOURCE_TYPE_CONVERTER = new TrainingDataSourceTypeConverter();

    @ForAutoConverter
    static final Function<PcsStatsEnums.HistogramCounters, FlEnums.HistogramCounters>
        HISTOGRAM_COUNTERS_CONVERTER = new HistogramCountersConverter();

    @ForAutoConverter
    static final Function<PcsStatsEnums.CollectionName, FlEnums.CollectionName>
        COLLECTION_NAME_CONVERTER = new CollectionNameConverter();
  }

  /** Converter for {@link TrainingEventKind}. */
  @AutoEnumConverter(
      from = TrainingEventKind.class,
      to = FlEnums.TrainingEventKind.class,
      implementing = Function.class)
  private static class TrainingEventKindConverter
      extends AutoEnumConverter_PcsLogMessageConverter_TrainingEventKindConverter {}

  /** Converter for {@link TrainingErrorCode}. */
  @AutoEnumConverter(
      from = TrainingErrorCode.class,
      to = FlEnums.TrainingErrorCode.class,
      implementing = Function.class)
  private static class TrainingErrorCodeConverter
      extends AutoEnumConverter_PcsLogMessageConverter_TrainingErrorCodeConverter {}

  /** Converter for {@link TrainingDataSourceType}. */
  @AutoEnumConverter(
      from = TrainingDataSourceType.class,
      to = FlEnums.TrainingDataSourceType.class,
      implementing = Function.class)
  private static class TrainingDataSourceTypeConverter
      extends AutoEnumConverter_PcsLogMessageConverter_TrainingDataSourceTypeConverter {}

  /** Converter for {@link HistogramCounters}. */
  @AutoEnumConverter(
      from = PcsStatsEnums.HistogramCounters.class,
      to = FlEnums.HistogramCounters.class,
      implementing = Function.class)
  private static class HistogramCountersConverter
      extends AutoEnumConverter_PcsLogMessageConverter_HistogramCountersConverter {}

  /** Converter for {@link CollectionName}. */
  @AutoEnumConverter(
      from = PcsStatsEnums.CollectionName.class,
      to = FlEnums.CollectionName.class,
      implementing = Function.class,
      mappings = {})
  private static class CollectionNameConverter
      extends AutoEnumConverter_PcsLogMessageConverter_CollectionNameConverter {}

  /** Converter for {@link IntelligenceFederatedLearningSecAggClientLogReported}. */
  @AutoConverter(
      from = IntelligenceFederatedLearningSecAggClientLogReported.class,
      to = AtomsProto.IntelligenceFederatedLearningSecAggClientLogReported.class,
      implementing = Function.class,
      mappings = {
        @AutoConverter.Mapping(from = "federated_compute_version", to = "fc_version"),
      })
  public static class IntelligenceFlSecaggClientLogReportedConverter
      extends AutoConverter_PcsLogMessageConverter_IntelligenceFlSecaggClientLogReportedConverter {
    @ForAutoConverter
    static final Function<SecAggClientEventKind, FlEnums.SecAggClientEventKind>
        SEC_AGG_CLIENT_EVENT_KIND_CONVERTER = new SecAggClientEventKindConverter();

    @ForAutoConverter
    static final Function<SecAggClientCryptoOperationType, FlEnums.SecAggClientCryptoOperationType>
        CRYPTO_OPERATION_TYPE_CONVERTER = new SecAggClientCryptoOperationTypeConverter();

    @ForAutoConverter
    static final Function<SecAggClientRound, FlEnums.SecAggClientRound> CLIENT_ROUND_CONVERTER =
        new SecAggClientRoundConverter();

    @ForAutoConverter
    static final Function<SecAggClientErrorCode, FlEnums.SecAggClientErrorCode>
        CLIENT_ERROR_CODE_CONVERTER = new SecAggClientErrorCodeConverter();
  }

  /** Converter for {@link SecAggClientEventKind}. */
  @AutoEnumConverter(
      from = SecAggClientEventKind.class,
      to = FlEnums.SecAggClientEventKind.class,
      implementing = Function.class)
  private static class SecAggClientEventKindConverter
      extends AutoEnumConverter_PcsLogMessageConverter_SecAggClientEventKindConverter {}

  /** Converter for {@link SecAggClientCryptoOperationType}. */
  @AutoEnumConverter(
      from = SecAggClientCryptoOperationType.class,
      to = FlEnums.SecAggClientCryptoOperationType.class,
      implementing = Function.class)
  private static class SecAggClientCryptoOperationTypeConverter
      extends AutoEnumConverter_PcsLogMessageConverter_SecAggClientCryptoOperationTypeConverter {}

  /** Converter for {@link SecAggClientRound}. */
  @AutoEnumConverter(
      from = SecAggClientRound.class,
      to = FlEnums.SecAggClientRound.class,
      implementing = Function.class)
  private static class SecAggClientRoundConverter
      extends AutoEnumConverter_PcsLogMessageConverter_SecAggClientRoundConverter {}

  /** Converter for {@link SecAggClientErrorCode}. */
  @AutoEnumConverter(
      from = SecAggClientErrorCode.class,
      to = FlEnums.SecAggClientErrorCode.class,
      implementing = Function.class)
  private static class SecAggClientErrorCodeConverter
      extends AutoEnumConverter_PcsLogMessageConverter_SecAggClientErrorCodeConverter {}

  /** Converter for {@link TimerHistogramCounter}. */
  @AutoEnumConverter(
      from = TimerHistogramCounter.class,
      to = PcsStatsEnums.HistogramCounters.class,
      implementing = Function.class)
  public static class TimerHistogramCounterConverter
      extends AutoEnumConverter_PcsLogMessageConverter_TimerHistogramCounterConverter {}

  /** Converter for {@link LongHistogramCounter}. */
  @AutoEnumConverter(
      from = LongHistogramCounter.class,
      to = PcsStatsEnums.HistogramCounters.class,
      implementing = Function.class)
  public static class LongHistogramCounterConverter
      extends AutoEnumConverter_PcsLogMessageConverter_LongHistogramCounterConverter {}

  /** Converter for fcp {@link HistogramCounters}. */
  @AutoEnumConverter(
      from = HistogramCounters.class,
      to = PcsStatsEnums.HistogramCounters.class,
      implementing = Function.class,
      mappings = {
        @AutoConverter.Mapping(from = "UNRECOGNIZED", to = "HISTOGRAM_COUNTER_UNDEFINED"),
      })
  public static class HistogramCounterConverter
      extends AutoEnumConverter_PcsLogMessageConverter_HistogramCounterConverter {}

  private PcsLogMessageConverter() {}
}
