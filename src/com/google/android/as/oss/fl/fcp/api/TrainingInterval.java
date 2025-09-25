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

package com.google.android.as.oss.fl.fc.api;

import static com.google.common.base.Preconditions.checkArgument;

import android.os.Parcel;
import androidx.annotation.IntDef;
import com.google.fcp.client.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.fcp.client.common.internal.safeparcel.SafeParcelable;
import com.google.common.base.Objects;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Training interval settings, required for local computation tasks, optional for federated tasks.
 *
 * <p>When a training interval setting is provided for a task, the scheduling for the task follows
 * these semantics.
 *
 * <ul>
 *   <li>When a one-time task is scheduled, the earliest next runtime is calculated with federated
 *       compute's default.
 *   <li>When a recurrent task is scheduled, the user defined min interval will be used to
 *       calculated the earliest next runtime.
 *   <li>When a one-time task succeeds, it will be removed.
 *   <li>When any task fails, it will be re-scheduled with federated compute's default failed retry
 *       interval.
 *   <li>The user defined min interval is safeguarded with the FC-maintained minimum and maximum
 *       interval values (i.e. the interval cannot be shorter or longer than federated compute's
 *       limits allow it to be)
 *   <li>When a training interval is specified for a federated task, the longer interval between the
 *       server specified pace-steering scheduling window and the user defined minimum interval will
 *       be chosen for rescheduling the task.
 *   <li>If a minimum scheduling interval is defined, the first time this task will run is after the
 *       minimum scheduling interval passes.
 * </ul>
 */
// TODO: Resolve nullness suppression.
@SuppressWarnings("nullness")
@SafeParcelable.Class(creator = "TrainingIntervalCreator")
public class TrainingInterval extends AbstractSafeParcelable {
  /**
   * The scheduling modes for a task. Recurrent tasks will be rescheduled after each run. One-off
   * task will not be rescheduled if the task succeeds.
   */
  @IntDef({SchedulingMode.RECURRENT, SchedulingMode.ONE_TIME})
  public @interface SchedulingMode {
    int RECURRENT = 0;
    int ONE_TIME = 1;
  }

  /** Builder for {@link TrainingInterval} */
  public static final class Builder {
    @SchedulingMode private int schedulingMode = SchedulingMode.RECURRENT;
    private long minimumIntervalMillis = 0L;

    /**
     * Specifies the scheduling mode, the value needs to be one of the values in {@link
     * SchedulingMode}. If the user does not provide a value, the default value will be {@link
     * SchedulingMode#RECURRENT}.
     */
    @CanIgnoreReturnValue
    public Builder setSchedulingMode(@SchedulingMode int mode) {
      schedulingMode = mode;
      return this;
    }

    /**
     * Sets the minimum time interval between two training runs in milliseconds.
     *
     * <p>This field will only be used when the Scheduling mode is {@link SchedulingMode#RECURRENT}.
     * Only positive values are accepted, zero or negative values will result in
     * IllegalArgumentException.
     *
     * <p>Please also note this value is advisory, which does not guarantee the job will be run
     * immediately after the interval expired. Federated compute will still enforce a minimum
     * required interval to ensure system health.
     */
    @CanIgnoreReturnValue
    public Builder setMinimumIntervalMillis(long minimumIntervalMillis) {
      this.minimumIntervalMillis = minimumIntervalMillis;
      return this;
    }

    public TrainingInterval build() {
      return new TrainingInterval(schedulingMode, minimumIntervalMillis);
    }
  }

  /** Create an instance of {@link Builder} for {@link TrainingInterval} */
  public static Builder newBuilder() {
    return new Builder();
  }

  public static final Creator<TrainingInterval> CREATOR = new TrainingIntervalCreator();

  @Field(id = 1, getter = "getSchedulingMode")
  private final int schedulingMode;

  @Field(id = 2, getter = "getMinimumIntervalMillis")
  private final long minimumIntervalMillis;

  @Constructor
  TrainingInterval(
      @Param(id = 1) @SchedulingMode int schedulingMode,
      @Param(id = 2) long minimumIntervalMillis) {
    validate(schedulingMode, minimumIntervalMillis);
    this.schedulingMode = schedulingMode;
    this.minimumIntervalMillis = minimumIntervalMillis;
  }

  public int getSchedulingMode() {
    return schedulingMode;
  }

  public long getMinimumIntervalMillis() {
    return minimumIntervalMillis;
  }

  @SuppressWarnings("static-access")
  @Override
  public void writeToParcel(Parcel out, int flags) {
    TrainingIntervalCreator.writeToParcel(this, out, flags);
  }

  @Override
  public boolean equals(Object otherObj) {
    if (this == otherObj) {
      return true;
    }
    if (!(otherObj instanceof TrainingInterval)) {
      return false;
    }

    TrainingInterval otherInterval = (TrainingInterval) otherObj;

    return schedulingMode == otherInterval.schedulingMode
        && minimumIntervalMillis == otherInterval.minimumIntervalMillis;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(schedulingMode, minimumIntervalMillis);
  }

  private static void validate(@SchedulingMode int schedulingMode, long minimumIntervalMillis) {
    checkArgument(
        schedulingMode != SchedulingMode.RECURRENT || minimumIntervalMillis > 0,
        "Recurrent jobs cannot have non-positive minimal interval.");
  }
}
