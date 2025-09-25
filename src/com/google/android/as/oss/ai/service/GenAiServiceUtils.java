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

package com.google.android.as.oss.ai.service;

import android.os.RemoteException;
import androidx.annotation.Nullable;
import com.google.android.apps.aicore.aidl.AIFeature;
import com.google.android.apps.aicore.aidl.IAICoreService;
import com.google.android.apps.aicore.aidl.ICancellationCallback;
import com.google.android.apps.aicore.aidl.ILLMResultCallback;
import com.google.android.apps.aicore.aidl.ILLMService;
import com.google.android.apps.aicore.aidl.IPrepareInferenceEngineCallback;
import com.google.android.apps.aicore.aidl.ISmartReplyResultCallback;
import com.google.android.apps.aicore.aidl.ISmartReplyService;
import com.google.android.apps.aicore.aidl.ISummarizationResultCallback;
import com.google.android.apps.aicore.aidl.ISummarizationService;
import com.google.android.apps.aicore.aidl.ITarsResultCallback;
import com.google.android.apps.aicore.aidl.ITarsService;
import com.google.android.apps.aicore.aidl.ITextEmbeddingResultCallback;
import com.google.android.apps.aicore.aidl.ITextEmbeddingService;
import com.google.android.apps.aicore.aidl.InferenceError;
import com.google.android.apps.aicore.aidl.LLMRequest;
import com.google.android.apps.aicore.aidl.LLMResult;
import com.google.android.apps.aicore.aidl.SmartReplyRequest;
import com.google.android.apps.aicore.aidl.SmartReplyResult;
import com.google.android.apps.aicore.aidl.SummarizationRequest;
import com.google.android.apps.aicore.aidl.SummarizationResult;
import com.google.android.apps.aicore.aidl.TarsRequest;
import com.google.android.apps.aicore.aidl.TarsResult;
import com.google.android.apps.aicore.aidl.TextEmbeddingRequest;
import com.google.android.apps.aicore.aidl.TextEmbeddingResult;
import com.google.android.as.oss.ai.aidl.PccCancellationCallback;
import com.google.android.as.oss.ai.aidl.PccLlmResultCallback;
import com.google.android.as.oss.ai.aidl.PccLlmService;
import com.google.android.as.oss.ai.aidl.PccSmartReplyResultCallback;
import com.google.android.as.oss.ai.aidl.PccSmartReplyService;
import com.google.android.as.oss.ai.aidl.PccSummarizationResultCallback;
import com.google.android.as.oss.ai.aidl.PccSummarizationService;
import com.google.android.as.oss.ai.aidl.PccTarsResultCallback;
import com.google.android.as.oss.ai.aidl.PccTarsService;
import com.google.android.as.oss.ai.aidl.PccTextEmbeddingResultCallback;
import com.google.android.as.oss.ai.aidl.PccTextEmbeddingService;
import com.google.common.flogger.GoogleLogger;
import java.util.concurrent.Callable;

/** Utility class to host independent helper methods. */
final class GenAiServiceUtils {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();

  public static <T> T checkNonNullAidlResponse(Callable<T> method) throws RemoteException {
    T obj;
    try {
      obj = method.call();
    } catch (Exception e) {
      throw new RemoteException("Callable failed: " + e.getMessage());
    }
    if (obj == null) {
      throw new NullPointerException(
          "Callable returned null object. Maybe PCS<>AICore connection is dead?");
      // Note that not all exceptions propagate to cross-process clients, but NPEs do. See here
      // for the list: https://cs.android.com/android/platform/superproject/+/...
      // .../main:frameworks/base/core/java/android/os/Parcel.java?q=getExceptionCode
    }
    return obj;
  }

  public static PccCancellationCallback createCancellationCallback(
      @Nullable ICancellationCallback callback) {
    return new PccCancellationCallback.Stub() {
      @Override
      public void cancel() throws RemoteException {
        if (callback != null) {
          callback.cancel();
        }
      }
    };
  }

