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

#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_bsa_calls.h"

#include <jni.h>

#include <cstdint>
#include <memory>
#include <optional>
#include <string>
#include <utility>
#include <vector>

#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_global_ref.h"
#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_helpers.h"
#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_logging.h"
#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_message_interface_wrapper.h"
#include "third_party/absl/functional/any_invocable.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/str_format.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/time/time.h"
#include "third_party/absl/types/span.h"
#include "third_party/quiche/blind_sign_auth/blind_sign_auth.h"
#include "third_party/quiche/blind_sign_auth/blind_sign_auth_interface.h"
#include "third_party/quiche/blind_sign_auth/blind_sign_message_interface.h"
#include "third_party/quiche/blind_sign_auth/blind_sign_message_response.h"

jclass kBlindSignTokenClass;
jclass kNativeAttestAndSignCallbackClass;
jmethodID kNativeAttestAndSignCallbackConstructorID;
jmethodID kOnChallengeDataMethodID;
jmethodID kOnSignedTokensMethodID;
jmethodID kOnErrorMethodID;
jmethodID kBlindSignTokenCtorID;

namespace privateinference::bsa::calls {
class BsaContext;

absl::StatusOr<jobject> CreateJavaBlindSignToken(JNIEnv* env,
                                                 quiche::BlindSignToken token);

absl::StatusOr<jobject> CreateJavaBlindSignTokensList(
    JNIEnv* env, absl::Span<quiche::BlindSignToken> tokens);

quiche::AttestationDataCallback WrapAttestationDataCallback(
    JNIEnv* env, JavaVM* vm, jobject attestation_data_callback);

// Since the contract (assuming correct implementation) is that
// WrapSignedTokenCallback is guaranteed to be called once it's created, we also
// pass it ownership of a BsaContext, so that it can be freed when the callback
// completes.
quiche::SignedTokenCallback WrapSignedTokenCallback(
    JNIEnv* env, JavaVM* vm, jobject signed_token_callback,
    std::unique_ptr<BsaContext> bsa_context);

privacy::ppn::BlindSignAuthOptions GetBlindSignAuthOptions() {
  privacy::ppn::BlindSignAuthOptions options;
  options.set_enable_privacy_pass(true);
  return options;
}

// A helper struct that keeps track of things that need to stay alive during the
// sequence of callbacks.
//
// The BSA library accepts an unowned pointer to the MessageInterface, so
// something needs to manage its lifetime: that's this object.
//
// We also create the BSA object itself here, and mange its lifetime as well.
class BsaContext {
 public:
  explicit BsaContext(
      std::unique_ptr<JavaMessageInterfaceWrapper> message_interface_wrapper)
      : message_interface_wrapper_(std::move(message_interface_wrapper)),
        bsa_(std::make_unique<quiche::BlindSignAuth>(
            message_interface_wrapper_.get(), GetBlindSignAuthOptions())) {}
  quiche::BlindSignAuthInterface* GetBsa() { return bsa_.get(); }
  ~BsaContext() = default;

  // Can't copy, but can move.
  BsaContext(const BsaContext&) = delete;
  BsaContext& operator=(const BsaContext&) = delete;
  BsaContext(BsaContext&&) = default;
  BsaContext& operator=(BsaContext&&) = default;

