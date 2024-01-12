<!--
 Copyright 2023 Google LLC

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

# Protected Download Protocol / API

Protected Download enables downloading of resources to the device with support
for a binary transparency log based verification, ensuring these are the
official resources provided by Google.

This API is used to deliver sensitive models/heuristics to Private Compute Core
apps. The mechanism of download is open-sourced to show that through the
connection to the server personal user data is not sent to Google, but rather
receiving the model or heuristics in an encrypted and verified manner.

As a first use case, this API is used by Google Play Protect Service. As Google
Play Protect Service keeps users safe from malware, the models and heuristics
themselves need to be protected from malware authors.

An extra layer of security that Protected Download provides is the ability to
instantiate a Virtual Machine and use its public key for downloads. The virtual
machine is then transferred to the client application. By having the VM
instantiated within the Protected Download service, it demonstrates that the
public key does not contain any sensitive data.
