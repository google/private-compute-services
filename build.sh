#!/bin/bash
# Fail on any error.
set -e

echo "build --enable_platform_specific_config" > ".bazelrc"
echo "build:linux --sandbox_tmpfs_path=/tmp" >> ".bazelrc"

# Install Bazel
use_bazel.sh 6.1.0
bazel version

bazel build //src/com/google/android/as/oss:pcs