 private:
  std::unique_ptr<JavaMessageInterfaceWrapper> message_interface_wrapper_;
  std::unique_ptr<quiche::BlindSignAuthInterface> bsa_;
};

// Adapt the Java-defined proxy layer enum to the C++ enum.
//
// For now, the ordinal values are aligned, but it would be easy to accidentally
// change this alignment without noticing.
absl::StatusOr<quiche::ProxyLayer> GetQuicheProxyLayer(int proxy_layer) {
  switch (proxy_layer) {
    // The indices here match the enum values in the Java ProxyLayer enum.
    case 0:
      return quiche::ProxyLayer::kProxyA;
    case 1:
      return quiche::ProxyLayer::kProxyB;
    case 2:
      return quiche::ProxyLayer::kTerminalLayer;
    default:
      return absl::InvalidArgumentError(
          absl::StrCat("Invalid proxy layer ordinal: ", proxy_layer));
  }
}
// A helper to call the onError method for a callback struct.
//
// This can be used with any Java-provided callback object that implements the
// ErrorCallback interface.
void OnError(JNIEnv* env, const absl::Status& status, jobject callback) {
  absl::StatusOr<jobject> java_exception =
      helpers::CreateAbslStatusException(env, status);
  if (!java_exception.ok()) {
    bsa_log_info("Failed to create Java Exception for error message: %s",
                 status.ToString().c_str());
    // We will still try to call the onError callback with null exception,
    // it will probably crash, but this is better than just hanging.
  }
  env->CallVoidMethod(callback, kOnErrorMethodID, *java_exception);
}

void GetAttestationTokens(JNIEnv* env, jclass cls, jobject message_interface,
                          jint num_tokens, jint proxy_layer,
                          jobject attestation_data_callback,
                          jobject signed_token_callback) {
  JavaVM* vm = nullptr;
  jint env_result = env->GetJavaVM(&vm);
  if (env_result != JNI_OK) {
    bsa_log_info("GAT Failed to get Java VM: %d", env_result);
    OnError(env, absl::InvalidArgumentError("AS failed to get Java VM"),
            signed_token_callback);
    return;
  }

  absl::StatusOr<quiche::ProxyLayer> quiche_proxy_layer =
      GetQuicheProxyLayer(proxy_layer);
  if (!quiche_proxy_layer.ok()) {
    OnError(env, quiche_proxy_layer.status(), signed_token_callback);
    return;
  }

  auto bsa_context = std::make_unique<BsaContext>(
      std::make_unique<JavaMessageInterfaceWrapper>(env, message_interface));

  bsa_context->GetBsa()->GetAttestationTokens(
      num_tokens, *quiche_proxy_layer,
      WrapAttestationDataCallback(env, vm, attestation_data_callback),
      WrapSignedTokenCallback(env, vm, signed_token_callback,
                              std::move(bsa_context)));
}

quiche::AttestationDataCallback WrapAttestationDataCallback(
    JNIEnv* env, JavaVM* vm, jobject attestation_data_callback) {
  return [vm, attestation_data_callback =
                  GlobalRef(env, attestation_data_callback)](
             absl::string_view challenge,
             quiche::AttestAndSignCallback attest_and_sign_callback) mutable {
    absl::StatusOr<JNIEnv*> env = helpers::GetCurrentThreadJNIEnv(vm);
    if (!env.ok()) {
      bsa_log_info("AttestationDataCallback failed to get JNIEnv: %s",
                   env.status().ToString());
      return;
    }

    std::string challenge_str(challenge);
    absl::StatusOr<jobject> challenge_byte_array =
        helpers::ByteArrayFromString(*env, challenge_str);
    if (!challenge_byte_array.ok()) {
      std::move(attest_and_sign_callback)(challenge_byte_array.status(),
                                          std::nullopt);
      return;
    }

    // We need to move it to the heap to pass to Java.
    auto callback_ptr = std::make_unique<quiche::AttestAndSignCallback>(
        std::move(attest_and_sign_callback));

    // Now we wrap the C++ provided call back in a Java object, so
    // that Java code can eventually pass it back to us for execution.
    jobject wrapped_callback = (*env)->NewObject(
        kNativeAttestAndSignCallbackClass,
        kNativeAttestAndSignCallbackConstructorID, callback_ptr.get());
    if (wrapped_callback == nullptr) {
      // We failed to create the Java object, so we still own the callback.
      // Call it (which also releases it).
      std::move (*callback_ptr)(
          absl::InternalError("Failed to create NativeAttestAndSignCallback"),
          std::nullopt);
      return;
    }

    // The wrapped callback is now owned by the Java object.
    // So we release the unique pointer's ownership of it now.
    callback_ptr.release();
    (*env)->CallVoidMethod(attestation_data_callback, kOnChallengeDataMethodID,
                           *challenge_byte_array, wrapped_callback);
  };
}

quiche::SignedTokenCallback WrapSignedTokenCallback(
    JNIEnv* env, JavaVM* vm, jobject signed_token_callback,
    std::unique_ptr<BsaContext> bsa_context) {
  return
      // We are moving bsa_context into this lambda *only* to keep it alive
      // until the callback is called.
      [vm, bsa_context = std::move(bsa_context),
       signed_token_callback = GlobalRef(env, signed_token_callback)](
          absl::StatusOr<absl::Span<quiche::BlindSignToken>> response) mutable {
        absl::StatusOr<JNIEnv*> env = helpers::GetCurrentThreadJNIEnv(vm);
        if (!env.ok()) {
          bsa_log_info("SignedTokenCallback failed to get JNIEnv");
          return;
        }
        if (!response.ok()) {
          OnError(*env, response.status(), signed_token_callback);
          return;
        }
        absl::StatusOr<jobject> java_tokens =
            CreateJavaBlindSignTokensList(*env, *response);
        if (!java_tokens.ok()) {
          OnError(*env, java_tokens.status(), signed_token_callback);
          return;
        }
        (*env)->CallVoidMethod(signed_token_callback, kOnSignedTokensMethodID,
                               *java_tokens);
      };
}

absl::StatusOr<jobject> CreateJavaBlindSignToken(JNIEnv* env,
                                                 quiche::BlindSignToken token) {
  // Note: The token.geo_hint field is not used in our use case, so we just
  // ignore it here.

  int64_t expiration_millis =
      absl::ToInt64Milliseconds(token.expiration - absl::UnixEpoch());

  absl::StatusOr<jbyteArray> token_byte_array =
      helpers::ByteArrayFromString(env, token.token);
  if (!token_byte_array.ok()) {
    return token_byte_array.status();
  }

  absl::StatusOr<jobject> token_object =
      env->NewObject(kBlindSignTokenClass, kBlindSignTokenCtorID,
                     *token_byte_array, expiration_millis);
  if (!token_object.ok()) {
    return token_object.status();
  }

  return token_object;
}

absl::StatusOr<jobject> CreateJavaBlindSignTokensList(
    JNIEnv* env, absl::Span<quiche::BlindSignToken> tokens) {
  absl::StatusOr<jobject> tokens_list = helpers::CreateArrayList(env);
  if (!tokens_list.ok()) {
    return tokens_list.status();
  }

  for (const quiche::BlindSignToken& token : tokens) {
    absl::StatusOr<jobject> token_object = CreateJavaBlindSignToken(env, token);
    if (!token_object.ok()) {
      return token_object.status();
    }
    helpers::AddToList(env, *tokens_list, *token_object);
  }
  return tokens_list;
}

void OnResponse(JNIEnv* env, jclass cls, jlong contextPtr, jint statusCode,
                jbyteArray body) {
  quiche::BlindSignMessageCallback callback = std::move(
      *reinterpret_cast<quiche::BlindSignMessageCallback*>(contextPtr));
  absl::StatusOr<std::string> body_str = helpers::ByteArrayToString(env, body);
  if (!body_str.ok()) {
    std::move(callback)(
        absl::InternalError("Failed to convert Java body to string"));
    return;
  }
  std::move(callback)(quiche::BlindSignMessageResponse(
      static_cast<absl::StatusCode>(statusCode), *body_str));
}

void OnResponseError(JNIEnv* env, jclass cls, jlong contextPtr, jint statusCode,
                     jbyteArray message) {
  quiche::BlindSignMessageCallback callback = std::move(
      *reinterpret_cast<quiche::BlindSignMessageCallback*>(contextPtr));
  absl::StatusOr<std::string> message_str =
      helpers::ByteArrayToString(env, message);
  if (!message_str.ok()) {
    std::move(callback)(
        absl::InternalError("Failed to convert Java body to string"));
    return;
  }

  absl::Status status =
      absl::Status(static_cast<absl::StatusCode>(statusCode), *message_str);
  std::move(callback)(status);
}

void ReleaseResponseCallback(JNIEnv* env, jclass cls, jlong contextPtr) {
  auto callback =
      std::move(*reinterpret_cast<absl::AnyInvocable<void(int, std::string)>*>(
          contextPtr));
  // Callback will be dropped.
}

void OnAttestationData(JNIEnv* env, jclass cls, jlong contextPtr,
                       jobjectArray attestationData,
                       jbyteArray tokenChallenge) {
  quiche::AttestAndSignCallback callback =
      std::move(*reinterpret_cast<quiche::AttestAndSignCallback*>(contextPtr));

  std::optional<std::string> token_challenge = std::nullopt;

  if (tokenChallenge != nullptr) {
    absl::StatusOr<std::string> token_challenge_str =
        helpers::ByteArrayToString(env, tokenChallenge);
    if (!token_challenge_str.ok()) {
      std::move(callback)(
          absl::InternalError("Failed to convert token challenge to string"),
          std::nullopt);
      return;
    }
    token_challenge = *token_challenge_str;
  }

  jsize attestation_data_length = env->GetArrayLength(attestationData);

  std::vector<std::string> attestation_data_strs;
  attestation_data_strs.reserve(attestation_data_length);

  for (jsize i = 0; i < attestation_data_length; ++i) {
    jbyteArray attestation_element =
        (jbyteArray)env->GetObjectArrayElement(attestationData, i);

    absl::StatusOr<std::string> item_str =
        helpers::ByteArrayToString(env, attestation_element);

    if (!item_str.ok()) {
      std::move(callback)(
          absl::InternalError(absl::StrFormat(
              "Failed to convert attestation data[%d] to string: %s", i,
              item_str.status().ToString())),
          std::nullopt);
      return;
    }

    attestation_data_strs.push_back(*item_str);
  }

  // When the new callback structure is ready, pass the vector to the callback.
  std::move(callback)(attestation_data_strs, token_challenge);
}

void OnAttestationDataError(JNIEnv* env, jclass cls, jlong contextPtr,
                            jint statusCode, jbyteArray message) {
  quiche::AttestAndSignCallback callback =
      std::move(*reinterpret_cast<quiche::AttestAndSignCallback*>(contextPtr));

  absl::StatusOr<std::string> message_str =
      helpers::ByteArrayToString(env, message);
  if (!message_str.ok()) {
    std::move(callback)(
        absl::InternalError("Failed to convert message to string"),
        std::nullopt);
    return;
  }

  absl::Status status =
      absl::Status(static_cast<absl::StatusCode>(statusCode), *message_str);
  std::move(callback)(status, std::nullopt);
}

void ReleaseAttestationDataCallback(JNIEnv* env, jclass cls, jlong contextPtr) {
  auto callback =
      std::move(*reinterpret_cast<absl::AnyInvocable<void(int, std::string)>*>(
          contextPtr));
  // Callback will be dropped.
}

namespace {}

absl::Status CacheMethodIds(JNIEnv* env) {
  bsa_log_debug("Cache jni_bsa_calls method IDs");

  absl::Status status;
  status = helpers::CacheClasses(
      env,
      {
          {
              .target = &kBlindSignTokenClass,
              .class_name = helpers::BsaClass("$BlindSignToken"),
          },
          {
              .target = &kNativeAttestAndSignCallbackClass,
              .class_name = helpers::BsaClass("$NativeAttestAndSignCallback"),
          },
      });
  if (!status.ok()) return status;

  status = helpers::CacheMethodIds(
      env,
      {
          {
              .target = &kOnChallengeDataMethodID,
              .class_name = helpers::BsaClass("$AttestationDataCallback"),
              .method_name = "onChallengeData",
              .method_signature =
                  "([B" + helpers::BsaSig("$NativeAttestAndSignCallback") +
                  ")V",
          },
          {
              .target = &kOnSignedTokensMethodID,
              .class_name = helpers::BsaClass("$SignedTokenCallback"),
              .method_name = "onSignedTokens",
              .method_signature = "(Ljava/util/List;)V",
          },
          {
              .target = &kOnErrorMethodID,
              .class_name = helpers::BsaClass("$ErrorCallback"),
              .method_name = "onError",
              .method_signature = "(Ljava/lang/Throwable;)V",
          },
          {
              .target = &kBlindSignTokenCtorID,
              .class_name = helpers::BsaClass("$BlindSignToken"),
              .method_name = "<init>",
              .method_signature = "([BJ)V",
          },
          {
              .target = &kNativeAttestAndSignCallbackConstructorID,
              .class_name = helpers::BsaClass("$NativeAttestAndSignCallback"),
              .method_name = "<init>",
              .method_signature = "(J)V",
          },
      });
  if (!status.ok()) return status;

  return absl::OkStatus();
}

absl::Status OnUnload(JNIEnv* env) {
  for (jobject global_ref : {kBlindSignTokenClass}) {
    if (global_ref != nullptr) {
      env->DeleteGlobalRef(global_ref);
      global_ref = nullptr;
    }
  }

  return absl::OkStatus();
}

}  // namespace privateinference::bsa::calls
