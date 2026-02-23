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

package com.google.android.`as`.oss.privateinference.library.bsa.token.cache

import com.google.android.`as`.oss.privateinference.library.bsa.token.ArateaTokenWithoutChallenge
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider.DiskCached
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider.MemoryCached
import com.google.android.`as`.oss.privateinference.library.bsa.token.BsaTokenProvider.MultilevelCached
import com.google.android.`as`.oss.privateinference.library.bsa.token.ProxyToken
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
internal interface OptionalCachingTokenProviderModule {
  @BindsOptionalOf
  @MemoryCached
  fun bindProxyTokenMemoryCacheProvider(): BsaTokenProvider<ProxyToken>

  @BindsOptionalOf
  @MemoryCached
  fun bindArateaTokenMemoryCacheProvider(): BsaTokenProvider<ArateaTokenWithoutChallenge>

  @BindsOptionalOf @DiskCached fun bindProxyTokenDiskCacheProvider(): BsaTokenProvider<ProxyToken>

  @BindsOptionalOf
  @DiskCached
  fun bindArateaTokenDiskCacheProvider(): BsaTokenProvider<ArateaTokenWithoutChallenge>

  @BindsOptionalOf
  @MultilevelCached
  fun bindProxyTokenMultilevelCacheProvider(): BsaTokenProvider<ProxyToken>

  @BindsOptionalOf
  @MultilevelCached
  fun bindArateaTokenMultilevelCacheProvider(): BsaTokenProvider<ArateaTokenWithoutChallenge>

  @Multibinds
  @ProxyToken.Qualifier
  fun bindProxyTokenCacheControlPlanes(): Set<BsaTokenCacheControlPlane>

  @Multibinds
  @ArateaTokenWithoutChallenge.Qualifier
  fun bindArateaTokenCacheControlPlanes(): Set<BsaTokenCacheControlPlane>
}
