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

package com.google.android.`as`.oss.privateinference.library.bsa.token.auth

import com.google.android.`as`.oss.common.CoroutineQualifiers
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.logging.PcsStatsEnums.CountMetricId
import com.google.android.`as`.oss.privateinference.config.PrivateInferenceConfig
import com.google.android.`as`.oss.privateinference.library.bsa.BlindSignAuth
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaAnnotations.ProxyTokenErrorLogMapper
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.grpc.Status.Code
import io.grpc.StatusException
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
internal object AuthenticatingProxyTokenProviderModule {
  @Provides
  @Singleton
  @BsaTokenProvider.Authenticating
  fun provideProxyTokenProvider(
    @CoroutineQualifiers.ApplicationScope coroutineScope: CoroutineScope,
    blindSignAuth: BlindSignAuth,
    configReader: ConfigReader<@JvmSuppressWildcards PrivateInferenceConfig>,
  ): BsaTokenProvider<ProxyToken> =
    AuthenticatingProxyTokenProvider(
      coroutineScope = coroutineScope,
      blindSignAuth = blindSignAuth,
      configReader = configReader,
    )

  @Provides
  @Singleton
  @ProxyTokenErrorLogMapper
  fun provideProxyTokenFetchErrorLogMapper(): (Throwable) -> CountMetricId? = {
    when (it) {
      is StatusException ->
        when (it.status.code) {
          Code.PERMISSION_DENIED -> CountMetricId.PCS_PI_IPP_GET_PROXY_TOKEN_ERROR_BAD_ATTESTATION
          Code.RESOURCE_EXHAUSTED -> CountMetricId.PCS_PI_IPP_GET_PROXY_TOKEN_ERROR_RATE_LIMITED
          else -> null
        }
      else -> null
    }
  }
}
