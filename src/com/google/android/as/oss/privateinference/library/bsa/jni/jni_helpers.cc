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

#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_helpers.h"

#include <jni.h>

#include <algorithm>
#include <string>

#include "java/com/google/android/apps/miphone/pcs/privateinference/library/bsa/jni/jni_logging.h"
#include "third_party/absl/status/status.h"
#include "third_party/absl/status/statusor.h"
#include "third_party/absl/strings/str_cat.h"
#include "third_party/absl/strings/string_view.h"
#include "third_party/absl/types/span.h"

jclass kArrayListClass = nullptr;
jclass kBlindSignAuthJniBridgeClass = nullptr;
jmethodID kArrayListConstructor = nullptr;
jmethodID kCreateStatusExceptionMethodId = nullptr;
jmethodID kListAddMethodID = nullptr;

namespace privateinference::bsa::helpers {

/** Creates a Java ArrayList. */
absl::StatusOr<jobject> CreateArrayList(JNIEnv* env) {
  jobject list = env->NewObject(kArrayListClass, kArrayListConstructor);
  if (list == nullptr) {
    return absl::InternalError("Failed to create ArrayList");
  }
  return list;
}

void AddToList(JNIEnv* env, jobject list, jobject object) {
  env->CallBooleanMethod(list, kListAddMethodID, object);
}

absl::StatusOr<jbyteArray> ByteArrayFromString(JNIEnv* env,
                                               absl::string_view str) {
  jbyte* buffer = new jbyte[str.size()];
  std::copy(str.begin(), str.end(), buffer);

  jbyteArray token_byte_array = env->NewByteArray(str.size());
  if (token_byte_array == nullptr) {
    return absl::InternalError("Failed to create ByteArray");
  }
  env->SetByteArrayRegion(token_byte_array, 0, str.size(), buffer);

  return token_byte_array;
}

absl::StatusOr<std::string> ByteArrayToString(JNIEnv* env,
                                              jbyteArray byteArray) {
  jsize length = env->GetArrayLength(byteArray);
  jbyte* elements = env->GetByteArrayElements(byteArray, /*isCopy=*/nullptr);
  if (elements == nullptr) {
    return absl::InternalError("Failed to get ByteArray elements");
  }
  std::string str(reinterpret_cast<const char*>(elements), length);
  env->ReleaseByteArrayElements(byteArray, elements, JNI_ABORT);
  return str;
}

absl::StatusOr<jobject> CreateStatusException(JNIEnv* env,
                                              absl::Status status) {
  jint canonical_code = static_cast<int>(status.code());
  // absl::string_view data is not guaranteed to be null terminated.
  // So we copy into a std::string that will provided a null-terminated c_str
  std::string message_str(status.message());
  jstring message = env->NewStringUTF(message_str.c_str());
  if (message == nullptr) {
    bsa_log_info("Failed to create exception for %s", status.message());
    return absl::InternalError("Failed to create String for exception");
  }
  jobject exception = env->CallStaticObjectMethod(
      kBlindSignAuthJniBridgeClass, kCreateStatusExceptionMethodId,
      canonical_code, message);
  env->DeleteLocalRef(message);
  return exception;
}

absl::StatusOr<JNIEnv*> GetCurrentThreadJNIEnv(JavaVM* vm) {
  JNIEnv* env = nullptr;
  jint result = vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6);
  if (result != JNI_OK) {
    return absl::InternalError("Failed to get JNIEnv (" +
                               std::to_string(result) + ")");
  }
  return env;
}

