#!/bin/bash
set -e

read -p "You are about to run the script to build PCS apk. Please note that \
this script works only on Debian and may install some common binaries on your \
machine. We advise that you use a clean VM / container to run this script. \
Please press y to proceed: " -n 1 -r USER_REPLY
echo 
if [[ ! $USER_REPLY =~ ^[Yy]$ ]]
then
    [[ "$0" = "${BASH_SOURCE[0]}" ]] && exit 1 || return 1 
fi

# Install Android SDKs needed by google_bazel_common
sudo apt-get update && sudo apt-get install wget unzip default-jdk build-essential git

readonly TMPDIR=pcs-androidsdk
export ANDROID_SDK_PATH=${TMPDIR}/sdk
export ANDROID_HOME=${TMPDIR}/sdk
wget --directory-prefix="$TMPDIR"/download https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip
unzip -d "$TMPDIR" "$TMPDIR"/download/*.zip
yes | "${TMPDIR}"/cmdline-tools/bin/sdkmanager --install "platforms;android-33" --sdk_root="${ANDROID_HOME}"
yes | "${TMPDIR}"/cmdline-tools/bin/sdkmanager --install "build-tools;33.0.1" --sdk_root="${ANDROID_HOME}"

echo "build --enable_platform_specific_config" > ".bazelrc"
echo "build:linux --sandbox_tmpfs_path=/tmp" >> ".bazelrc"

# Install Bazel
./use_bazel.sh 6.1.1
bazel build //src/com/google/android/as/oss:pcs