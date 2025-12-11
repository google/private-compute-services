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

package com.google.android.as.oss.networkusage.ui.content.impl;

import static com.google.android.as.oss.attestation.PccAttestationMeasurementClient.ATTESTATION_FEATURE_NAME;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.ATTESTATION_REQUEST;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_START_QUERY;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FEEDBACK_REQUEST;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.HTTP;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.PD;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.SURVEY_REQUEST;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.AICORE_PACKAGE_NAME;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.ASI_PACKAGE_NAME;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.GPPS_PACKAGE_NAME;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.LAUNCHER_PACKAGE_NAME;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.PCS_PACKAGE_NAME;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.PSI_PACKAGE_NAME;
import static com.google.android.as.oss.networkusage.ui.content.impl.ContentMapEntryBuilder.STATSD_PACKAGE_NAME;

import android.content.Context;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.android.as.oss.networkusage.ui.content.impl.NetworkUsageLogContentMapImpl.ConnectionResources;
import com.google.android.as.oss.networkusage.ui.user.R;
import com.google.android.as.oss.privateinference.networkusage.PrivateInferenceNetworkUsageLogHelper.IPProtectionRequestType;
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
    ContentMapEntryBuilder aicoreHttpEntryBuilder =
        new ContentMapEntryBuilder(context).packageName(AICORE_PACKAGE_NAME).connectionType(HTTP);
    ContentMapEntryBuilder launcherHttpEntryBuilder =
        new ContentMapEntryBuilder(context).packageName(LAUNCHER_PACKAGE_NAME).connectionType(HTTP);
    ContentMapEntryBuilder asiFcEntryBuilder =
        new ContentMapEntryBuilder(context)
            .packageName(ASI_PACKAGE_NAME)
            .connectionType(FC_TRAINING_START_QUERY);
    ContentMapEntryBuilder gppsFcEntryBuilder =
        new ContentMapEntryBuilder(context)
            .packageName(GPPS_PACKAGE_NAME)
            .connectionType(FC_TRAINING_START_QUERY);
    ContentMapEntryBuilder pdEntryBuilder = new ContentMapEntryBuilder(context).connectionType(PD);
    ContentMapEntryBuilder surveyEntryBuilder =
        new ContentMapEntryBuilder(context)
            .packageName(ASI_PACKAGE_NAME)
            .connectionType(SURVEY_REQUEST);
    ContentMapEntryBuilder attestationEntryBuilder =
        new ContentMapEntryBuilder(context)
            .packageName(ASI_PACKAGE_NAME)
            .connectionType(ATTESTATION_REQUEST);
    ContentMapEntryBuilder psiHttpEntryBuilder =
        new ContentMapEntryBuilder(context).packageName(PSI_PACKAGE_NAME).connectionType(HTTP);

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
                .connectionKeyStringId(R.string.url_regex_nowplaying_albumart_ondevice)
                .featureNameId(R.string.feature_name_nowplaying)
                .descriptionId(R.string.description_nowplaying_albumart)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_nowplaying_albumart_ondemand)
                .featureNameId(R.string.feature_name_nowplaying)
                .descriptionId(R.string.description_nowplaying_albumart)
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
                .connectionKeyStringId(R.string.url_regex_universal_search_corpora_mdd_edgedl)
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
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.NOTIFICATION_INTELLIGENCE.name())
                .featureNameId(R.string.feature_name_notification_intelligence)
                .descriptionId(R.string.description_notification_intelligence_fa)
                .build(),
            asiFcEntryBuilder
                .connectionKeyString(FeatureName.PWM.name())
                .featureNameId(R.string.feature_name_pwm)
                .descriptionId(R.string.description_pwm_fa)
                .build(),
            pdEntryBuilder
                .connectionKeyString(context.getString(R.string.ap_client_id_gpps))
                .featureNameId(R.string.feature_name_gpps)
                .descriptionId(R.string.description_gpps_ap)
                .packageName(GPPS_PACKAGE_NAME)
                .build(),
            piEntryBuilder
                .packageName(AICORE_PACKAGE_NAME)
                .connectionKeyString(
                    PcsPrivateInferenceFeatureName.FEATURE_NAME_RECORDER_TRANSCRIPT_SUMMARIZATION
                        .name())
                .featureNameId(R.string.feature_name_pi_transcript_summarization)
                .descriptionId(R.string.description_pi_transcript_summarization)
                .build(),
            piEntryBuilder
                .packageName(PSI_PACKAGE_NAME)
                .connectionKeyString(
                    PcsPrivateInferenceFeatureName.FEATURE_NAME_PSI_MEMORY_GENERATION.name())
                .featureNameId(R.string.feature_name_pi_memory_generation)
                .descriptionId(R.string.description_pi_memory_generation)
                .build(),
            piEntryBuilder
                .packageName(PCS_PACKAGE_NAME)
                .connectionKeyString(IPProtectionRequestType.IPP_GET_PROXY_CONFIG.name())
                .featureNameId(R.string.feature_name_ip_protection)
                .descriptionId(R.string.description_ipp_get_proxy_config)
                .build(),
            piEntryBuilder
                .packageName(PCS_PACKAGE_NAME)
                .connectionKeyString(IPProtectionRequestType.IPP_GET_ANONYMOUS_TOKEN.name())
                .featureNameId(R.string.feature_name_ip_protection)
                .descriptionId(R.string.description_ipp_get_anonymous_token)
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
            asiHttpEntryBuilder
                .connectionKeyString(FeatureName.TOAST_SEARCH.name())
                .featureNameId(R.string.feature_name_toast_search)
                .descriptionId(R.string.description_toast_search_fa_ranking)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(
                    R.string.url_regex_dynamic_contextual_suggestions_classifier_mdd)
                .featureNameId(R.string.feature_name_dynamic_contextual_suggestions)
                .descriptionId(R.string.description_dynamic_contextual_suggestions)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_scam_detection_mdd)
                .featureNameId(R.string.feature_name_scam_detection)
                .descriptionId(R.string.description_scam_detection)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_notification_organizer_mdd)
                .featureNameId(R.string.feature_name_notification_organizer)
                .descriptionId(R.string.description_notification_organizer)
                .build(),
            asiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_notification_summaries_mdd)
                .featureNameId(R.string.feature_name_notification_summaries)
                .descriptionId(R.string.description_notification_summaries)
                .build(),
            attestationEntryBuilder
                .connectionKeyString(ATTESTATION_FEATURE_NAME)
                .featureNameId(R.string.feature_name_android_key_attestation)
                .descriptionId(R.string.description_android_key_attestation)
                .build(),
            new ContentMapEntryBuilder(context)
                .packageName(STATSD_PACKAGE_NAME)
                .connectionType(FC_TRAINING_START_QUERY)
                .connectionKeyString(FeatureName.PLATFORM_LOGGING.name())
                .featureNameId(R.string.feature_name_platform_logging)
                .descriptionId(R.string.description_platform_logging)
                .build(),
            aicoreHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_aicore_service)
                .featureNameId(R.string.feature_name_aicore_service)
                .descriptionId(R.string.description_aicore_service)
                .build(),
            aicoreHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_aicore_service_edgedl)
                .featureNameId(R.string.feature_name_aicore_service)
                .descriptionId(R.string.description_aicore_service)
                .build(),
            aicoreHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_aicore_bt_signature)
                .featureNameId(R.string.feature_name_aicore_service)
                .descriptionId(R.string.description_bt_log_root_signature)
                .build(),
            aicoreHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_aicore_safety)
                .featureNameId(R.string.feature_name_aicore_service)
                .descriptionId(R.string.description_aicore_service)
                .build(),
            pdEntryBuilder
                .connectionKeyStringId(R.string.ap_client_id_aicore)
                .featureNameId(R.string.feature_name_aicore_service)
                .descriptionId(R.string.description_aicore_ap)
                .packageName(AICORE_PACKAGE_NAME)
                .build(),
            launcherHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_search_engine_customization)
                .featureNameId(R.string.feature_name_search_engine_customization)
                .descriptionId(R.string.description_search_engine_customization)
                .build(),
            launcherHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_app_widgets_filtering)
                .featureNameId(R.string.feature_name_app_widgets_filtering)
                .descriptionId(R.string.description_app_widgets_filtering)
                .build(),
            surveyEntryBuilder
                .connectionKeyStringId(R.string.url_regex_user_survey_one_platform)
                .featureNameId(R.string.feature_name_user_survey)
                .descriptionId(R.string.description_user_survey)
                .build(),
            surveyEntryBuilder
                .connectionKeyStringId(R.string.url_regex_user_survey_legacy)
                .featureNameId(R.string.feature_name_user_survey)
                .descriptionId(R.string.description_user_survey)
                .build(),
            psiHttpEntryBuilder
                .connectionKeyStringId(R.string.url_regex_device_intelligence_edgedl)
                .featureNameId(R.string.feature_name_device_intelligence)
                .descriptionId(R.string.description_device_intelligence)
                .build(),
            pdEntryBuilder
                .connectionKeyStringId(R.string.ap_client_id_psi)
                .featureNameId(R.string.feature_name_device_intelligence)
                .descriptionId(R.string.description_psi_ap)
                .packageName(PSI_PACKAGE_NAME)
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
      final String connectionKey =
          switch (details.type()) {
            case HTTP -> details.connectionKey().getHttpConnectionKey().getUrlRegex();
            case PIR -> details.connectionKey().getPirConnectionKey().getUrlRegex();
            case FC_CHECK_IN, FC_TRAINING_START_QUERY, FC_TRAINING_RESULT_UPLOAD ->
                details.connectionKey().getFlConnectionKey().getFeatureName();
            case PD -> details.connectionKey().getPdConnectionKey().getClientId();
            case ATTESTATION_REQUEST ->
                details.connectionKey().getAttestationConnectionKey().getFeatureName();
            case SURVEY_REQUEST -> details.connectionKey().getSurveyConnectionKey().getUrlRegex();
            case FEEDBACK_REQUEST ->
                details.connectionKey().getFeedbackConnectionKey().getFeatureName();
            default ->
                // Should never be reached.
                throw new UnsupportedOperationException(
                    String.format("Unsupported connection type '%s'", details.type().name()));
          };
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
