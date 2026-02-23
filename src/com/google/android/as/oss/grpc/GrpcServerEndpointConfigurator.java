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

import android.content.Context;
import android.os.IBinder;
import io.grpc.Server;
import io.grpc.binder.IBinderReceiver;
import java.io.IOException;

/**
 * Responsible for implementing the logic to configure and create an {@link IBinder} for serving
 * gRPC connections over Binder.
 */
public interface GrpcServerEndpointConfigurator {
  /**
   * Constructs and starts an on-device gRPC server, then returns an {@link IBinder} that can be
   * returned from the host service's {@link android.app.Service#onBind(Intent)}.
   *
   * @param context the hosting service context.
   * @param cls the hosting service class.
   * @param configuration details on how to configure the on-device server.
   * @param iBinderReceiver the {@link IBinderReceiver} that will be used to receive the {@link
   *     IBinder} of the created server.
   * @return the created {@link Server} instance.
   */
  Server buildOnDeviceServerEndpoint(
      Context context,
      Class<?> cls,
      GrpcServerEndpointConfiguration configuration,
      IBinderReceiver iBinderReceiver)
      throws IOException;
}
