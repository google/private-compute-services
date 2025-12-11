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

#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_message_interface_wrapper.h"

#include <jni.h>

#include <optional>
#include <string>
#include <utility>

#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_helpers.h"
#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_logging.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/quiche/blind_sign_auth/blind_sign_message_interface.h"

jmethodID kDoRequestMethodID;
jclass kNativeMessageCallbackClass;
jmethodID kResponseCallbackConstructorID;
jobject kBlindSignMessageRequestTypeInitialData;
jobject kBlindSignMessageRequestTypeAuthAndSign;
jobject kBlindSignMessageRequestTypeAttestAndSign;

namespace privateinference::bsa::calls {

jobject GetBlindSignMessageRequestType(
    quiche::BlindSignMessageRequestType request_type) {
  switch (request_type) {
    case quiche::BlindSignMessageRequestType::kGetInitialData:
      return kBlindSignMessageRequestTypeInitialData;
    case quiche::BlindSignMessageRequestType::kAuthAndSign:
      return kBlindSignMessageRequestTypeAuthAndSign;
    case quiche::BlindSignMessageRequestType::kAttestAndSign:
      return kBlindSignMessageRequestTypeAttestAndSign;
    default:
      return nullptr;
  }
}

JavaMessageInterfaceWrapper::JavaMessageInterfaceWrapper(
    JNIEnv* env, jobject java_message_interface)
    : java_message_interface_(env, env->NewGlobalRef(java_message_interface)) {
  jint vm_result = env->GetJavaVM(&vm_);
  if (vm_result != JNI_OK) {
    bsa_log_info("Failed to get Java VM: %d", vm_result);
    return;
  }
}

void JavaMessageInterfaceWrapper::DoRequest(
    quiche::BlindSignMessageRequestType request_type,
    std::optional<absl::string_view> authorization_header,
    const std::string& body, quiche::BlindSignMessageCallback callback) {
  absl::StatusOr<JNIEnv*> env = helpers::GetCurrentThreadJNIEnv(vm_);
  if (!env.ok()) {
    std::move(callback)(env.status());
    return;
  }

  absl::StatusOr<jbyteArray> message_byte_array =
      bsa::helpers::ByteArrayFromString(*env, body);
  if (!message_byte_array.ok()) {
    std::move(callback)(message_byte_array.status());
    return;
  }

  // We need to move it to the heap to pass to Java.
  auto callback_ptr = new quiche::BlindSignMessageCallback(std::move(callback));

  // Now we wrap the C++ provided call back in a Java object, so that
  // Java code can eventually pass it back to us for execution.
  jobject wrapped_callback =
      (*env)->NewObject(kNativeMessageCallbackClass,
                        kResponseCallbackConstructorID, callback_ptr);
  if (wrapped_callback == nullptr) {
    std::move (*callback_ptr)(
        absl::InternalError("Failed to create native callback wrapper"));
    return;
  }

  jobject request_type_object = GetBlindSignMessageRequestType(request_type);
  if (request_type_object == nullptr) {
    std::move (*callback_ptr)(
        absl::InternalError("Failed to get BlindSignMessageRequestType"));
    return;
  }

  (*env)->CallVoidMethod(java_message_interface_, kDoRequestMethodID,
                         /*request_type=*/request_type_object,
                         /*authorization_header=*/nullptr,
                         /*message=*/*message_byte_array,
                         /*callback=*/wrapped_callback);
}

absl::Status JavaMessageInterfaceWrapper::CacheMethodIds(JNIEnv* env) {
  bsa_log_debug("Cache jni_message_interface_wrapper method IDs");

  absl::Status status;

  status = helpers::CacheClasses(
      env, {
               {
                   .target = &kNativeMessageCallbackClass,
                   .class_name = helpers::BsaClass("$NativeMessageCallback"),
               },
           });
  if (!status.ok()) return status;

  status = helpers::CacheMethodIds(
      env, {{
                .target = &kDoRequestMethodID,
                .class_name = helpers::BsaClass("$BridgeMessageInterface"),
                .method_name = "doRequest",
                .method_signature =
                    "(" + helpers::BsaSig("$BlindSignMessageRequestType") +
                    "[B[B" + helpers::BsaSig("$NativeMessageCallback") + ")V",
            },
            {
                .target = &kResponseCallbackConstructorID,
                .class_name = helpers::BsaClass("$NativeMessageCallback"),
                .method_name = "<init>",
                .method_signature = "(J)V",
            }});
  if (!status.ok()) return status;

  status = helpers::CacheEnums(
      env, {{
                .target = &kBlindSignMessageRequestTypeInitialData,
                .enum_name = helpers::BsaClass("$BlindSignMessageRequestType"),
                .enum_value = "GET_INITIAL_DATA",
            },
            {
                .target = &kBlindSignMessageRequestTypeAuthAndSign,
                .enum_name = helpers::BsaClass("$BlindSignMessageRequestType"),
                .enum_value = "AUTH_AND_SIGN",
            },
            {
                .target = &kBlindSignMessageRequestTypeAttestAndSign,
                .enum_name = helpers::BsaClass("$BlindSignMessageRequestType"),
                .enum_value = "ATTEST_AND_SIGN",
            }});
  if (!status.ok()) return status;

  return absl::OkStatus();
}

absl::Status JavaMessageInterfaceWrapper::OnUnload(JNIEnv* env) {
  for (jobject global_ref : {(jobject)kNativeMessageCallbackClass,
                             kBlindSignMessageRequestTypeAuthAndSign,
                             kBlindSignMessageRequestTypeInitialData}) {
    if (global_ref != nullptr) {
      env->DeleteGlobalRef(global_ref);
      global_ref = nullptr;
    }
  }

  return absl::OkStatus();
}

}  // namespace privateinference::bsa::calls
