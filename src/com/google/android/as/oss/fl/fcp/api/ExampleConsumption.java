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
import com.google.fcp.client.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.fcp.client.common.internal.safeparcel.SafeParcelable;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Arrays;
import javax.annotation.Nullable;

/**
 * A container for information regarding an example store access, including the collection name, the
 * selection criteria, the number of examples which has been used, the resumption token if available
 * and the selector context if available.
 */
// TODO: Resolve nullness suppression.
@SuppressWarnings("nullness")
@SafeParcelable.Class(creator = "ExampleConsumptionCreator")
public class ExampleConsumption extends AbstractSafeParcelable {
  /** Builder for {@link ExampleConsumption} */
  public static final class Builder {
    private String collectionName;
    private byte[] selectionCriteria;
    private int exampleCount;
    @Nullable private byte[] resumptionToken;
    @Nullable private byte[] selectorContext;

    /** Specifies the collection Uri for the example store. */
    @CanIgnoreReturnValue
    public Builder setCollectionName(String collectionName) {
      this.collectionName = collectionName;
      return this;
    }

    /** Sets the selection criteria. */
    @CanIgnoreReturnValue
    public Builder setSelectionCriteria(byte[] criteria) {
      this.selectionCriteria = criteria;
      return this;
    }

    /**
     * Sets the number of examples has been used for this training task.
     *
     * <p>Please note this value may not be accurate when additional sample selections happen inside
     * the TF computation.
     */
    @CanIgnoreReturnValue
    public Builder setExampleCount(int exampleCount) {
      this.exampleCount = exampleCount;
      return this;
    }

    /**
     * Sets the resumption token that was returned by the ExampleStoreService along with the last
     * used example.
     */
    @CanIgnoreReturnValue
    public Builder setResumptionToken(@Nullable byte[] resumptionToken) {
      this.resumptionToken = resumptionToken;
      return this;
    }

    /** Sets the SelectorContext for the query. */
    @CanIgnoreReturnValue
    public Builder setSelectorContext(@Nullable byte[] selectorContext) {
      this.selectorContext = selectorContext;
      return this;
    }

    public ExampleConsumption build() {
      return new ExampleConsumption(
          collectionName, selectionCriteria, exampleCount, resumptionToken, selectorContext);
    }
  }

  /** Create an instance of {@link Builder} for {@link ExampleConsumption} */
  public static Builder newBuilder() {
    return new Builder();
  }

  public static final Creator<ExampleConsumption> CREATOR = new ExampleConsumptionCreator();

  @Field(id = 1, getter = "getCollectionName")
  private final String collectionName;

  @Field(id = 2, getter = "getSelectionCriteria")
  private final byte[] selectionCriteria;

  @Field(id = 3, getter = "getExampleCount")
  private final int exampleCount;

  @Field(id = 4, getter = "getResumptionToken")
  @Nullable
  private final byte[] resumptionToken;

  @Field(id = 5, getter = "getSelectorContext")
  @Nullable
  private final byte[] selectorContext;

  @Constructor
  ExampleConsumption(
      @Param(id = 1) String collectionName,
      @Param(id = 2) byte[] selectionCriteria,
      @Param(id = 3) int exampleCount,
      @Nullable @Param(id = 4) byte[] resumptionToken,
      @Nullable @Param(id = 5) byte[] selectorContext) {
    validate(collectionName, selectionCriteria);
    this.collectionName = collectionName;
    this.selectionCriteria = selectionCriteria;
    this.exampleCount = exampleCount;
    this.resumptionToken = resumptionToken;
    this.selectorContext = selectorContext;
  }

  public String getCollectionName() {
    return collectionName;
  }

  public byte[] getSelectionCriteria() {
    return selectionCriteria;
  }

  public int getExampleCount() {
    return exampleCount;
  }

  @Nullable
  public byte[] getResumptionToken() {
    return resumptionToken;
  }

  @Nullable
  public byte[] getSelectorContext() {
    return selectorContext;
  }

  @SuppressWarnings("static-access")
  @Override
  public void writeToParcel(Parcel out, int flags) {
    ExampleConsumptionCreator.writeToParcel(this, out, flags);
  }

  @Override
  public boolean equals(Object otherObj) {
    if (this == otherObj) {
      return true;
    }
    if (!(otherObj instanceof ExampleConsumption)) {
      return false;
    }

    ExampleConsumption otherInstance = (ExampleConsumption) otherObj;

    return collectionName.equals(otherInstance.collectionName)
        && Arrays.equals(selectionCriteria, otherInstance.selectionCriteria)
        && exampleCount == otherInstance.exampleCount
        && Arrays.equals(resumptionToken, otherInstance.resumptionToken)
        && Arrays.equals(selectorContext, otherInstance.selectorContext);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(
        collectionName,
        Arrays.hashCode(selectionCriteria),
        exampleCount,
        Arrays.hashCode(resumptionToken),
        Arrays.hashCode(selectorContext));
  }

  private static void validate(String collectionName, byte[] selectionCriteria) {
    checkArgument(
        !Strings.isNullOrEmpty(collectionName) && selectionCriteria != null,
        "Collection name cannot be null or empty. Selection criteria cannot be null.");
  }
}