  public static PccSummarizationService createSummarizationServiceStub(
      IAICoreService service, AIFeature feature) throws RemoteException {
    ISummarizationService summarizationService =
        checkNonNullAidlResponse(() -> service.getSummarizationService(feature));
    return new PccSummarizationService.Stub() {
      @Override
      public PccCancellationCallback runCancellableInference(
          SummarizationRequest request, PccSummarizationResultCallback callback)
          throws RemoteException {
        try {
          ICancellationCallback cancellationCallback =
              checkNonNullAidlResponse(
                  () ->
                      summarizationService.runCancellableInference(
                          request,
                          new ISummarizationResultCallback.Stub() {
                            @Override
                            public void onSummarizationInferenceSuccess(
                                SummarizationResult response) throws RemoteException {
                              callback.onSummarizationInferenceSuccess(response);
                            }

                            @Override
                            public void onSummarizationInferenceFailure(int err)
                                throws RemoteException {
                              callback.onSummarizationInferenceFailure(err);
                            }
                          }));
          return createCancellationCallback(cancellationCallback);
        } catch (RemoteException e) {
          // Binder doesn't propagate exceptions across processes.
          logger.atWarning().withCause(e).log("Failed to run summarization service");
          callback.onSummarizationInferenceFailure(InferenceError.IPC_ERROR);
          return createCancellationCallback(null);
        }
      }

      @Override
      public PccCancellationCallback prepareInferenceEngine(
          IPrepareInferenceEngineCallback callback) throws RemoteException {
        try {
          ICancellationCallback cancellationCallback =
              checkNonNullAidlResponse(
                  () ->
                      summarizationService.prepareInferenceEngine(
                          new IPrepareInferenceEngineCallback.Stub() {
                            @Override
                            public void onPreparationSuccess() throws RemoteException {
                              callback.onPreparationSuccess();
                            }

                            @Override
                            public void onPreparationFailure(@InferenceError int err)
                                throws RemoteException {
                              callback.onPreparationFailure(err);
                            }
                          }));
          return createCancellationCallback(cancellationCallback);
        } catch (RemoteException e) {
          // When any of the above fail due to an exception, binder doesn't propagate it across
          // processes, just silently returns a null. We can do slightly better.
          callback.onPreparationFailure(InferenceError.IPC_ERROR);
          // Now that we've indicated a failure via callback, we don't need to throw or return null.
          return createCancellationCallback(null);
        }
      }
    };
  }

  public static PccTarsService createTarsServiceStub(IAICoreService service, AIFeature feature)
      throws RemoteException {
    ITarsService tarsService = checkNonNullAidlResponse(() -> service.getTarsService(feature));
    return new PccTarsService.Stub() {
      @Override
      public PccCancellationCallback runCancellableInference(
          TarsRequest request, PccTarsResultCallback callback) throws RemoteException {
        try {
          ICancellationCallback cancellationCallback =
              checkNonNullAidlResponse(
                  () ->
                      tarsService.runCancellableInference(
                          request,
                          new ITarsResultCallback.Stub() {
                            @Override
                            public void onTarsSuccess(TarsResult response) throws RemoteException {
                              callback.onTarsInferenceSuccess(response);
                            }

                            @Override
                            public void onTarsFailure(@InferenceError int err)
                                throws RemoteException {
                              callback.onTarsInferenceFailure(err);
                            }
                          }));
          return createCancellationCallback(cancellationCallback);
        } catch (RemoteException e) {
          callback.onTarsInferenceFailure(InferenceError.IPC_ERROR);
          return createCancellationCallback(null);
        }
      }
    };
  }

  public static PccTextEmbeddingService createTextEmbeddingServiceStub(
      IAICoreService service, AIFeature feature) throws RemoteException {
    ITextEmbeddingService textEmbeddingService =
        checkNonNullAidlResponse(() -> service.getTextEmbeddingService(feature));
    return new PccTextEmbeddingService.Stub() {
      @Override
      public PccCancellationCallback runCancellableInference(
          TextEmbeddingRequest request, PccTextEmbeddingResultCallback callback)
          throws RemoteException {
        try {
          ICancellationCallback cancellationCallback =
              checkNonNullAidlResponse(
                  () ->
                      textEmbeddingService.runCancellableInference(
                          request,
                          new ITextEmbeddingResultCallback.Stub() {
                            @Override
                            public void onTextEmbeddingInferenceSuccess(
                                TextEmbeddingResult response) throws RemoteException {
                              callback.onTextEmbeddingInferenceSuccess(response);
                            }

                            @Override
                            public void onTextEmbeddingInferenceFailure(int err)
                                throws RemoteException {
                              callback.onTextEmbeddingInferenceFailure(err);
                            }
                          }));
          return createCancellationCallback(cancellationCallback);
        } catch (RemoteException e) {
          // When any of the above fail due to an exception, binder doesn't propagate it across
          // processes, just silently returns a null. We can do slightly better.
          callback.onTextEmbeddingInferenceFailure(InferenceError.IPC_ERROR);
          // Now that we've indicated a failure via callback, we don't need to throw or return null.
          return createCancellationCallback(null);
        }
      }
    };
  }

