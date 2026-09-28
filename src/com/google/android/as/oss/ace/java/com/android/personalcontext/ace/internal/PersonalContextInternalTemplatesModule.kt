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

package com.android.personalcontext.ace.internal

import com.android.personalcontext.ace.internal.templates.gboard.GboardVisualizerTemplate
import com.android.personalcontext.ace.internal.templates.richcard.RichCardVisualizerTemplate
import com.android.personalcontext.ace.internal.templates.weather.WeatherVisualizerTemplate
import com.android.personalcontext.ace.visualizer.templates.VisualizerTemplate
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoSet

/** Dagger for templates that are ready for prod. */
@Module
interface PersonalContextInternalTemplatesModule {
  companion object {

    @Provides
    @IntoSet
    fun provideWeatherVisualizerTemplate(
      impl: Lazy<WeatherVisualizerTemplate>
    ): VisualizerTemplate {
      return impl.get()
    }

    @Provides
    @IntoSet
    fun provideRichCardVisualizerTemplate(
      impl: Lazy<RichCardVisualizerTemplate>
    ): VisualizerTemplate {
      return impl.get()
    }

    @Provides
    @IntoSet
    fun provideGboardVisualizerTemplate(impl: Lazy<GboardVisualizerTemplate>): VisualizerTemplate {
      return impl.get()
    }
  }
}
