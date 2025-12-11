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

package com.google.android.as.oss.fl.fc.service.scheduler.endorsementoptions;

import android.content.Context;
import com.google.intelligence.fcp.confidentialcompute.AccessPolicyEndorsementOptions;
import com.google.protobuf.contrib.android.ProtoParsers;
import java.util.Map;

/** Determines the endorsement key to be used for the apk. */
public final class EndorsementOptionsProviderImpl implements EndorsementOptionsProvider {
  private final Map<EndorsementClientType, Integer> resourceIdMap;

  public EndorsementOptionsProviderImpl(Map<EndorsementClientType, Integer> resourceIdMap) {
    this.resourceIdMap = resourceIdMap;
  }

  @Override
  public AccessPolicyEndorsementOptions getEndorsementOptions(
      Context context, EndorsementClientType clientType) {
    Integer resourceId = resourceIdMap.get(clientType);
    if (resourceId == null) {
      throw new IllegalArgumentException("No resource ID found for client type: " + clientType);
    }
    return ProtoParsers.parseFromRawRes(
        context, AccessPolicyEndorsementOptions.parser(), resourceId);
  }
}
