// Copyright 2025 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// We are not actually no_std because the jni crate is pulling it in, but at
// least this enforces that this lib isn't using anything from the std lib
#![no_std]

extern crate alloc;

use alloc::{boxed::Box, format, string::ToString, sync::Arc, vec};
use core::ptr::null_mut;
use oak_time::Duration;

use jni::{
    objects::{JByteArray, JClass, JObject, JValue},
    sys::{jlong, jobject},
    JNIEnv,
};
use oak_attestation_verification::{EventLogVerifier, SessionBindingPublicKeyPolicy};
use oak_crypto::certificate::certificate_verifier::CertificateVerifier;
use oak_crypto_tink::signature_verifier::SignatureVerifier;
use oak_jni_attestation_publisher::JNIAttestationPublisher;
use oak_jni_attestation_verification_clock::JNIClock;
use oak_proto_rust::attestation::CERTIFICATE_BASED_ATTESTATION_ID;
use oak_session::{
    attestation::AttestationType, config::SessionConfig, config::SessionConfigBuilder,
    handshake::HandshakeType, key_extractor::DefaultBindingKeyExtractor,
    session::AttestationPublisher,
};

/// Acceptable time period before the certificate validity starts and after it ends that allows
/// devices with skewed clocks to validate certificates.
/// 26 hours were chosen based on the conversation with the Android clock team
const ALLOWED_CLOCK_SKEW: Duration = Duration::from_seconds(26 * 60 * 60);

/// Maximum accepted certificate validity duration.
/// Client uses this value to verify that the certificate wasn't issued for a unnecessary amount of
/// time. 216 days were chosen, because current Keystore configuration produces public keysets are
/// only valid for 216 days.
const VALIDITY_LIMIT: Duration = Duration::from_seconds(216 * 24 * 60 * 60);

macro_rules! runtime_exception {
    ($env:ident, $($msg:expr),*) => {
        $env.throw_new("java/lang/RuntimeException", format!($($msg,)*))
            // At this point, there's not much we can do, so panic is the best option.
            .unwrap_or_else(|e| panic!("Failed to throw exception: {:?}: ${:?}", format!($($msg,)*), e))
    };
}

pub fn new_java_session_config_builder(
    env: &mut JNIEnv,
    session_config_builder: SessionConfigBuilder,
) -> anyhow::Result<jobject> {
    let builder_ptr = Box::into_raw(Box::new(session_config_builder));
    let cls = env
        .find_class("com/google/oak/session/OakSessionConfigBuilder")
        .map_err(|e| anyhow::anyhow!("Failed to find class: {e:?}"))?;

    env.new_object(cls, "(J)V", &[JValue::Long(builder_ptr as jlong)])
        .map(|o| o.as_raw())
        .map_err(|e| anyhow::anyhow!("Failed to create object: {e:?}"))
}

#[no_mangle]
    extern "system" fn Java_com_google_android_as_oss_privateinference_library_oakutil_PeerAttestedClientSessionConfigBuilder_nativeGet(
    mut env: JNIEnv,
    _class: JClass,
    public_keyset_bytes: JByteArray,
    java_clock_object: JObject,
    nullable_attestation_publisher: JObject,
) -> jobject {
    let result = internal_native_get(
        &mut env,
        public_keyset_bytes,
        java_clock_object,
        nullable_attestation_publisher,
    );
    match result {
        Ok(result) => result,
        Err(err) => {
            match env.exception_check() {
                // Exception was already thrown, no action needed.
                Ok(true) => {}
                // Exception was not throw, so throw one now.
                Ok(false) => runtime_exception!(env, "{:#}", err),
                Err(e) => runtime_exception!(env, "Failed to check exception: {:#}", e),
            }
            null_mut()
        }
    }
}

fn internal_native_get(
    env: &mut JNIEnv,
    public_keyset_bytes: JByteArray,
    java_clock_object: JObject,
    nullable_attestation_publisher: JObject,
) -> anyhow::Result<jobject> {
    let jni_clock = JNIClock::new(env, &java_clock_object)
        .map_err(|e| anyhow::anyhow!("Failed to create JNIClock: {e:?}"))?;
    let public_keyset_vec = env
        .convert_byte_array(&public_keyset_bytes)
        .map_err(|e| anyhow::anyhow!("Failed to convert byte array: {e:?}"))?;

    let mut certificate_verifier =
        CertificateVerifier::new(SignatureVerifier::new(&public_keyset_vec));
    certificate_verifier.set_allowed_clock_skew(ALLOWED_CLOCK_SKEW);
    certificate_verifier.set_validity_limit(VALIDITY_LIMIT);

    let policy = SessionBindingPublicKeyPolicy::new(certificate_verifier);

    let attestation_verifier = EventLogVerifier::new(vec![Box::new(policy)], Arc::new(jni_clock));

    let mut session_config_builder =
        SessionConfig::builder(AttestationType::PeerUnidirectional, HandshakeType::NoiseNN)
            .add_peer_verifier_with_key_extractor(
                CERTIFICATE_BASED_ATTESTATION_ID.to_string(),
                Box::new(attestation_verifier),
                Box::new(DefaultBindingKeyExtractor {}),
            );

    if !nullable_attestation_publisher.is_null() {
        let attestation_publisher: Arc<dyn AttestationPublisher> =
            Arc::new(JNIAttestationPublisher::new(env, &nullable_attestation_publisher)?);

        session_config_builder =
            session_config_builder.add_attestation_publisher(&attestation_publisher);
    }

    new_java_session_config_builder(env, session_config_builder)
}
