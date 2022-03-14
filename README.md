<!--
 Copyright 2021 Google LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

# Private Compute Services

This repository contains the code for the Private Compute Services app, a suite
of services that provide a privacy-preserving bridge between Private Compute
Core and the cloud.

[Android’s Private Compute Core](https://blog.google/products/android/android-12-beta/)
is a secure environment that is isolated from the rest of the operating system
and apps where close-source features can safely access ambient and OS data.

Since Android 11, the OS prevents Private Compute Core components from having
direct communication with other apps, and forces its features to use a small set
of well defined APIs in AOSP. These protections are already open-source and can
be found
[here](https://cs.android.com/android/_/android/platform/packages/modules/Permission/+/efd83ae33345e86dd7e890ab03750aa04d954da1:PermissionController/res/xml/roles.xml;l=668,711,755,797,832;drc=77db87e9fcfaed305c2a4eabe72a66def3f91d11).

From Android 12 forward, features inside Private Compute Core do not have direct
access to the network. Instead, these features communicate over a small set of
APIs to Private Compute Services (defined in this repository). This ensures no
private or identifying information is exposed outside of the device using a set
of privacy preserving technologies including federated learning, federated
analytics, and private information retrieval.

You can learn more about Private Compute Services in this
[blog post](https://security.googleblog.com/2021/09/introducing-androids-private-compute.html).

## Current APIs

*   [Private Information Retrieval](https://en.wikipedia.org/wiki/Private_information_retrieval):
    Enables downloading slices of a dataset without revealing to the server
    which slice it downloaded.
*   Federated compute: Enables privacy-preserving aggregate machine learning and
    analytics across many devices, without any raw data leaving the device.
*   HTTP download: Enables access to static resources like updated ML models.

## Note on dependencies

This project depends on the following separate open sourced repositories, as
well as a limited number of dependencies which remain closed source. All API
definitions are included in the open sourced repos.

The open sourced dependencies are:

*   [Federated compute](https://github.com/google/federated-compute)
*   [Private retrieval](https://github.com/google/private-retrieval)
