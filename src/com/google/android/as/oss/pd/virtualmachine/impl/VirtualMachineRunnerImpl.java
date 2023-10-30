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

package com.google.android.as.oss.pd.virtualmachine.impl;

import com.google.android.as.oss.pd.api.proto.GetVmRequest;
import com.google.android.as.oss.pd.api.proto.GetVmResponse;
import com.google.android.as.oss.pd.virtualmachine.VirtualMachineRunner;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.concurrent.Executor;

/** A {@link VirtualMachineRunner} that interacts with virtual machines. */
public class VirtualMachineRunnerImpl implements VirtualMachineRunner {
  private final Executor executor;

  public VirtualMachineRunnerImpl(Executor executor) {
    this.executor = executor;
  }

  @Override
  public ListenableFuture<GetVmResponse> provisionVirtualMachine(GetVmRequest request) {
    // Virtual Machine handling not yet implemented.
    return Futures.immediateFuture(GetVmResponse.getDefaultInstance());
  }
}
