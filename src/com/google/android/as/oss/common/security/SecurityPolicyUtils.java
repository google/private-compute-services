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

package com.google.android.as.oss.common.security;

import android.content.Context;
import android.content.pm.Signature;
import android.os.Build;
import androidx.annotation.Nullable;
import com.google.android.as.oss.common.security.api.PackageSecurityInfo;
import com.google.common.flogger.GoogleLogger;
import io.grpc.binder.SecurityPolicies;
import io.grpc.binder.SecurityPolicy;
import java.util.ArrayList;
import java.util.List;

/** A utility class for security policies related setups */
public class SecurityPolicyUtils {

  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  /**
   * Makes a {@link SecurityPolicy} from a {@link PackageSecurityInfo}.
   *
   * <p>Returns null if the package security info is invalid.
   */
  @Nullable
  public static SecurityPolicy makeSecurityPolicy(
      PackageSecurityInfo packageSecurityInfo, Context context) {
    return makeSecurityPolicy(
        packageSecurityInfo,
        context,
        /* allowTestKeys= */ false,
        /* usePermissionDeniedForInvalidPolicy= */ true);
  }

  /**
   * Makes a {@link SecurityPolicy} from a {@link PackageSecurityInfo}, with the option to allow
   * test keys.
   *
   * <p>Returns null if the package security info is invalid.
   */
  @Nullable
  public static SecurityPolicy makeSecurityPolicy(
      PackageSecurityInfo packageSecurityInfo, Context context, boolean allowTestKeys) {
    return makeSecurityPolicy(
        packageSecurityInfo,
        context,
        allowTestKeys,
        /* usePermissionDeniedForInvalidPolicy= */ true);
  }

  /**
   * Makes a {@link SecurityPolicy} from a {@link PackageSecurityInfo}, with the option to allow
   * test keys.
   *
   * <p>For an invalid setup, depending on the value of usePermissionDeniedForInvalidPolicy, either
   * returns a permission denied policy or null.
   */
  @Nullable
  public static SecurityPolicy makeSecurityPolicy(
      PackageSecurityInfo packageSecurityInfo,
      Context context,
      boolean allowTestKeys,
      boolean usePermissionDeniedForInvalidPolicy) {
    try {
      return makeSecurityPolicyInternal(packageSecurityInfo, context, allowTestKeys);
    } catch (RuntimeException e) {
      logger.atInfo().withCause(e).log("Failed to make security policy");
      if (usePermissionDeniedForInvalidPolicy) {
        return SecurityPolicies.permissionDenied("Invalid security policy");
      } else {
        return null;
      }
    }
  }

  /** Returns true if the build is a user build. */
  public static Boolean isUserBuild() {
    return Build.TYPE != null && Build.TYPE.equals("user");
  }

  /** Internal method to make a {@link SecurityPolicy} from a {@link PackageSecurityInfo}. */
  private static SecurityPolicy makeSecurityPolicyInternal(
      PackageSecurityInfo packageSecurityInfo, Context context, boolean allowTestKeys) {
    if (packageSecurityInfo.getPackageName().isEmpty()) {
      throw new IllegalArgumentException("Package name is empty");
    }

    List<byte[]> allowedSignatures = new ArrayList<>();
    for (String releaseKey : packageSecurityInfo.getAllowedReleaseKeysList()) {
      allowedSignatures.add(getSignatureBytes(releaseKey));
    }
    if (allowTestKeys) {
      for (String testKey : packageSecurityInfo.getAllowedTestKeysList()) {
        allowedSignatures.add(getSignatureBytes(testKey));
      }
    }
    if (allowedSignatures.isEmpty()) {
      throw new IllegalArgumentException("No allowed signatures found");
    }

    return SecurityPolicies.oneOfSignatureSha256Hash(
        context.getPackageManager(), packageSecurityInfo.getPackageName(), allowedSignatures);
  }

  /** Returns the signature bytes for the given signature hash. */
  private static byte[] getSignatureBytes(String signatureHash) {
    Signature signature = new Signature(signatureHash);
    return signature.toByteArray();
  }

  private SecurityPolicyUtils() {}
}
