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

import static com.google.android.apps.miphone.astrea.grpc.VirtualMachineContextKeys.VM_DESCRIPTOR_CONTEXT_KEY;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.system.virtualmachine.VirtualMachine;
import android.system.virtualmachine.VirtualMachineCallback;
import android.system.virtualmachine.VirtualMachineConfig;
import android.system.virtualmachine.VirtualMachineDescriptor;
import android.system.virtualmachine.VirtualMachineException;
import android.system.virtualmachine.VirtualMachineManager;
import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import com.google.android.as.oss.pd.api.proto.DeleteVmRequest;
import com.google.android.as.oss.pd.api.proto.DeleteVmResponse;
import com.google.android.as.oss.pd.api.proto.GetVmRequest;
import com.google.android.as.oss.pd.api.proto.GetVmResponse;
import com.google.android.as.oss.pd.persistence.ClientPersistentState;
import com.google.android.as.oss.pd.persistence.PersistentStateManager;
import com.google.android.as.oss.pd.virtualmachine.VirtualMachineRunner;
import com.google.android.pd.ISecureService;
import com.google.common.flogger.GoogleLogger;
import com.google.common.util.concurrent.FluentFuture;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.ByteString;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/** A {@link VirtualMachineRunner} that interacts with virtual machines. */
@TargetApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
public class VirtualMachineRunnerImpl implements VirtualMachineRunner {
  private final Context context;
  private final Executor executor;
  private final PersistentStateManager persistenceManager;

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final ClientPersistentState DEFAULT_PERSISTENT_STATE =
      ClientPersistentState.newBuilder()
          .setExternalKeySet(ByteString.empty())
          .setPageToken(ByteString.empty())
          .build();

  private static final String VM_NAME = "pd_vm";

  public VirtualMachineRunnerImpl(
      PersistentStateManager persistenceManager, Executor executor, Context context) {
    this.executor = executor;
    this.context = context;
    this.persistenceManager = persistenceManager;
  }

  @Override
  public ListenableFuture<GetVmResponse> provisionVirtualMachine(GetVmRequest request) {
    AtomicReference<VirtualMachineDescriptor> descriptorRef = VM_DESCRIPTOR_CONTEXT_KEY.get();
    return FluentFuture.from(getVirtualMachineDescriptor(request))
        .transform(
            descriptor -> {
              descriptorRef.set(descriptor);
              return GetVmResponse.getDefaultInstance();
            },
            executor);
  }

  @Override
  public ListenableFuture<DeleteVmResponse> deleteVirtualMachine(DeleteVmRequest request) {
    VirtualMachineManager vmManager = context.getSystemService(VirtualMachineManager.class);
    if (vmManager == null) {
      throw new UnsupportedOperationException("VMs are unsupported on this device.");
    }

    return Futures.submit(
        () -> {
          try {
            vmManager.delete(VM_NAME);
            return DeleteVmResponse.getDefaultInstance();
          } catch (VirtualMachineException e) {
            throw new VmException("Failed to delete VM.", e);
          }
        },
        this.executor);
  }

  private ListenableFuture<VirtualMachineDescriptor> getVirtualMachineDescriptor(
      GetVmRequest request) {
    VirtualMachine virtualMachine;
    try {
      virtualMachine = getOrCreateVm(request);
    } catch (VmException e) {
      return Futures.<VirtualMachineDescriptor>immediateFailedFuture(e);
    }

    return FluentFuture.from(runVm(virtualMachine))
        .transformAsync(unused -> readOrCreatePersistentState(), executor)
        .transformAsync(
            previousState -> {
              byte[] publicKey = runTartarusService(virtualMachine).getSerializedPublicKey();
              ClientPersistentState newState =
                  previousState.toBuilder()
                      .setExternalKeySet(ByteString.copyFrom(publicKey))
                      .build();
              return persistenceManager.writeState(PersistentStateManager.VM_CLIENT_ID, newState);
            },
            executor)
        .<Void>transform(
            unused -> {
              virtualMachine.close();
              return null;
            },
            executor)
        .transform(
            unused -> {
              try {
                return virtualMachine.toDescriptor();
              } catch (VirtualMachineException e) {
                throw new VmException("Error getting descriptor from VM", e);
              }
            },
            executor);
  }

