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

package com.google.android.as.oss.fl.federatedcompute.logging.statsd;

import static com.google.common.base.Strings.nullToEmpty;

import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Utility class for TF errors to extract safe error messages by using matchers. */
public class TfErrorParser {
  private static final Pattern TF_CC_PATTERN =
      Pattern.compile(
          "^(\\(at tf_wrapper.cc:\\d+\\) Error in \\w+::\\w*\\(\\): [^:]*): (.*)$", Pattern.DOTALL);
  private static final Pattern TF_JAVA_PATTERN =
      Pattern.compile("^(TensorflowException \\(code=\\d+\\)): (.*)$", Pattern.DOTALL);
  private static final Pattern MISSING_OP_PATTERN =
      Pattern.compile("(Op type not registered '[^']*')");
  private static final Pattern TF_EE_ERROR =
      Pattern.compile(
          "^(Error during eligibility eval computation: code: \\d+, error: Error in"
              + " \\w+::\\w*\\(\\): [^:]*): (.*)$",
          Pattern.DOTALL);
  private static final Pattern TF_COMP_ERROR =
      Pattern.compile(
          "^(Error during computation: code: \\d+, error: Error in \\w+::\\w*\\(\\): [^:]*): (.*)$",
          Pattern.DOTALL);

  private static final String REDACTED_STRING = "<redacted>";
  private static final ImmutableList<Pattern> TF_ERROR_PATTERNS;

  static {
    TF_ERROR_PATTERNS =
        ImmutableList.of(TF_CC_PATTERN, TF_COMP_ERROR, TF_JAVA_PATTERN, TF_EE_ERROR);
  }

  private TfErrorParser() {}

  public static String parse(String errorMsg) {
    for (Pattern pattern : TF_ERROR_PATTERNS) {
      Matcher matcher = pattern.matcher(errorMsg);
      if (matcher.find()) {
        return parseMessageFromMatcher(matcher);
      }
    }

    return REDACTED_STRING;
  }

  private static String extractSafeDetailsFromTFSuffix(String tfErrorMessageSuffix) {
    Matcher missingOpMatcher = MISSING_OP_PATTERN.matcher(tfErrorMessageSuffix);
    if (missingOpMatcher.find()) {
      return getNonNullGroup(missingOpMatcher, 1);
    }
    // TODO: Extend this to more buckets of error messages. See
    // http://screen/qVUGbFE3Z7h.

    return REDACTED_STRING;
  }

  @NonNull
  private static String parseMessageFromMatcher(Matcher tfErrorMatcher) {
    String prefix = getNonNullGroup(tfErrorMatcher, 1);
    String detail = getNonNullGroup(tfErrorMatcher, 2);
    return String.format("%s: %s", prefix, extractSafeDetailsFromTFSuffix(detail));
  }

  private static String getNonNullGroup(Matcher m, int groupIdx) {
    return nullToEmpty(m.group(groupIdx));
  }
}
