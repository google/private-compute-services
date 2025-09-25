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

package com.google.android.`as`.oss.delegatedui.service.templates.sundog

import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateType
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateKey
import com.google.android.`as`.oss.delegatedui.service.templates.TemplateRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
interface SundogTemplateHandlerModule {

  @Binds
  @IntoMap
  @TemplateKey(DelegatedUiTemplateType.TEMPLATE_TYPE_SUNDOG_LOCATION)
  fun provideSundogLocationTemplateHandler(impl: SundogLocationTemplateRenderer): TemplateRenderer

  @Binds
  @IntoMap
  @TemplateKey(DelegatedUiTemplateType.TEMPLATE_TYPE_SUNDOG_EVENT)
  fun provideSundogEventTemplateHandler(impl: SundogEventTemplateRenderer): TemplateRenderer
}
