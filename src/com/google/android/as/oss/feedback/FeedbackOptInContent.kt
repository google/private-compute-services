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

package com.google.android.`as`.oss.feedback

import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp

@Composable
fun FeedbackOptInContent(
  modifier: Modifier,
  optInLabel: String,
  optInLabelLinkPrivacyPolicy: String,
  optInLabelLinkViewData: String,
  optInChecked: Boolean,
  onOptInCheckedChanged: (Boolean) -> Unit,
  onViewDataClicked: () -> Unit,
) {
  ParagraphTheme {
    Row(modifier = modifier) {
      Checkbox(
        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 16.dp).size(24.dp),
        checked = optInChecked,
        onCheckedChange = onOptInCheckedChanged,
      )
      Spacer(modifier = Modifier.width(12.dp))

      val optInText = buildAnnotatedString {
        append(optInLabel)

        // Privacy policy.
        val policyUrl = "https://policies.google.com/privacy"
        val privacyPolicyStart = optInLabel.indexOf(optInLabelLinkPrivacyPolicy)
        val privacyPolicyEnd = privacyPolicyStart + optInLabelLinkPrivacyPolicy.length
        if (privacyPolicyStart != -1) {
          addLink(
            url = LinkAnnotation.Url(policyUrl),
            start = privacyPolicyStart,
            end = privacyPolicyEnd,
          )
        }

        // View data.
        val viewDataStart = optInLabel.indexOf(optInLabelLinkViewData)
        val viewDataEnd = viewDataStart + optInLabelLinkViewData.length
        if (viewDataStart != -1) {
          addLink(
            clickable =
              LinkAnnotation.Clickable(
                tag = OPT_IN_LABEL_LINK_VIEW_DATA_TAG,
                linkInteractionListener = { onViewDataClicked() },
              ),
            start = viewDataStart,
            end = viewDataEnd,
          )
          addStyle(
            style = SpanStyle(fontWeight = FontWeight.Bold),
            start = viewDataStart,
            end = viewDataEnd,
          )
        }
      }

      Column {
        Text(
          modifier =
            Modifier.fillMaxWidth().semantics(
              // Required for screen readers because `optInText` is a complex [AnnotatedString]
              // which forces the Text composable to be pushed to the back of the traversal order.
              mergeDescendants = true
            ) {},
          text = optInText,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!optInLabel.contains(optInLabelLinkViewData)) { // For backwards compat.
          Spacer(Modifier.height(8.dp))
          Text(
            modifier = Modifier.clickable { onViewDataClicked() },
            text = optInLabelLinkViewData,
            style =
              MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline,
              ),
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}

/** [MaterialTheme] without flex font. */
@Composable
private fun ParagraphTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkColorScheme()
      else -> lightColorScheme()
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography()) { content() }
}

private const val OPT_IN_LABEL_LINK_VIEW_DATA_TAG = "opt_in_label_link_view_data"