absl::Status CacheMethodIds(JNIEnv* env,
                            absl::Span<const MethodSpec> method_spec) {
  for (const MethodSpec& method_spec : method_spec) {
    jclass cls = env->FindClass(method_spec.class_name.c_str());
    if (cls == nullptr) {
      return absl::InternalError(
          absl::StrCat(method_spec.class_name, " not found"));
    }
    jmethodID method_id;
    if (method_spec.is_static) {
      method_id = env->GetStaticMethodID(cls, method_spec.method_name.c_str(),
                                         method_spec.method_signature.c_str());
    } else {
      method_id = env->GetMethodID(cls, method_spec.method_name.c_str(),
                                   method_spec.method_signature.c_str());
    }
    if (method_id == nullptr) {
      return absl::InternalError(
          absl::StrCat(method_spec.class_name, ".", method_spec.method_name,
                       "(", method_spec.method_signature, ") not found"));
    }
    *method_spec.target = method_id;
  }
  return absl::OkStatus();
}

absl::Status CacheClasses(JNIEnv* env,
                          absl::Span<const ClassSpec> class_names) {
  for (const ClassSpec& class_spec : class_names) {
    jclass cls = env->FindClass(class_spec.class_name.c_str());
    if (cls == nullptr) {
      return absl::InternalError(
          absl::StrCat(class_spec.class_name, " not found"));
    }
    jobject global_ref = env->NewGlobalRef(cls);
    if (global_ref == nullptr) {
      return absl::InternalError(absl::StrCat(
          "Failed to create global ref for ", class_spec.class_name));
    }
    *class_spec.target = (jclass)global_ref;
  }
  return absl::OkStatus();
}

absl::Status CacheEnums(JNIEnv* env, absl::Span<const EnumSpec> enum_spec) {
  for (const EnumSpec& enum_spec : enum_spec) {
    jclass cls = env->FindClass(enum_spec.enum_name.c_str());
    if (cls == nullptr) {
      return absl::InternalError(
          absl::StrCat(enum_spec.enum_name, " not found"));
    }

    jfieldID field_id = env->GetStaticFieldID(
        cls, enum_spec.enum_value.c_str(),
        std::string("L" + enum_spec.enum_name + ";").c_str());
    if (field_id == nullptr) {
      return absl::InternalError(absl::StrCat(
          enum_spec.enum_name, ".", enum_spec.enum_value, " not found"));
    }

    jobject enum_value = env->GetStaticObjectField(cls, field_id);
    if (enum_value == nullptr) {
      return absl::InternalError(absl::StrCat(
          enum_spec.enum_name, ".", enum_spec.enum_value, " not found"));
    }
    *enum_spec.target = env->NewGlobalRef(enum_value);
  }
  return absl::OkStatus();
}

absl::Status OnLoad(JNIEnv* env) {
  absl::Status status =
      CacheClasses(env, {
                            {
                                .target = &kArrayListClass,
                                .class_name = "java/util/ArrayList",
                            },
                            {
                                .target = &kBlindSignAuthJniBridgeClass,
                                .class_name = BsaClass(""),
                            },
                        });
  if (!status.ok()) return status;

  status = CacheMethodIds(
      env, {
               {
                   .target = &kListAddMethodID,
                   .class_name = "java/util/List",
                   .method_name = "add",
                   .method_signature = "(Ljava/lang/Object;)Z",
               },

               {
                   .target = &kArrayListConstructor,
                   .class_name = "java/util/ArrayList",
                   .method_name = "<init>",
                   .method_signature = "()V",
               },
               {
                   .target = &kCreateStatusExceptionMethodId,
                   .class_name = BsaClass(""),
                   .method_name = "createStatusException",
                   .method_signature =
                       "(ILjava/lang/String;)Lio/grpc/StatusException;",
                   .is_static = true,
               },
           });

  if (!status.ok()) return status;

  return absl::OkStatus();
}

absl::Status OnUnload(JNIEnv* env) {
  for (jobject global_ref : {kArrayListClass, kBlindSignAuthJniBridgeClass}) {
    if (global_ref != nullptr) {
      env->DeleteGlobalRef(global_ref);
      global_ref = nullptr;
    }
  }

  return absl::OkStatus();
}
}  // namespace privateinference::bsa::helpers
