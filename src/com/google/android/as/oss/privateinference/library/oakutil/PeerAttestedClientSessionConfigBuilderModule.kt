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

package com.google.android.`as`.oss.privateinference.library.oakutil

import com.google.android.`as`.oss.privateinference.library.oakutil.proto.VerificationKeys
import com.google.oak.remote_attestation.AttestationVerificationClock
import com.google.oak.session.AttestationPublisher
import com.google.oak.session.OakSessionConfigBuilder
import com.google.protobuf.util.kotlin.toJavaInstant
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Instant
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

@Module
@InstallIn(SingletonComponent::class)
internal object PeerAttestedClientSessionConfigBuilderModule {

  @Provides
  // Not a singleton: we need a new instance every time.
  fun providePeerAttestedClientSessionConfigBuilder(
    verificationKeys: VerificationKeys,
    clock: AttestationVerificationClock,
    publisher: Optional<AttestationPublisher>,
  ): OakSessionConfigBuilder {
    val validUntil = verificationKeys.validUntil.toJavaInstant()
    if (validUntil.isBefore(Instant.ofEpochMilli(clock.millisecondsSinceEpoch()))) {
      throw AttestationVerificationException("Verification keys have expired since $validUntil")
    }

    return PeerAttestedClientSessionConfigBuilder.get(
      verificationKeys.tinkSerializedPublicKeyset.toByteArray(),
      clock,
      publisher.getOrNull(),
    )
  }
}
