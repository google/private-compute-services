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

import com.google.android.as.oss.networkusage.db.ConnectionDetails;

/** Thrown when attempting to populate a view with an unknown NetworkUsageEntity. */
public class MissingResourcesForEntityException extends RuntimeException {

  static MissingResourcesForEntityException missingTitleFor(ConnectionDetails connectionDetails) {
    return new MissingResourcesForEntityException(
        String.format(
            "Title not found for %s entity with connectionKey %s",
            connectionDetails.type(),
            NetworkUsageItemUtils.getConnectionKeyString(connectionDetails)));
  }

  static MissingResourcesForEntityException missingDescriptionFor(
      ConnectionDetails connectionDetails) {
    return new MissingResourcesForEntityException(
        String.format(
            "Description not found for %s entity with connectionKey %s",
            connectionDetails.type(),
            NetworkUsageItemUtils.getConnectionKeyString(connectionDetails)));
  }

  private MissingResourcesForEntityException(String message) {
    super(message);
  }
}
