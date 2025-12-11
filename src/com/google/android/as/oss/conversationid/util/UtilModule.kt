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

package com.google.android.`as`.oss.conversationid.util

import android.os.Build.VERSION_CODES
import androidx.annotation.RequiresApi
import com.google.android.`as`.oss.common.config.ConfigReader
import com.google.android.`as`.oss.common.security.config.PccSecurityConfig
import com.google.android.`as`.oss.conversationid.config.ConversationIdConfig
import com.google.common.time.TimeSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/** Module for ConversationIdManager. */
@Module
@InstallIn(SingletonComponent::class)
internal object UtilModule {

  @Provides
  @Singleton
  fun provideConversationIdHolder(timeSource: TimeSource): ConversationIdHolder =
    ConversationIdHolder(timeSource)

  @Provides
  @Singleton
  fun provideServiceValidator(
    configReader: ConfigReader<ConversationIdConfig>,
    securityPolicyConfigReader: ConfigReader<PccSecurityConfig>,
  ): ServiceValidator = ServiceValidator(configReader, securityPolicyConfigReader)

  @Provides
  @Singleton
  @RequiresApi(VERSION_CODES.BAKLAVA)
  fun provideConversationIdManager(
    conversationIdHolder: ConversationIdHolder
  ): ConversationIdManager = ConversationIdManager(conversationIdHolder)
}
