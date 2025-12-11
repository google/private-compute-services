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

package com.google.android.as.oss.fl.federatedcompute.attestation;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.android.as.oss.attestation.AttestationMeasurementRequest;
import com.google.android.as.oss.attestation.PccAttestationMeasurementClient;
import com.google.android.as.oss.attestation.api.proto.AttestationMeasurementResponse;
import com.google.android.as.oss.attestation.config.PcsAttestationMeasurementConfig;
import com.google.android.as.oss.common.config.ConfigReader;
import com.google.android.as.oss.common.time.TimeSource;
import com.google.fcp.client.AttestationClient;
import com.google.common.io.BaseEncoding;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * An Implementation of {@link AttestationClient}, that is used for generating an attestation
 * measurement for federated computation.
 */
public class FcAttestationClient implements AttestationClient {
  private final PccAttestationMeasurementClient attestationMeasurementClient;
  private final Executor executor;

  private final TimeSource timeSource;

  private final ConfigReader<PcsAttestationMeasurementConfig> attestationMeasurementConfigReader;

  private final Random randomGenerator;

  static FcAttestationClient create(
      PccAttestationMeasurementClient attestationMeasurementClient,
      Executor executor,
      TimeSource timeSource,
      ConfigReader<PcsAttestationMeasurementConfig> attestationMeasurementConfigReader,
      Random randomGenerator) {
    return new FcAttestationClient(
        attestationMeasurementClient,
        executor,
        timeSource,
        attestationMeasurementConfigReader,
        randomGenerator);
  }

  @Override
  public void requestMeasurement(ResultsCallback resultsCallback) {
    requestMeasurement(resultsCallback, "");
  }

  @Override
  public void requestMeasurement(ResultsCallback resultsCallback, String contentBinding) {
    AttestationMeasurementRequest.Builder attestationMeasurementRequestBuilder =
        AttestationMeasurementRequest.builder();

    if (!contentBinding.isEmpty()) {
      attestationMeasurementRequestBuilder.setContentBinding(contentBinding);
    }

    AttestationMeasurementRequest attestationMeasurementRequest =
        AttestationMeasurementRequest.builder().build();

    FluentFuture.from(requestMeasurementDelay())
        .transformAsync(
            unused ->
                attestationMeasurementClient.requestAttestationMeasurement(
                    attestationMeasurementRequest),
            executor)
        .addCallback(
            new FutureCallback<>() {
              @Override
              public void onSuccess(AttestationMeasurementResponse result) {
                resultsCallback.onCompleted(encodeResponse(result));
              }

              @Override
              public void onFailure(Throwable t) {
                resultsCallback.onFailure(t);
              }
            },
            executor);
  }

  // Helps to encode an AttestationMeasurementResponse, to a string, as required by
  // federated-compute side plumbing.
  private String encodeResponse(AttestationMeasurementResponse response) {
    return BaseEncoding.base64().encode(response.toByteArray());
  }

  /**
   * Add random delay before measurement request. The delay is only added if the request is on the
   * hour, where we see the highest batch of job scheduler wake ups, for Fc.
   *
   * <p>Ideally, this delay should help distribute the requests during the peak time, to avoid huge
   * server-side spikes.
   */
  private ListenableFuture<Void> requestMeasurementDelay() {
    return Futures.submit(
        () -> {
          {
            if (attestationMeasurementConfigReader.getConfig().enableRandomJitter()) {
              LocalTime localTime = timeSource.now().atZone(ZoneOffset.UTC).toLocalTime();
              if (isCloseToTheHour(localTime)) {
                long randomDelaySeconds =
                    (long)
                        ((double)
                                (attestationMeasurementConfigReader.getConfig().maxDelaySeconds()
                                    - attestationMeasurementConfigReader
                                        .getConfig()
                                        .minDelaySeconds())
                            * randomGenerator.nextDouble());
                CountDownLatch latch = new CountDownLatch(1);

                try {
                  latch.await(randomDelaySeconds, SECONDS);
                } catch (InterruptedException e) {
                  // We probably should not continue work on the thread after an interruption.
                  throw new IllegalStateException(e);
                }
              }
            }
          }
        },
        executor);
  }

  /**
   * Helper method to check if a {@link LocalTime} is close to the hour.
   *
   * <p>Returns true if the time is 30 seconds before or after the hour (e.g. 1:59:30 - 2:00:29).
   */
  private boolean isCloseToTheHour(LocalTime localTime) {
    // 30 seconds before the hour (m=59, s=30-59).
    // 30 seconds after the hour (m=0, s=0-29).
    int minBeforeTheHour = 59;
    int minOnTheHour = 0;
    int currentMinute = localTime.getMinute();

    if (currentMinute == minBeforeTheHour) {
      return localTime.getSecond()
          >= 60 - attestationMeasurementConfigReader.getConfig().delaySeconds();
    } else if (currentMinute == minOnTheHour) {
      return localTime.getSecond() < attestationMeasurementConfigReader.getConfig().delaySeconds();
    }
    return false;
  }

  private FcAttestationClient(
      PccAttestationMeasurementClient attestationMeasurementClient,
      Executor executor,
      TimeSource timeSource,
      ConfigReader<PcsAttestationMeasurementConfig> attestationMeasurementConfigReader,
      Random randomGenerator) {
    this.attestationMeasurementClient = attestationMeasurementClient;
    this.executor = executor;
    this.timeSource = timeSource;
    this.attestationMeasurementConfigReader = attestationMeasurementConfigReader;
    this.randomGenerator = randomGenerator;
  }
}
