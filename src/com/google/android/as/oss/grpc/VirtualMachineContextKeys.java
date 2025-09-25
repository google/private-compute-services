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

package com.google.android.apps.miphone.pcs.grpc;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.system.virtualmachine.VirtualMachineDescriptor;
import io.grpc.Context;
import io.grpc.Metadata;
import io.grpc.binder.ParcelableUtils;
import java.util.concurrent.atomic.AtomicReference;

/** Utility class with keys for accessing VirtualMachineDescriptors passed through grpc metadata. */
public class VirtualMachineContextKeys {

  // Only reference VirtualMachineDescriptor.CREATOR after an SDK version check to avoid a class
  // initialization failure.
  public static final Metadata.Key<VirtualMachineDescriptor> VM_DESCRIPTOR_METADATA_KEY =
      ParcelableUtils.metadataKey(
          "vm-descriptor-bin",
          Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE
              ? VirtualMachineDescriptor.CREATOR
              : new DummyCreator<VirtualMachineDescriptor>());

  public static final Context.Key<AtomicReference<VirtualMachineDescriptor>>
      VM_DESCRIPTOR_CONTEXT_KEY = Context.key("vm-descriptor-bin");

  private VirtualMachineContextKeys() {}

  private static class DummyCreator<T> implements Parcelable.Creator<T> {
    @Override
    public T createFromParcel(Parcel source) {
      throw new UnsupportedOperationException();
    }

    @Override
    public T[] newArray(int size) {
      throw new UnsupportedOperationException();
    }
  }
}
