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

use alloc::{
    boxed::Box,
    format,
    string::{String, ToString},
    sync::Arc,
    vec,
};
use core::ptr::null_mut;
use oak_time::Duration;

use anyhow::Context;
use jni::{
    objects::{JByteArray, JClass, JObject, JString, JValue},
    sys::{jlong, jobject},
    JNIEnv,
};
use oak_attestation_verification::{
    verifier::{to_attestation_results, verify as verify_attestation},
    create_amd_verifier, create_insecure_verifier, create_intel_tdx_verifier,
    EventLogVerifier, SessionBindingPublicKeyPolicy,
};
use oak_attestation_verification_types::verifier::AttestationVerifier;
use oak_crypto::certificate::certificate_verifier::CertificateVerifier;
use oak_crypto_tink::signature_verifier::SignatureVerifier;
use oak_jni_attestation_publisher::JNIAttestationPublisher;
use oak_jni_attestation_verification_clock::JNIClock;
use oak_proto_rust::attestation::CERTIFICATE_BASED_ATTESTATION_ID;
use oak_proto_rust::oak::attestation::v1::{
    reference_values, AttestationResults, Endorsements, Evidence, ReferenceValues,
    RootLayerReferenceValues,
};
use oak_session::{
    attestation::AttestationType, config::SessionConfig, config::SessionConfigBuilder,
    handshake::HandshakeType,
    key_extractor::{DefaultBindingKeyExtractor, DefaultSigningKeyExtractor, KeyExtractor},
    session::AttestationPublisher,
};
use prost::Message;

/// Acceptable time period before the certificate validity starts and after it ends that allows
/// devices with skewed clocks to validate certificates.
/// 26 hours were chosen based on the conversation with the Android clock team
const ALLOWED_CLOCK_SKEW: Duration = Duration::from_seconds(26 * 60 * 60);

/// Maximum accepted certificate validity duration.
/// Client uses this value to verify that the certificate wasn't issued for a unnecessary amount of
/// time. 216 days were chosen, because current Keystore configuration produces public keysets are
/// only valid for 216 days.
const VALIDITY_LIMIT: Duration = Duration::from_seconds(216 * 24 * 60 * 60);

struct ReferenceValuesAttestationVerifier {
    reference_values: ReferenceValues,
    clock: Arc<dyn oak_time::Clock>,
}

impl AttestationVerifier for ReferenceValuesAttestationVerifier {
    fn verify(
        &self,
        evidence: &Evidence,
        endorsements: &Endorsements,
    ) -> anyhow::Result<AttestationResults> {
        let verification_result = verify_attestation(
            self.clock.get_time().into_unix_millis(),
            evidence,
            endorsements,
            &self.reference_values,
        );
        Ok(to_attestation_results(&verification_result))
    }
}

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

fn create_oak_containers_attestation_verifier<T: oak_time::Clock + 'static>(
    clock: T,
    reference_values: &ReferenceValues,
    root_layer: &RootLayerReferenceValues,
) -> anyhow::Result<Box<dyn AttestationVerifier>> {
    if root_layer.amd_sev.is_some() {
        return Ok(Box::new(create_amd_verifier(clock, reference_values)?));
    }
    if root_layer.intel_tdx.is_some() {
        return Ok(Box::new(create_intel_tdx_verifier(
            clock,
            reference_values,
        )?));
    }
    if root_layer.insecure.is_some() {
        return Ok(Box::new(create_insecure_verifier(clock, reference_values)?));
    }

    anyhow::bail!("No supported root layer reference values")
}

fn create_workload_attestation_components<T: oak_time::Clock + 'static>(
    clock: T,
    reference_values: &ReferenceValues,
) -> anyhow::Result<(Box<dyn AttestationVerifier>, Box<dyn KeyExtractor>)> {
    match reference_values.r#type.as_ref() {
        Some(reference_values::Type::OakContainers(rvs)) => {
            let root_layer = rvs.root_layer.as_ref().context("No root layer reference values")?;
            Ok((
                create_oak_containers_attestation_verifier(clock, reference_values, root_layer)?,
                Box::new(DefaultBindingKeyExtractor {}),
            ))
        }
        Some(reference_values::Type::OakRestrictedKernel(_)) => Ok((
            Box::new(ReferenceValuesAttestationVerifier {
                reference_values: reference_values.clone(),
                clock: Arc::new(clock),
            }),
            // Restricted Kernel verification exposes the session binding key
            // through the extracted signing key path used by Oak's legacy
            // default extractor.
            Box::new(DefaultSigningKeyExtractor {}),
        )),
        _ => anyhow::bail!("Unsupported workload reference value type"),
    }
}

