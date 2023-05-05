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

package com.google.android.`as`.oss.policies.impl

import android.content.Context
import arcs.core.data.proto.ManifestProto
import arcs.core.data.proto.PolicyProto
import com.google.android.`as`.oss.policies.api.PolicyMap
import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import dagger.hilt.android.qualifiers.ApplicationContext

/** Helper functions for loading policies from assets. */
object AssetLoader {

  /**
   * Loads a set of policies from the specified assets.
   *
   * @param context The app's context.
   * @param fileNames The set of file names of the assets containing the policies to load.
   * @return A Multimap from policy name to the policy's specification.
   */
  fun loadPolicyMapFromAssets(
    @ApplicationContext context: Context,
    vararg fileNames: String
  ): PolicyMap {
    val policyMap: Multimap<String, PolicyProto> = ArrayListMultimap.create()
    setOf(*fileNames).forEach {
      val policy = loadPolicyFromAsset(context, it)
      policyMap.put(policy.name, policy)
    }
    return policyMap
  }

  fun loadPolicyFromAsset(@ApplicationContext context: Context, fileName: String): PolicyProto {
    val manifest = ManifestProto.parseFrom(context.assets.open(fileName).use { it.readBytes() })
    check(manifest.policiesCount == 1) {
      "$fileName has ${manifest.policiesCount} policies, expected 1."
    }
    return manifest.policiesList.first()
  }
}