  @NonNull
  private ListenableFuture<Void> runVm(VirtualMachine virtualMachine) {
    return CallbackToFutureAdapter.getFuture(
        completer -> {
          try {
            virtualMachine.setCallback(
                executor,
                new VirtualMachineCallback() {
                  @Override
                  public void onPayloadStarted(VirtualMachine vm) {
                    logger.atFine().log("onPayloadStarted received from VM.");
                  }

                  @Override
                  public void onPayloadReady(VirtualMachine vm) {
                    logger.atFine().log("onPayloadReady received from VM.");
                    vm.clearCallback();
                    completer.set(null);
                  }

                  @Override
                  public void onPayloadFinished(VirtualMachine vm, int exitCode) {
                    vm.clearCallback();
                    completer.setException(
                        new VmException(
                            "onPayloadFinished received from VM with exitCode: " + exitCode));
                  }

                  @Override
                  public void onError(VirtualMachine vm, int errorCode, String message) {
                    vm.clearCallback();
                    completer.setException(
                        new VmException(
                            "onError received from VM with errorCode: "
                                + errorCode
                                + " and message "
                                + message));
                  }

                  @Override
                  public void onStopped(VirtualMachine vm, int reason) {
                    vm.clearCallback();
                    completer.setException(
                        new VmException("onStopped received from VM with reason: " + reason));
                  }
                });
            virtualMachine.run();
          } catch (VirtualMachineException e) {
            completer.setException(new VmException("Failure while running VM.", e));
          }
          return "payloadReady future";
        });
  }

  private VirtualMachine getOrCreateVm(GetVmRequest request) {
    VirtualMachineManager vmManager = context.getSystemService(VirtualMachineManager.class);
    if (vmManager == null) {
      throw new UnsupportedOperationException("VMs are unsupported on this device.");
    }

    // Check that protected VMs are supported. Devices that support AVF are not required to
    // support protected VMs (although they're strongly encouraged to).
    int capabilities = vmManager.getCapabilities();
    if ((capabilities & VirtualMachineManager.CAPABILITY_PROTECTED_VM) == 0) {
      throw new UnsupportedOperationException("Protected VMs are unsupported on this device.");
    }

    VirtualMachineConfig config =
        buildVmConfig(context, request.getApkPath(), request.getPayloadPath());
    try {
      VirtualMachine virtualMachine = vmManager.getOrCreate(VM_NAME, config);
      try {
        // Update the config in case the VM already exists with a different config.
        virtualMachine.setConfig(config);
        return virtualMachine;
      } catch (VirtualMachineException e) {
        logger.atWarning().withCause(e).log(
            "Error while updating VM config. Deleting and restarting VM.");
        vmManager.delete(VM_NAME);
        return vmManager.create(VM_NAME, config);
      }
    } catch (VirtualMachineException e) {
      throw new VmException("Failed to start or update VM.", e);
    }
  }

  private static VirtualMachineConfig buildVmConfig(
      Context context, String apkPath, String payloadPath) {
    // TODO Support split APKs.
    // See [redacted] to enable debug logs.
    // TODO: Use a locally overridable flag to permit local VM debugging.
    return new VirtualMachineConfig.Builder(context)
        .setProtectedVm(true)
        .setApkPath(apkPath)
        .setPayloadBinaryName(payloadPath)
        .build();
  }

  private static ISecureService runTartarusService(VirtualMachine vm)
      throws VirtualMachineException {
    return ISecureService.Stub.asInterface(vm.connectToVsockServer(ISecureService.SERVICE_PORT));
  }

  private ListenableFuture<ClientPersistentState> readOrCreatePersistentState() {
    return Futures.transform(
        persistenceManager.readState(PersistentStateManager.VM_CLIENT_ID),
        optionalState -> {
          if (optionalState.isPresent()) {
            logger.atInfo().log(
                "found persistent state for client %s", PersistentStateManager.VM_CLIENT_ID);
          } else {
            logger.atInfo().log(
                "creating new persistent state for client %s", PersistentStateManager.VM_CLIENT_ID);
          }
          return optionalState.orElse(DEFAULT_PERSISTENT_STATE);
        },
        executor);
  }

  /** Thrown if the VirtualMachine fails to start or stop properly. */
  static class VmException extends RuntimeException {
    public VmException(String message, Throwable cause) {
      super(message, cause);
    }

    public VmException(String message) {
      super(message);
    }
  }
}
