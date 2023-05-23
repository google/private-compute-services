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

import static com.google.android.as.oss.networkusage.ui.user.NetworkUsageItemDetailsActivity.NETWORK_USAGE_ITEM_EXTRA_KEY;
import static com.google.common.collect.AndroidAccessToCollectors.toImmutableList;
import static java.util.Comparator.comparing;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import com.google.android.as.oss.networkusage.db.ConnectionDetails;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.common.collect.ImmutableList;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;

/** {@link LogItemWrapper} for {@link NetworkUsageEntity} items in the RecyclerView. */
class NetworkUsageItemWrapper extends LogItemWrapper implements Parcelable {
  public static final Creator<NetworkUsageItemWrapper> CREATOR =
      new Creator<NetworkUsageItemWrapper>() {
        @Override
        public NetworkUsageItemWrapper createFromParcel(Parcel in) {
          return new NetworkUsageItemWrapper(in);
        }

        @Override
        public NetworkUsageItemWrapper[] newArray(int size) {
          return new NetworkUsageItemWrapper[size];
        }
      };
  public static final Comparator<NetworkUsageItemWrapper> BY_LATEST_TIMESTAMP =
      comparing(NetworkUsageItemWrapper::latestCreationTime, Comparator.reverseOrder());

  private final ImmutableList<NetworkUsageEntity> networkUsageEntities;
  @Nullable private NetworkUsageItemOnClickCallback callback = null;

  NetworkUsageItemWrapper(NetworkUsageEntity networkUsageEntity) {
    this(ImmutableList.of(networkUsageEntity));
  }

  NetworkUsageItemWrapper(ImmutableList<NetworkUsageEntity> networkUsageEntities) {
    this.networkUsageEntities =
        ImmutableList.sortedCopyOf(NetworkUsageEntity.BY_LATEST_TIMESTAMP, networkUsageEntities);
  }

  NetworkUsageItemWrapper(Parcel in) {
    this.networkUsageEntities =
        ImmutableList.copyOf(
            in.readParcelableList(new ArrayList<>(), NetworkUsageEntity.class.getClassLoader()));
  }

  void setCallback(@Nullable NetworkUsageItemOnClickCallback callback) {
    this.callback = callback;
  }

  @Nullable
  NetworkUsageItemOnClickCallback getCallback() {
    return this.callback;
  }

  @Override
  boolean isSameItemAs(@Nullable LogItemWrapper other) {
    if (!(other instanceof NetworkUsageItemWrapper)) {
      return false;
    }
    return connectionDetails().equals(((NetworkUsageItemWrapper) other).connectionDetails());
  }

  @Override
  boolean hasSameContentAs(@Nullable LogItemWrapper other) {
    if (!(other instanceof NetworkUsageItemWrapper)) {
      return false;
    }
    NetworkUsageItemWrapper otherNetworkUsageItemWrapper = (NetworkUsageItemWrapper) other;
    return latestCreationTime().equals(otherNetworkUsageItemWrapper.latestCreationTime())
        && networkUsageEntities.size() == otherNetworkUsageItemWrapper.networkUsageEntities.size()
        && networkUsageEntities.containsAll(otherNetworkUsageItemWrapper.networkUsageEntities);
  }

  @Override
  LogItemViewHolderFactory getViewHolderFactory() {
    return LogItemViewHolderFactory.USAGE_ITEM_VIEW_HOLDER_FACTORY;
  }

  @Override
  void onItemClick(Context context) {
    if (this.callback != null) {
      this.callback.onItemInspectionCall();
    }
    Intent intent = new Intent();
    intent.setComponent(new ComponentName(context, NetworkUsageItemDetailsActivity.class));
    intent.putExtra(NETWORK_USAGE_ITEM_EXTRA_KEY, this);
    context.startActivity(intent);
  }

  public ImmutableList<NetworkUsageEntity> networkUsageEntities() {
    return networkUsageEntities;
  }

  public ConnectionDetails connectionDetails() {
    return networkUsageEntities.get(0).connectionDetails();
  }

  public Instant latestCreationTime() {
    return networkUsageEntities.get(0).creationTime();
  }

  public NetworkUsageItemWrapper newerEntitiesOnly(Instant earliestPermittedDate) {
    ImmutableList<NetworkUsageEntity> newerEntities =
        networkUsageEntities.stream()
            .filter(entity -> entity.creationTime().isAfter(earliestPermittedDate))
            .collect(toImmutableList());
    return new NetworkUsageItemWrapper(newerEntities);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelableList(networkUsageEntities, flags);
  }
}
