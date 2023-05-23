/*
 * Copyright 2023 Google LLC
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

package com.google.android.as.oss.networkusage.ui.content.impl;

import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_START_QUERY;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.HTTP;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.PD;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.ASI_PACKAGE_NAME;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.GPPS_PACKAGE_NAME;

import android.content.Context;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.android.as.oss.networkusage.ui.content.impl.NetworkUsageLogContentMapImpl.ConnectionResources;
import com.google.android.as.oss.networkusage.ui.user.R;
import com.google.android.as.oss.proto.PcsFeatureEnum.FeatureName;
import com.google.common.collect.ImmutableMap;
import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

/**
 * Module that provides map entries for {@link NetworkUsageLogContentMap}. A map entry describes an
 * expected download/upload request for PCS, and the corresponding description to show in the
 * network usage log. Requests that are not represented by one of the entries in the map will be
 * blocked by PCS.
 */
@Module
@InstallIn(SingletonComponent.class)
abstract class NetworkUsageLogContentModule {

  @Provides
  static ImmutableMap<ConnectionDetails, ConnectionResources> provideEntryContentMap(
      @ApplicationContext Context context ) {
    ContentMapEntryBuilder asiHttpEntryBuilder =
        new ContentMapEntryBuilder(context).packageName(ASI_PACKAGE_NAME).connectionType(HTTP);
    ContentMapEntryBuilder gppsHttpEntryBuilder =
        new ContentMapEntryBuilder(context).packageName(GPPS_PACKAGE_NAME).connectionType(HTTP);
    ContentMapEntryBuilder asiFcEntryBuilder =
        new ContentMapEntryBuilder(context)
            .packageName(ASI_PACKAGE_NAME)
            .connectionType(FC_TRAINING_START_QUERY);
    ContentMapEntryBuilder gppsFcEntryBuilder =
        new ContentMapEntryBuilder(context)
            .packageName(GPPS_PACKAGE_NAME)
            .connectionType(FC_TRAINING_START_QUERY);
    ContentMapEntryBuilder pdEntryBuilder = new ContentMapEntryBuilder(context).connectionType(PD);

    ImmutableMap<ConnectionDetails, ConnectionResources> entries =
        ImmutableMap.ofEntries(
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_speech_recognition)
                .featureNameId(R.string.feature_name_speech_recognition)
                .descriptionId(R.string.description_speech_recognition)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_suspicious_message_alert)
                .featureNameId(R.string.feature_name_suspicious_message_alert)
                .descriptionId(R.string.description_suspicious_message_alert_http)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_smart_copy_paste)
                .featureNameId(R.string.feature_name_smart_copy_paste)
                .descriptionId(R.string.description_smart_copy_paste_http)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_nowplaying)
                .featureNameId(R.string.feature_name_nowplaying)
                .descriptionId(R.string.description_nowplaying_http)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_actions)
                .featureNameId(R.string.feature_name_tc)
                .descriptionId(R.string.description_tc_actions)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_annotator)
                .featureNameId(R.string.feature_name_tc)
                .descriptionId(R.string.description_tc_annotator)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_langid)
                .featureNameId(R.string.feature_name_smart_text_selection)
                .descriptionId(R.string.description_tc_langid)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_template_table)
                .featureNameId(R.string.feature_name_smart_text_selection)
                .descriptionId(R.string.description_tc_template_table)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_dynamic_collections_table)
                .featureNameId(R.string.feature_name_tc)
                .descriptionId(R.string.description_tc_dynamic_collections_table)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_dynamic_action_ranking)
                .featureNameId(R.string.feature_name_smart_text_selection)
                .descriptionId(R.string.description_tc_dynamic_action_ranking)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_weekly_webref)
                .featureNameId(R.string.feature_name_tc)
                .descriptionId(R.string.description_tc_weekly_webref)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_live_caption)
                .featureNameId(R.string.feature_name_live_caption)
                .descriptionId(R.string.description_live_caption)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_live_translate)
                .featureNameId(R.string.feature_name_live_translate)
                .descriptionId(R.string.description_live_translate_http)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_people_conversations_service)
                .featureNameId(R.string.feature_name_people_conversations_service)
                .descriptionId(R.string.description_people_conversations_service_http)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_universal_search_spelling_checker)
                .featureNameId(R.string.feature_name_universal_search)
                .descriptionId(R.string.description_universal_search_spelling_checker)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_universal_search_corpora)
                .featureNameId(R.string.feature_name_universal_search)
                .descriptionId(R.string.description_universal_search_corpora)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_universal_search_corpora_mdd)
                .featureNameId(R.string.feature_name_universal_search)
                .descriptionId(R.string.description_universal_search_corpora)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_name_notification_auto_expiration)
                .featureNameId(R.string.feature_name_notification_auto_expiration)
                .descriptionId(R.string.description_notification_auto_expiration)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_app_suggestions_refraction)
                .featureNameId(R.string.feature_name_app_suggestions)
                .descriptionId(R.string.description_app_suggestions_refraction)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_tc_fa_fl_component)
                .featureNameId(R.string.feature_name_smart_text_selection)
                .descriptionId(R.string.description_tc_fa_fl_component)
                .build(),
            gppsHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_gpps_bt_signature)
                .featureNameId(R.string.feature_name_gpps)
                .descriptionId(R.string.description_bt_log_root_signature)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.SUSPICIOUS_MESSAGE_ALERT.name())
                .featureNameId(R.string.feature_name_suspicious_message_alert)
                .descriptionId(R.string.description_suspicious_message_alert_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.CONTENT_CAPTURE.name())
                .featureNameId(R.string.feature_name_content_capture_service)
                .descriptionId(R.string.description_content_capture_service_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.PECAN.name())
                .featureNameId(R.string.feature_name_people_conversations_service)
                .descriptionId(R.string.description_people_conversations_service_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.AUTOFILL.name())
                .featureNameId(R.string.feature_name_smart_copy_paste)
                .descriptionId(R.string.description_smart_copy_paste_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.NOW_PLAYING.name())
                .featureNameId(R.string.feature_name_nowplaying)
                .descriptionId(R.string.description_nowplaying_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.LIVE_TRANSLATE.name())
                .featureNameId(R.string.feature_name_live_translate)
                .descriptionId(R.string.description_live_translate_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.SMART_TEXT_SELECTION.name())
                .featureNameId(R.string.feature_name_smart_text_selection)
                .descriptionId(R.string.description_smart_text_selection_fa_fl)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.TOAST_SEARCH.name())
                .featureNameId(R.string.feature_name_toast_search)
                .descriptionId(R.string.description_toast_search)
                .build(),
            gppsFcEntryBuilder
                .connectionKeyString(FeatureName.GPPS_FEATURE_NAME.name())
                .featureNameId(R.string.feature_name_gpps)
                .descriptionId(R.string.description_gpps_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.AMBIENT_CONTEXT.name())
                .featureNameId(R.string.feature_name_ambient_context)
                .descriptionId(R.string.description_ambient_context)
                .build(),
            pdEntryBuilder
                .connectionKeyString(context.getString(R.string.ap_client_id_gpps))
                .featureNameId(R.string.feature_name_gpps)
                .descriptionId(R.string.description_gpps_ap)
                .packageName(GPPS_PACKAGE_NAME)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_quick_tap)
                .featureNameId(R.string.feature_name_quick_tap)
                .descriptionId(R.string.description_quick_tap)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_speech_recognizer_personalization)
                .featureNameId(R.string.feature_name_speech_recognizer_personalization)
                .descriptionId(R.string.description_speech_recognizer_personalization)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_cinematic_wallpaper)
                .featureNameId(R.string.feature_name_cinematic_wallpaper)
                .descriptionId(R.string.description_cinematic_wallpaper)
                .build(),
            );
    ImmutableMap<ConnectionDetails, ConnectionResources> finalImmutableMap =
        entries;

    if (!verifyUniqueKeysAcrossPackages(finalImmutableMap)) {
      throw new UnsupportedOperationException("Connection key should be unique across packages.");
    }

    return finalImmutableMap;
  }

  /**
   * Helper method to ensure that the keys in the entryContentMap are unique to avoid providing
   * incorrect ConnectionDetails.
   */
  static boolean verifyUniqueKeysAcrossPackages(
      ImmutableMap<ConnectionDetails, ConnectionResources> entryContentMap) {
    HashMap<String, String> connectionKeyToPackage = new HashMap<>();
    for (ConnectionDetails details : entryContentMap.keySet()) {
      final String connectionKey;
      switch (details.type()) {
        case HTTP:
          connectionKey = details.connectionKey().getHttpConnectionKey().getUrlRegex();
          break;
        case PIR:
          connectionKey = details.connectionKey().getPirConnectionKey().getUrlRegex();
          break;
        case FC_CHECK_IN:
        case FC_TRAINING_START_QUERY:
        case FC_TRAINING_RESULT_UPLOAD:
          connectionKey = details.connectionKey().getFlConnectionKey().getFeatureName();
          break;
        case PD:
          connectionKey = details.connectionKey().getPdConnectionKey().getClientId();
          break;
        default:
          // Should never be reached.
          throw new UnsupportedOperationException(
              String.format("Unsupported connection type '%s'", details.type().name()));
      }
      if (connectionKeyToPackage.containsKey(connectionKey)) {
        if (!Objects.equals(connectionKeyToPackage.get(connectionKey), details.packageName())) {
          return false;
        }
      }
      connectionKeyToPackage.put(connectionKey, details.packageName());
    }
    return true;
  }

  @Binds
  abstract NetworkUsageLogContentMap bindNetworkUsageLogContentMap(
      NetworkUsageLogContentMapImpl impl);
}