  public static PccLlmService createLlmServiceStub(IAICoreService service, AIFeature feature)
      throws RemoteException {
    ILLMService llmService = checkNonNullAidlResponse(() -> service.getLLMService(feature));
    return new PccLlmService.Stub() {
      @Override
      public void runInference(LLMRequest request, PccLlmResultCallback callback)
          throws RemoteException {
        try {
          llmService.runInference(
              request,
              new ILLMResultCallback.Stub() {
                @Override
                public void onLLMInferenceSuccess(LLMResult response) throws RemoteException {
                  callback.onLLMInferenceSuccess(response);
                }

                @Override
                public void onLLMInferenceFailure(int err) throws RemoteException {
                  callback.onLLMInferenceFailure(err);
                }
              });
        } catch (RemoteException e) {
          // Binder doesn't propagate exceptions across processes.
          logger.atWarning().withCause(e).log("Failed to run LLM service");
          callback.onLLMInferenceFailure(InferenceError.IPC_ERROR);
        }
      }

      @Override
      public PccCancellationCallback runCancellableInference(
          LLMRequest request, PccLlmResultCallback callback) throws RemoteException {
        try {
          ICancellationCallback cancellationCallback =
              checkNonNullAidlResponse(
                  () ->
                      llmService.runCancellableInference(
                          request,
                          new ILLMResultCallback.Stub() {
                            @Override
                            public void onLLMInferenceSuccess(LLMResult response)
                                throws RemoteException {
                              callback.onLLMInferenceSuccess(response);
                            }

                            @Override
                            public void onLLMInferenceFailure(int err) throws RemoteException {
                              callback.onLLMInferenceFailure(err);
                            }
                          }));
          return createCancellationCallback(cancellationCallback);
        } catch (RemoteException e) {
          // When any of the above fail due to an exception, binder doesn't propagate it across
          // processes, just silently returns a null. We can do slightly better.
          callback.onLLMInferenceFailure(InferenceError.IPC_ERROR);
          // Now that we've indicated a failure via callback, we don't need to throw or return null.
          return createCancellationCallback(null);
        }
      }
    };
  }

  public static PccSmartReplyService createSmartReplyServiceStub(
      IAICoreService service, AIFeature feature) throws RemoteException {
    ISmartReplyService smartReplyService =
        checkNonNullAidlResponse(() -> service.getSmartReplyService(feature));
    return new PccSmartReplyService.Stub() {
      @Override
      public PccCancellationCallback runCancellableInference(
          SmartReplyRequest request, PccSmartReplyResultCallback callback) throws RemoteException {
        try {
          ICancellationCallback cancellationCallback =
              checkNonNullAidlResponse(
                  () ->
                      smartReplyService.runCancellableInference(
                          request,
                          new ISmartReplyResultCallback.Stub() {
                            @Override
                            public void onSmartReplyInferenceSuccess(SmartReplyResult response)
                                throws RemoteException {
                              callback.onSmartReplyInferenceSuccess(response);
                            }

                            @Override
                            public void onSmartReplyInferenceFailure(int err)
                                throws RemoteException {
                              callback.onSmartReplyInferenceFailure(err);
                            }
                          }));
          return createCancellationCallback(cancellationCallback);
        } catch (RemoteException e) {
          // When any of the above fail due to an exception, binder doesn't propagate it across
          // processes, just silently returns a null. We can do slightly better.
          callback.onSmartReplyInferenceFailure(InferenceError.IPC_ERROR);
          // Now that we've indicated a failure via callback, we don't need to throw or return null.
          return createCancellationCallback(null);
        }
      }

      @Override
      public int getApiVersion() throws RemoteException {
        return smartReplyService.getApiVersion();
      }

      @Override
      public PccCancellationCallback prepareInferenceEngine(
          IPrepareInferenceEngineCallback callback) throws RemoteException {
        try {
          ICancellationCallback cancellationCallback =
              checkNonNullAidlResponse(
                  () ->
                      smartReplyService.prepareInferenceEngine(
                          new IPrepareInferenceEngineCallback.Stub() {
                            @Override
                            public void onPreparationSuccess() throws RemoteException {
                              callback.onPreparationSuccess();
                            }

                            @Override
                            public void onPreparationFailure(@InferenceError int err)
                                throws RemoteException {
                              callback.onPreparationFailure(err);
                            }
                          }));
          return createCancellationCallback(cancellationCallback);
        } catch (RemoteException e) {
          // When any of the above fail due to an exception, binder doesn't propagate it across
          // processes, just silently returns a null. We can do slightly better.
          callback.onPreparationFailure(InferenceError.IPC_ERROR);
          // Now that we've indicated a failure via callback, we don't need to throw or return null.
          return createCancellationCallback(null);
        }
      }
    };
  }

  private GenAiServiceUtils() {}
}
