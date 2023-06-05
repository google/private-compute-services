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

package com.google.android.as.oss.networkusage.ui.user;

import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_CHECK_IN;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_RESULT_UPLOAD;
import static com.google.android.as.oss.networkusage.db.ConnectionDetails.ConnectionType.FC_TRAINING_START_QUERY;
import static com.google.common.collect.AndroidAccessToCollectors.toImmutableList;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.format.Formatter;
import androidx.annotation.VisibleForTesting;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.Status;
import com.google.android.as.oss.networkusage.ui.content.NetworkUsageLogContentMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.Multimaps;
import com.google.common.flogger.GoogleLogger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collection;
import java.util.StringJoiner;

/** Utils for processing entities before displaying in the UI. */
class NetworkUsageItemUtils {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  private static final Uri GITHUB_URI =
      Uri.parse(
          "https://github.com/google/private-compute-services/tree/master/src/com/google/android/as/oss/assets/federatedcompute");

  static ImmutableList<LogItemWrapper> processEntityList(
      ImmutableList<NetworkUsageEntity> entityList,
      ImmutableList<EntityListProcessor> processors,
      NetworkUsageItemOnClickCallback callback) {

    ImmutableList<NetworkUsageItemWrapper> networkUsageItems =
        entityList.stream().map(NetworkUsageItemWrapper::new).collect(toImmutableList());

    for (EntityListProcessor processor : processors) {
      networkUsageItems = processor.process(networkUsageItems);
    }
    ImmutableList<LogItemWrapper> finalEntityList =
        ImmutableList.<LogItemWrapper>builder()
            .add(NetworkUsageItemUtils.createSummary(networkUsageItems))
            .addAll(NetworkUsageItemUtils.sortAndDivideByDates(networkUsageItems))
            .build();

    finalEntityList.forEach(
        logItemWrapper -> {
          if (logItemWrapper instanceof NetworkUsageItemWrapper) {
            ((NetworkUsageItemWrapper) logItemWrapper).setCallback(callback);
          }
        });
    return finalEntityList;
  }

  /**
   * Returns the list of {@link LogItemWrapper}s consisting of entities sorted by latest creation
   * time and divided by date items.
   */
  @VisibleForTesting
  static ImmutableList<LogItemWrapper> sortAndDivideByDates(
      ImmutableList<NetworkUsageItemWrapper> networkUsageItems) {
    ImmutableList.Builder<LogItemWrapper> resultBuilder = ImmutableList.builder();

    ImmutableListMultimap<LocalDate, NetworkUsageItemWrapper> networkUsageItemsByDate =
        Multimaps.index(
            ImmutableList.sortedCopyOf(
                NetworkUsageItemWrapper.BY_LATEST_TIMESTAMP, networkUsageItems),
            networkUsageItem ->
                getLocalDate(networkUsageItem.latestCreationTime()).atStartOfDay().toLocalDate());

    for (LocalDate day : networkUsageItemsByDate.keySet()) {
      resultBuilder.add(DateTimeDividerItemWrapper.withDateFormat(day));
      resultBuilder.addAll(networkUsageItemsByDate.get(day));
    }
    return resultBuilder.build();
  }

  /** Creates the {@link SummaryWrapper} from the given entities. */
  @VisibleForTesting
  static SummaryWrapper createSummary(ImmutableList<NetworkUsageItemWrapper> wrappers) {
    int updatesCount = 0;
    long totalUpload = 0;
    long totalDownload = 0;

    for (NetworkUsageItemWrapper wrapper : wrappers) {
      updatesCount += wrapper.networkUsageEntities().size();
      for (NetworkUsageEntity networkUsageEntity : wrapper.networkUsageEntities()) {
        totalUpload += networkUsageEntity.uploadSize();
        totalDownload += networkUsageEntity.downloadSize();
      }
    }
    return new SummaryWrapper(
        NetworkUsageSummary.builder()
            .setUpdatesCount(updatesCount)
            .setTotalUpload(totalUpload)
            .setTotalDownload(totalDownload)
            .build());
  }

  static String getConnectionKeyString(ConnectionDetails connectionDetails) {
    switch (connectionDetails.type()) {
      case FC_TRAINING_START_QUERY:
      case FC_TRAINING_RESULT_UPLOAD:
        return connectionDetails.connectionKey().getFlConnectionKey().getFeatureName();
      case HTTP:
        return connectionDetails.connectionKey().getHttpConnectionKey().getUrlRegex();
      case PIR:
        return connectionDetails.connectionKey().getPirConnectionKey().getUrlRegex();
      case PD:
        return connectionDetails.connectionKey().getPdConnectionKey().getClientId();
      default:
        return "";
    }
  }

  @SuppressWarnings("AndroidJdkLibsChecker")
  static String getFormattedTime(Instant instant) {
    DateTimeFormatter formatter =
        DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withZone(ZoneId.systemDefault());
    return formatter.format(instant);
  }

