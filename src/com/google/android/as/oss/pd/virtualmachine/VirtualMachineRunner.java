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

package com.google.android.as.oss.pd.virtualmachine;

import com.google.android.as.oss.pd.api.proto.DeleteVmRequest;
import com.google.android.as.oss.pd.api.proto.DeleteVmResponse;
import com.google.android.as.oss.pd.api.proto.GetVmRequest;
import com.google.android.as.oss.pd.api.proto.GetVmResponse;
import com.google.common.util.concurrent.ListenableFuture;

/** An abstraction for interacting with a virtual machine */
public interface VirtualMachineRunner {
  /** Create a new virtual machine. Save the VM public key and return the VM's descriptor . */
  ListenableFuture<GetVmResponse> provisionVirtualMachine(GetVmRequest request);

  /** Delete an existing virtual machine. */
  ListenableFuture<DeleteVmResponse> deleteVirtualMachine(DeleteVmRequest request);
}
