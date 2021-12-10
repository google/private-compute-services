/*
 * Copyright 2021 Google LLC
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

package com.google.android.as.oss.fl.brella.api;

import android.os.Parcel;
import com.google.fcp.client.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.fcp.client.common.internal.safeparcel.SafeParcelable;
import com.google.common.base.Objects;

/** Options which describes the job scheduler constraints. */
// TODO: Resolve nullness suppression.
@SuppressWarnings("nullness")
@SafeParcelable.Class(creator = "InAppTrainingConstraintsCreator")
public final class InAppTrainingConstraints extends AbstractSafeParcelable {
  /** Returns a new {@link Builder} for {@link InAppTrainingConstraints}. */
  public static Builder newBuilder() {
    return new Builder();
  }

  /** Builder for {@link InAppTrainingConstraints}. */
  public static final class Builder {
    private boolean requiresNonInteractive = true;
    private boolean requiresCharging = true;
    // This field is ignored by personalization jobs.
    private boolean requiresUnmeteredNetwork = true;

    /**
     * Whether to check if the device is not in interactive mode before running the job. See {@code
     * PowerManager#isInteractive} for details about interactive mode.
     */
    public Builder setRequiresNonInteractive(boolean requiresNonInteractive) {
      this.requiresNonInteractive = requiresNonInteractive;
      return this;
    }

    /** Whether to check if the device is charging before running the job. */
    public Builder setRequiresCharging(boolean requiresCharging) {
      this.requiresCharging = requiresCharging;
      return this;
    }

    /**
     * Whether check if the device is connected to an unmetered network before running the job. This
     * requirement is ignored by personalization jobs.
     */
    public Builder setRequiresUnmeteredNetwork(boolean requiresUnmeteredNetwork) {
      this.requiresUnmeteredNetwork = requiresUnmeteredNetwork;
      return this;
    }

    public InAppTrainingConstraints build() {
      return new InAppTrainingConstraints(
          requiresNonInteractive, requiresCharging, requiresUnmeteredNetwork);
    }
  }

  public static final Creator<InAppTrainingConstraints> CREATOR =
      new InAppTrainingConstraintsCreator();

  @Field(id = 1, getter = "getRequiresNonInteractive")
  private final boolean requiresNonInteractive;

  @Field(id = 2, getter = "getRequiresCharging")
  private final boolean requiresCharging;

  @Field(id = 3, getter = "getRequiresUnmeteredNetwork")
  private final boolean requiresUnmeteredNetwork;

  @Constructor
  InAppTrainingConstraints(
      @Param(id = 1) boolean requiresNonInteractive,
      @Param(id = 2) boolean requiresCharging,
      @Param(id = 3) boolean requiresUnmeteredNetwork) {
    this.requiresNonInteractive = requiresNonInteractive;
    this.requiresCharging = requiresCharging;
    this.requiresUnmeteredNetwork = requiresUnmeteredNetwork;
  }

  @SuppressWarnings("static-access")
  @Override
  public void writeToParcel(Parcel out, int flags) {
    InAppTrainingConstraintsCreator.writeToParcel(this, out, flags);
  }

  @Override
  public boolean equals(Object otherObj) {
    if (this == otherObj) {
      return true;
    }
    if (!(otherObj instanceof InAppTrainingConstraints)) {
      return false;
    }

    InAppTrainingConstraints otherConstraints = (InAppTrainingConstraints) otherObj;

    return requiresNonInteractive == otherConstraints.getRequiresNonInteractive()
        && requiresCharging == otherConstraints.getRequiresCharging()
        && requiresUnmeteredNetwork == otherConstraints.getRequiresUnmeteredNetwork();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(requiresNonInteractive, requiresCharging, requiresUnmeteredNetwork);
  }

  public boolean getRequiresNonInteractive() {
    return requiresNonInteractive;
  }

  public boolean getRequiresCharging() {
    return requiresCharging;
  }

  public boolean getRequiresUnmeteredNetwork() {
    return requiresUnmeteredNetwork;
  }
}