  /**
   * Returns the full list of {@link LogItemWrapper} which make up the contents of the {@link
   * NetworkUsageItemDetailsActivity} for the given item.
   */
  static ImmutableList<LogItemWrapper> createNetworkUsageItemInfo(
      Context context,
      NetworkUsageLogContentMap contentMap,
      NetworkUsageItemWrapper networkUsageItem) {
    return ImmutableList.<LogItemWrapper>builder()
        .addAll(createGlobalInfo(context, contentMap, networkUsageItem.connectionDetails()))
        .addAll(NetworkUsageItemUtils.createTotalInstanceInfo(context, networkUsageItem))
        .build();
  }

  /**
   * Returns the list of {@link NetworkUsageItemInfo} which applies to all instances of the
   * download/upload with the given {@link ConnectionDetails}.
   */
  private static ImmutableList<NetworkUsageItemInfo> createGlobalInfo(
      Context context, NetworkUsageLogContentMap contentMap, ConnectionDetails connectionDetails) {
    String description;
    description =
        contentMap
            .getDescription(connectionDetails)
            .orElseThrow(
                () -> MissingResourcesForEntityException.missingDescriptionFor(connectionDetails));

    ImmutableList.Builder<NetworkUsageItemInfo> resultBuilder =
        ImmutableList.<NetworkUsageItemInfo>builder()
            .add(
                new NetworkUsageItemInfo(
                    context.getString(R.string.details_page_method),
                    contentMap.getMechanismName(connectionDetails)),
                new NetworkUsageItemInfo(
                    context.getString(R.string.details_page_description), description));
    if (connectionDetails.type() != FC_CHECK_IN) {
      resultBuilder.add(
          new NetworkUsageItemInfo(
              context.getString(R.string.details_page_apk_name),
              getApplicationNameForPackage(context, connectionDetails.packageName())));
    }
    return resultBuilder.build();
  }

  /**
   * Returns the list of {@link LogItemWrapper} which contains every batch of {@link
   * NetworkUsageItemInfo} per instance of the download/upload represented by the given item. Each
   * instance's info batch is preceded by the {@link DateTimeDividerItemWrapper} with the creation
   * time of the instance.
   */
  private static ImmutableList<LogItemWrapper> createTotalInstanceInfo(
      Context context, NetworkUsageItemWrapper networkUsageItem) {
    switch (networkUsageItem.connectionDetails().type()) {
      case PIR:
      case HTTP:
        return createDownloadInstanceInfo(context, networkUsageItem);
      case PD:
        return createPdDownloadInstanceInfo(context, networkUsageItem);
      case FC_TRAINING_RESULT_UPLOAD:
      case FC_TRAINING_START_QUERY:
        return createFcResultUploadInstanceInfo(context, networkUsageItem);
      case FC_CHECK_IN:
        return createFcCheckInInstanceInfo(context, networkUsageItem);
      case ATTESTATION_REQUEST:
        return createAttestationRequestInstanceInfo(context, networkUsageItem);
      default:
        logger.atWarning().log(
            "WARNING: Unknown ConnectionType %s", networkUsageItem.connectionDetails().type());
        return ImmutableList.of();
    }
  }

