/*
 * Copyright 2024 Google LLC
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

package com.google.android.`as`.oss.fl.federatedcompute.statsd.examplegenerator

import android.database.Cursor
import org.tensorflow.example.Example
import org.tensorflow.example.Features

/**
 * Default [ExamplesGenerator]. It generates a TF Example for each row in the supplied [cursor].
 * Each example has one feature for each of the cursor's columns.
 *
 * <p>Columns of type [Cursor.FIELD_TYPE_BLOB] and [Cursor.FIELD_TYPE_NULL] are not supported.
 */
class DefaultExamplesGenerator(private val cursor: Cursor) : ExamplesGenerator {
  init {
    if (cursor.moveToFirst()) {
      // Ensure the cursor can be parsed.
      for (col in 0 until cursor.columnCount) {
        val columnType = cursor.getType(col)
        require(columnType != Cursor.FIELD_TYPE_NULL) {
          "Value of column '${cursor.getColumnName(col)}' is null. Consider IFNULL function."
        }
        require(columnType != Cursor.FIELD_TYPE_BLOB) {
          "DefaultExamplesGenerator does not support columns of type $columnType."
        }
      }
    }
  }

  override fun hasNext(): Boolean = !cursor.isAfterLast && !cursor.isClosed

  override fun next(): Example {
    if (!hasNext()) {
      throw NoSuchElementException()
    }
    val example = cursor.toTfExample()
    cursor.moveToNext()
    return example
  }

  override fun close() = Unit

  internal fun Cursor.toTfExample(): Example {
    val features = Features.newBuilder()
    for (col in 0 until this.columnCount) {
      val colName = this.columnNames[col]
      when (val type = this.getType(col)) {
        Cursor.FIELD_TYPE_FLOAT ->
          features.putFeature(colName, TFFeatureCreator.floatList(this.getFloat(col)))
        Cursor.FIELD_TYPE_INTEGER ->
          features.putFeature(colName, TFFeatureCreator.int64List(this.getLong(col)))
        Cursor.FIELD_TYPE_STRING ->
          features.putFeature(colName, TFFeatureCreator.bytesList(this.getString(col)))
        // FIELD_TYPE_BLOB and FIELD_TYPE_NULL are intentionally not supported by this generator.
        else -> throw IllegalArgumentException("$type not supported.")
      }
    }
    return Example.newBuilder().setFeatures(features).build()
  }
}
