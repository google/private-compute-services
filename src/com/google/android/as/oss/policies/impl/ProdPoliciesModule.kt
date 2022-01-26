/*
 * Copyright 2021 Google LLC
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
import com.google.android.`as`.oss.policies.api.PolicyMap
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("EXPERIMENTAL_API_USAGE")
@Module
@InstallIn(SingletonComponent::class)
object ProdPoliciesModule {
  @Provides
  @Singleton
  @JvmStatic
  fun providePolicies(@ApplicationContext context: Context): PolicyMap {
    return AssetLoader.loadPolicyMapFromAssets(
      context,
      "AutofillPolicy_FederatedCompute_ASI_PROD.binarypb",
      "SafecommsPolicy_FederatedCompute_ASI_PROD.binarypb",
      "LiveTranslatePolicy_FederatedCompute_ASI_PROD.binarypb",
      "SmartSelectPolicy_FederatedCompute_Prod.binarypb",
      "ContentCapturePerformanceDataPolicy_FederatedCompute_ASI_PROD.binarypb",
      "GPPServicePolicy_FederatedCompute_Prod.binarypb",
      "NowPlayingUsagePolicy_FederatedCompute_ASI_PROD.binarypb",
      "PecanUsageEventPolicy_FederatedCompute_ASI_PROD.binarypb",
      "PecanScrollingEventPolicy_FederatedCompute_ASI_PROD.binarypb",
      "PecanLatencyAnalyticsEventPolicy_FederatedCompute_ASI_PROD.binarypb",
    )
  }
}
