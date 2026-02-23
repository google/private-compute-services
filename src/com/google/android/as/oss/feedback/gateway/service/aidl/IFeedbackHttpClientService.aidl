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

package com.google.android.as.oss.feedback.gateway.service.aidl;

import com.google.android.as.oss.feedback.gateway.service.aidl.IFeedbackResultCallback;

/**
 * Service interface for uploading feedback data via an HTTP client.
 * This interface defines methods for interacting with a feedback service,
 * specifically for uploading feedback information.
 */
interface IFeedbackHttpClientService {

    /**
     * Uploads the feedback data using the PCS gateway to Apex backend.
     *
     * @param feedbackData The feedback data to send.
     * @param callback Callback to notify the result of the operation.
     */
    oneway void uploadFeedback(in Bundle feedbackData, IFeedbackResultCallback callback);
}
