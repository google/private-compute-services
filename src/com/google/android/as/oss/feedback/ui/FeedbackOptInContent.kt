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

package com.google.android.`as`.oss.feedback.ui

import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.`as`.oss.delegatedui.service.templates.fonts.FlexFontUtils.withFlexFont

@Composable
fun FeedbackOptInControl(
  modifier: Modifier,
  optInCheckboxContentDescription: String,
  optInChecked: Boolean,
  onOptInCheckedChanged: (Boolean) -> Unit,
  viewDataTitle: String,
  viewDataDescription: String,
  onViewDataClicked: () -> Unit,
) {
  MainTheme(flexFont = true) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Checkbox(
        modifier =
          Modifier.semantics {
            contentDescription =
              optInCheckboxContentDescription.ifEmpty { viewDataTitle } // For backwards compat.
          },
        checked = optInChecked,
        onCheckedChange = onOptInCheckedChanged,
      )

      Row(
        modifier = modifier.clickable { onViewDataClicked() }.padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = viewDataTitle.ifEmpty { "Include data collected" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
          Text(
            text = viewDataDescription.ifEmpty { "Review data included with your feedback" },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }

        Icon(
          modifier = Modifier.padding(16.dp),
          painter = painterResource(R.drawable.keyboard_arrow_right_24),
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          contentDescription = null,
        )
      }
    }
  }
}

@Composable
fun FeedbackOptInPrivacyStatement(
  modifier: Modifier,
  optInLabel: String,
  optInLabelLinkPrivacyPolicy: String,
  optInLabelLinkViewData: String,
  onViewDataClicked: () -> Unit,
) {
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

  MainTheme(flexFont = false) {
    Text(
      modifier =
        modifier.fillMaxWidth().semantics(
          // Required for screen readers because `optInText` is a complex [AnnotatedString]
          // which forces the Text composable to be pushed to the back of the traversal order.
          mergeDescendants = true
        ) {},
      text = optInText,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}

/** [MaterialTheme] without flex font. */
@Composable
private fun MainTheme(
  flexFont: Boolean,
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      dynamicColor && VERSION.SDK_INT >= VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> darkColorScheme()
      else -> lightColorScheme()
    }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography().let { if (flexFont) it.withFlexFont() else it },
  ) {
    content()
  }
}

private const val OPT_IN_LABEL_LINK_VIEW_DATA_TAG = "opt_in_label_link_view_data"
