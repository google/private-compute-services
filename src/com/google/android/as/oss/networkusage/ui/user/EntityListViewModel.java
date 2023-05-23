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

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.as.oss.networkusage.db.NetworkUsageEntity;
import com.google.android.as.oss.networkusage.db.NetworkUsageLogRepository;
import java.util.List;

/** ViewModel for the list of NetworkUsageEntities to show in the NetworkUsageLogFragment. */
class EntityListViewModel extends ViewModel {
  private final LiveData<List<NetworkUsageEntity>> entitiesLiveData;

  public EntityListViewModel(NetworkUsageLogRepository repository) {
    super();
    this.entitiesLiveData = repository.getAll();
  }

  LiveData<List<NetworkUsageEntity>> getEntityListLiveData() {
    return entitiesLiveData;
  }

  /** ViewModel factory for EntityListViewModel. */
  static class EntityListViewModelFactory implements ViewModelProvider.Factory {
    private final NetworkUsageLogRepository repository;

    public EntityListViewModelFactory(NetworkUsageLogRepository repository) {
      this.repository = repository;
    }

    @SuppressWarnings({"unchecked"})
    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
      return (T) new EntityListViewModel(repository);
    }
  }
}
