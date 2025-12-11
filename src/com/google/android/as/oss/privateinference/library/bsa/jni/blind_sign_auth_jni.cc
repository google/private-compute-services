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

#include <jni.h>

#include <cstddef>
#include <string>

#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_bsa_calls.h"
#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_helpers.h"
#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_logging.h"
#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_message_interface_wrapper.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/strings/string_view.h"

namespace privateinference::bsa {
static absl::Status InternalOnUnload(JavaVM* vm);
static absl::Status InternalOnLoad(JavaVM* vm);
static absl::Status RegisterBlindSignAuthMethods(JNIEnv* env);
static absl::Status RegisterNativeOnResponseMethods(JNIEnv* env);
static absl::Status RegisterNativeAttestAndSignCallbackMethods(JNIEnv* env);
}  // namespace privateinference::bsa

JNIEXPORT jint JNI_OnLoad(JavaVM* vm, void* reserved) {
  absl::Status status = privateinference::bsa::InternalOnLoad(vm);
  if (!status.ok()) {
    bsa_log_info("OnLoad failed: %s", status.message());
    return -1;
  }
  return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM* vm, void* reserved) {
  absl::Status status = privateinference::bsa::InternalOnUnload(vm);
  if (!status.ok()) {
    bsa_log_info("OnUnload failed: %s", status.message());
  }
}

// Implementation details for the OnLoad/OnUnload methods.
namespace privateinference::bsa {
static absl::Status InternalOnLoad(JavaVM* vm) {
  JNIEnv* env = nullptr;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
    return absl::InternalError("Failed to get JNIEnv");
  }

  absl::Status status;

  status = calls::CacheMethodIds(env);
  if (!status.ok()) return status;

  status = helpers::OnLoad(env);
  if (!status.ok()) return status;

  status = RegisterBlindSignAuthMethods(env);
  if (!status.ok()) return status;

  status = RegisterNativeOnResponseMethods(env);
  if (!status.ok()) return status;

  status = RegisterNativeAttestAndSignCallbackMethods(env);
  if (!status.ok()) return status;

  status = calls::JavaMessageInterfaceWrapper::CacheMethodIds(env);
  if (!status.ok()) return status;

  return absl::OkStatus();
}

struct JNINativeMethodSpec {
  const std::string name;
  const std::string signature;
  void* fn_ptr;
};

template <std::size_t N>
static absl::Status RegisterNativeMethods(
    JNIEnv* env, const std::string& class_name,
    JNINativeMethodSpec (&method_specs)[N]) {
  JNINativeMethod native_methods[N];
  for (int i = 0; i < N; ++i) {
    native_methods[i] = {
        .name = (char*)method_specs[i].name.c_str(),
        .signature = (char*)method_specs[i].signature.c_str(),
        .fnPtr = method_specs[i].fn_ptr,
    };
  }

  jclass cls = env->FindClass(class_name.c_str());

  if (cls == nullptr) {
    return absl::InternalError("BlindSignAuth class not found");
  }

  jint error = env->RegisterNatives(
      cls, native_methods, sizeof(native_methods) / sizeof(native_methods[0]));
  if (error != 0) {
    return absl::InternalError("Failed to register BlindSignAuth methods");
  }

  return absl::OkStatus();
}

static absl::Status RegisterBlindSignAuthMethods(JNIEnv* env) {
  bsa_log_debug("Register BSA methods");
  JNINativeMethodSpec specs[] = {
      {
          .name = "nativeGetAttestationTokens",
          .signature = "(" + helpers::BsaSig("$BridgeMessageInterface") + "II" +
                       helpers::BsaSig("$AttestationDataCallback") +
                       helpers::BsaSig("$SignedTokenCallback") + ")V",
          .fn_ptr = (void*)&calls::GetAttestationTokens,
      },
  };

  return RegisterNativeMethods(env, helpers::BsaClass(""), specs);
}

static absl::Status RegisterNativeOnResponseMethods(JNIEnv* env) {
  bsa_log_debug("Register NativeOnResponse methods");
  JNINativeMethodSpec specs[] = {
      {
          .name = "nativeOnResponse",
          .signature = "(JI[B)V",
          .fn_ptr = (void*)&calls::OnResponse,
      },
      {
          .name = "nativeOnResponseError",
          .signature = "(JI[B)V",
          .fn_ptr = (void*)&calls::OnResponseError,
      },
      {
          .name = "nativeRelease",
          .signature = "(J)V",
          .fn_ptr = (void*)&calls::ReleaseResponseCallback,
      },
  };
  return RegisterNativeMethods(env, helpers::BsaClass("$NativeMessageCallback"),
                               specs);
}

static absl::Status RegisterNativeAttestAndSignCallbackMethods(JNIEnv* env) {
  bsa_log_debug("Register NativeAttestAndSignCallback methods");
  JNINativeMethodSpec specs[] = {
      {
          .name = "nativeOnAttestationData",
          .signature = "(J[[B[B)V",
          .fn_ptr = (void*)&calls::OnAttestationData,
      },
      {
          .name = "nativeOnAttestationDataError",
          .signature = "(JI[B)V",
          .fn_ptr = (void*)&calls::OnAttestationDataError,
      },
      {
          .name = "nativeRelease",
          .signature = "(J)V",
          .fn_ptr = (void*)&calls::ReleaseAttestationDataCallback,
      },
  };

  return RegisterNativeMethods(
      env, helpers::BsaClass("$NativeAttestAndSignCallback"), specs);
}

static absl::Status InternalOnUnload(JavaVM* vm) {
  JNIEnv* env = nullptr;
  if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
    return absl::InternalError("Failed to get JNIEnv");
  }
  absl::Status status = helpers::OnUnload(env);
  if (!status.ok()) return status;

  status = privateinference::bsa::calls::OnUnload(env);
  if (!status.ok()) return status;

  status = privateinference::bsa::helpers::OnUnload(env);
  if (!status.ok()) return status;

  return absl::OkStatus();
}
}  // namespace privateinference::bsa
