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

package com.google.android.as.oss.fl.fc.service.scheduler;

import android.content.Context;
import com.google.fcp.client.FcInvocation;
import com.google.fcp.client.FcInvocationOptions;
import com.google.fcp.client.FcInvoker;
import com.google.fcp.client.internal.logger.PrivateLoggerExampleStoreAdapter;
import com.google.fcp.client.proto.ContributionResultInfo;
import com.google.fcp.client.proto.SchedulingHints;
import com.google.fcp.client.tasks.Task;
import com.google.fcp.client.tasks.Tasks;
import com.google.fcp.client.privatelogger.impl.DataProvider;
import java.util.concurrent.ExecutorService;
import javax.inject.Inject;

/** Implementation of {@link FcpLoggerInvoker} using FcInvoker. */
public class FcpLoggerInvokerImpl implements FcpLoggerInvoker {

  @Inject
  FcpLoggerInvokerImpl() {}

  @Override
  public Task<FcpInvocation> upload(
      Context context,
      ExecutorService executor,
      FcpInvocationOptions options,
      FcpInvocationCallback callback,
      DataProvider dataProvider) {
    FcInvocationOptions fcOptions =
        new FcInvocationOptions.Builder()
            .setSessionName(options.sessionName())
            .setFederatedOptions(options.populationName())
            .setAccessPolicyEndorsementOptions(options.accessPolicyEndorsementOptions())
            .build();
    FcInvoker.InvocationCallback fcCallback =
        new FcInvoker.InvocationCallback() {
          @Override
          public void onComputationCompleted(ContributionResultInfo resultInfo) {
            callback.onComputationCompleted(
                FcpContributionResultInfo.create(
                    resultInfo.getTaskName(),
                    resultInfo.getResultType()
                        == ContributionResultInfo.ContributionResultType.SUCCESS));
          }

          @Override
          public void onInvocationFinished(SchedulingHints schedulingHints) {
            callback.onInvocationFinished();
          }
        };
    PrivateLoggerExampleStoreAdapter exampleStoreAdapter =
        new PrivateLoggerExampleStoreAdapter(dataProvider);

    Task<FcInvocation> invocationTask =
        FcInvoker.upload(context, executor, fcOptions, fcCallback, exampleStoreAdapter);
    return invocationTask.onSuccessTask(
        executor,
        (fcInvocation) ->
            Tasks.forResult(
                new FcpInvocation() {
                  @Override
                  public Task<Void> cancel() {
                    return fcInvocation.cancel();
                  }
                }));
  }
}