#[no_mangle]
extern "system" fn Java_com_google_android_as_oss_privateinference_library_oakutil_PeerAttestedClientSessionConfigBuilder_nativeGet(
    mut env: JNIEnv,
    _class: JClass,
    public_keyset_bytes: JByteArray,
    java_clock_object: JObject,
    nullable_attestation_publisher: JObject,
    workload_reference_values_bytes: JByteArray,
    workload_attestation_id: JString,
) -> jobject {
    let result = internal_native_get(
        &mut env,
        public_keyset_bytes,
        java_clock_object,
        nullable_attestation_publisher,
        workload_reference_values_bytes,
        workload_attestation_id,
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
    workload_reference_values_bytes: JByteArray,
    workload_attestation_id: JString,
) -> anyhow::Result<jobject> {
    let jni_clock = JNIClock::new(env, &java_clock_object)
        .map_err(|e| anyhow::anyhow!("Failed to create JNIClock: {e:?}"))?;
    let public_keyset_vec = env
        .convert_byte_array(&public_keyset_bytes)
        .map_err(|e| anyhow::anyhow!("Failed to convert byte array: {e:?}"))?;
    let workload_reference_values_vec = env
        .convert_byte_array(&workload_reference_values_bytes)
        .map_err(|e| anyhow::anyhow!("Failed to convert workload reference values: {e:?}"))?;
    let workload_attestation_id: String = env
        .get_string(&workload_attestation_id)
        .map_err(|e| anyhow::anyhow!("Failed to convert workload attestation ID: {e:?}"))?
        .into();

    let mut session_config_builder =
        SessionConfig::builder(AttestationType::PeerUnidirectional, HandshakeType::NoiseNN);

    // Use workload verification as a fail-closed replacement for the legacy
    // certificate path. Registering both verifiers would allow a peer to omit
    // workload evidence and still pass via the default legacy aggregator.
    if workload_reference_values_vec.is_empty() {
        let mut certificate_verifier =
            CertificateVerifier::new(SignatureVerifier::new(&public_keyset_vec));
        certificate_verifier.set_allowed_clock_skew(ALLOWED_CLOCK_SKEW);
        certificate_verifier.set_validity_limit(VALIDITY_LIMIT);

        let policy = SessionBindingPublicKeyPolicy::new(certificate_verifier);

        let attestation_verifier =
            EventLogVerifier::new(vec![Box::new(policy)], Arc::new(jni_clock));

        session_config_builder = session_config_builder
            .add_peer_verifier_with_key_extractor(
                CERTIFICATE_BASED_ATTESTATION_ID.to_string(),
                Box::new(attestation_verifier),
                Box::new(DefaultBindingKeyExtractor {}),
            );
    } else {
        if workload_attestation_id.is_empty() {
            anyhow::bail!("Workload attestation ID is required for workload verification");
        }
        let reference_values = ReferenceValues::decode(workload_reference_values_vec.as_slice())
            .map_err(|e| anyhow::anyhow!("Failed to decode workload reference values: {e:?}"))?;
        let (attestation_verifier, key_extractor) =
            create_workload_attestation_components(jni_clock, &reference_values)?;

        session_config_builder = session_config_builder
            .add_peer_verifier_with_key_extractor(
                workload_attestation_id,
                attestation_verifier,
                key_extractor,
            );
    }

    if !nullable_attestation_publisher.is_null() {
        let attestation_publisher: Arc<dyn AttestationPublisher> =
            Arc::new(JNIAttestationPublisher::new(env, &nullable_attestation_publisher)?);

        session_config_builder =
            session_config_builder.add_attestation_publisher(&attestation_publisher);
    }

    new_java_session_config_builder(env, session_config_builder)
}
