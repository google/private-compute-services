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

package com.android.personalcontext.ace.client.prototype.dryrun

import android.os.Bundle
import androidx.annotation.VisibleForTesting
import com.android.personalcontext.ace.client.prototype.PrototypeHint
import com.android.personalcontext.ace.client.prototype.PrototypeHint.Creator
import com.android.personalcontext.ace.client.prototype.PrototypeHintId.DryRunHintId

/**
 * A [PrototypeHint] signaling whether an evaluation is a background dry-run probe versus an active
 * visual render.
 *
 * @property isDryRun Indicates whether this evaluation is a dry-run probe (`true`) or an active
 *   visual render (`false`).
 */
data class DryRunHint(val isDryRun: Boolean) : PrototypeHint(DryRunHintId, this) {

  override fun exportDataToBundle(bundle: Bundle) {
    bundle.putBoolean(KEY_IS_DRY_RUN, isDryRun)
  }

  companion object : Creator {
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    const val KEY_IS_DRY_RUN = "DryRunHint__is_dry_run"

    override fun create(bundle: Bundle): PrototypeHint {
      return DryRunHint(isDryRun = bundle.getBoolean(KEY_IS_DRY_RUN, false))
    }
  }
}
