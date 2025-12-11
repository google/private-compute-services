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

package com.google.android.`as`.oss.delegatedui.service.common

import com.google.android.`as`.oss.delegatedui.api.common.DelegatedUiDataProviderInfo.DelegatedUiDataProvider
import com.google.android.`as`.oss.delegatedui.api.integration.templates.DelegatedUiTemplateType

object DelegatedUiExceptions {

  /** Represents the DUI failure case when the TemplateRenderer cannot be found. */
  class InvalidTemplateRendererError(val templateType: DelegatedUiTemplateType) :
    Exception("No template renderer found for template type ${templateType.name}.")

  /** Represents the DUI failure case when the TemplateRenderer rendered a null view. */
  object NullTemplateRenderedError :
    Exception(
      "TemplateRenderer rendered a null view. Check the renderer logic or the template data."
    ) {

    private fun readResolve(): Any = NullTemplateRenderedError
  }

  /** Represents the DUI failure case when the DataProvider service cannot be found. */
  class InvalidDataProviderServiceError(val dataProvider: DelegatedUiDataProvider) :
    Exception("No service found for data provider $dataProvider")
}
