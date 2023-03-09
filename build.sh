#!/bin/bash
# Fail on any error.
set -e

echo "build --enable_platform_specific_config" > ".bazelrc"
echo "build:linux --sandbox_tmpfs_path=/tmp" >> ".bazelrc"

bazel build //src/com/google/android/as/oss:pcs