/*
 * Copyright 2023 Google LLC
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

package com.google.android.as.oss.common.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.VisibleForTesting;
import com.google.common.flogger.GoogleLogger;

/**
 * BroadcastReceiver to listen on boot completed event, such that {@link
 * com.google.android.as.oss.PrivateComputeServicesApplication#onCreate()} is invoked, which would
 * initialize PCS application resources like federatedCompute etc. in cases where the app would not
 * be launched otherwise.
 */
public final class BootCompletedBroadcastReceiver extends BroadcastReceiver {
  private static final GoogleLogger logger = GoogleLogger.forEnclosingClass();
  @VisibleForTesting static boolean receiverInvoked;

  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();
    if (action == null) {
      return;
    }

    receiverInvoked = true; // only for testing
    logger.atInfo().log("Received intent: %s", action);
  }
}