  private static ImmutableList<LogItemWrapper> createDownloadInstanceInfo(
      Context context, NetworkUsageItemWrapper networkUsageItem) {
    ImmutableList.Builder<LogItemWrapper> builder = ImmutableList.builder();
    for (NetworkUsageEntity entity : networkUsageItem.networkUsageEntities()) {
      builder.add(
          DateTimeDividerItemWrapper.withTimeFormat(entity.creationTime()),
          new NetworkUsageItemInfo(
              context.getString(R.string.details_page_status),
              statusToString(context, entity.status())),
          new NetworkUsageItemInfo(
              context.getString(R.string.details_page_download_size),
              bytesCountToString(context, entity.downloadSize())),
          new NetworkUsageItemInfo(context.getString(R.string.details_page_url), entity.url()) {
            // Enable users to copy URL on click.
            @Override
            void onItemClick(Context context) {
              super.onItemClick(context);
              // Gets a handle to the clipboard service.
              ClipboardManager clipboard =
                  (ClipboardManager)
                      context.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
              ClipData clip = ClipData.newPlainText("URL", entity.url());
              if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
              }
            }
          });
    }
    return builder.build();
  }

  private static ImmutableList<LogItemWrapper> createAttestationRequestInstanceInfo(
      Context context, NetworkUsageItemWrapper networkUsageItem) {
    return addUploadAndDownloadItems(context, networkUsageItem);
  }

  private static ImmutableList<LogItemWrapper> createPdDownloadInstanceInfo(
      Context context, NetworkUsageItemWrapper networkUsageItem) {
    ImmutableList.Builder<LogItemWrapper> builder = ImmutableList.builder();
    for (NetworkUsageEntity entity : networkUsageItem.networkUsageEntities()) {
      builder.add(
          DateTimeDividerItemWrapper.withTimeFormat(entity.creationTime()),
          new NetworkUsageItemInfo(
              context.getString(R.string.details_page_status),
              statusToString(context, entity.status())));
      if (entity.downloadSize() > 0) {
        builder.add(
            new NetworkUsageItemInfo(
                context.getString(R.string.details_page_download_size),
                bytesCountToString(context, entity.downloadSize())));
      }
    }
    return builder.build();
  }

  private static ImmutableList<LogItemWrapper> createFcResultUploadInstanceInfo(
      Context context, NetworkUsageItemWrapper networkUsageItem) {
    return Multimaps.index(networkUsageItem.networkUsageEntities(), NetworkUsageEntity::fcRunId)
        .asMap()
        .values()
        .stream()
        .map(
            sameFcRunIdEntities ->
                createFcResultUploadSingleTaskInfo(
                    context,
                    new NetworkUsageItemWrapper(ImmutableList.copyOf(sameFcRunIdEntities))))
        .flatMap(Collection::stream)
        .collect(toImmutableList());
  }

  private static ImmutableList<LogItemWrapper> createFcResultUploadSingleTaskInfo(
      Context context, NetworkUsageItemWrapper networkUsageItemWrapper) {
    long uploadSize = 0;
    long downloadSize = 0;
    StringJoiner policyStringJoiner = new StringJoiner("\n");
    for (NetworkUsageEntity entity : networkUsageItemWrapper.networkUsageEntities()) {
      uploadSize += entity.uploadSize();
      downloadSize += entity.downloadSize();
      if (entity.policyProto().isInitialized() && !entity.policyProto().getName().isEmpty()) {
        policyStringJoiner.add(entity.policyProto().getName());
      }
    }
    ImmutableList.Builder<LogItemWrapper> resultBuilder = ImmutableList.builder();

    resultBuilder.addAll(
        ImmutableList.of(
            DateTimeDividerItemWrapper.withTimeFormat(networkUsageItemWrapper.latestCreationTime()),
            new NetworkUsageItemInfo(
                context.getString(R.string.details_page_status),
                statusToString(context, Status.SUCCEEDED))));
    if (uploadSize > 0) {
      resultBuilder.add(
          new NetworkUsageItemInfo(
              context.getString(R.string.details_page_upload_size),
              bytesCountToString(context, uploadSize)));
    }
    if (downloadSize > 0) {
      resultBuilder.add(
          new NetworkUsageItemInfo(
              context.getString(R.string.details_page_download_size),
              bytesCountToString(context, downloadSize)));
    }
    resultBuilder.addAll(
        ImmutableList.of(
            new NetworkUsageItemInfo(
                context.getString(R.string.details_page_policies), policyStringJoiner.toString()) {
              // Clicking on policy leads to github policies directory.
              @Override
              void onItemClick(Context context) {
                super.onItemClick(context);
                Intent intent = new Intent(Intent.ACTION_VIEW, GITHUB_URI);
                context.startActivity(intent);
              }
            }));
    return resultBuilder.build();
  }

  private static ImmutableList<LogItemWrapper> createFcCheckInInstanceInfo(
      Context context, NetworkUsageItemWrapper networkUsageItem) {
    return addUploadAndDownloadItems(context, networkUsageItem);
  }

  static LocalDate getLocalDate(Instant instant) {
    return OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalDate();
  }

  @VisibleForTesting
  static String bytesCountToString(Context context, long size) {
    return Formatter.formatShortFileSize(context, size);
  }

  @VisibleForTesting
  static String statusToString(Context context, Status status) {
    return context.getString(
        status == Status.SUCCEEDED
            ? R.string.details_page_status_success
            : R.string.details_page_status_failure);
  }

  private static String getApplicationNameForPackage(Context context, String packageName) {
    PackageManager pm = context.getPackageManager();
    try {
      ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
      return pm.getApplicationLabel(ai).toString();
    } catch (NameNotFoundException e) {
      logger.atWarning().withCause(e).log("Unknown package name");
      return "Unknown";
    }
  }

  private static ImmutableList<LogItemWrapper> addUploadAndDownloadItems(
      Context context, NetworkUsageItemWrapper networkUsageItem) {
    ImmutableList.Builder<LogItemWrapper> builder = ImmutableList.builder();
    for (NetworkUsageEntity entity : networkUsageItem.networkUsageEntities()) {
      builder.add(DateTimeDividerItemWrapper.withTimeFormat(entity.creationTime()));
      if (entity.downloadSize() > 0) {
        builder.add(
            new NetworkUsageItemInfo(
                context.getString(R.string.details_page_download_size),
                bytesCountToString(context, entity.downloadSize())));
      }
      if (entity.uploadSize() > 0) {
        builder.add(
            new NetworkUsageItemInfo(
                context.getString(R.string.details_page_upload_size),
                bytesCountToString(context, entity.uploadSize())));
      }
    }
    return builder.build();
  }

  private NetworkUsageItemUtils() {}
}
