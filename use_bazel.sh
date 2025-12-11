#!/bin/bash
# Sets the current bazel version. If not available on the local system, tries to
# install it.

arch=$(uname | tr '[:upper:]' '[:lower:]')

if [[ -z "$1" ]]; then
  echo "First argument should be the bazel version to use or 'latest'."
  echo "Usage: use_bazel 0.7.0 [--quiet] or use_bazel latest [--quiet]"
  exit 1
fi

ver="$1"
quiet="$2"

if [[ "$quiet" == "--quiet" ]]; then
  CURL_QUIET="-q"
  LOG_TO_DEV_NULL="> /dev/null 2>&1"
fi

curl --help | grep retry-connrefused
if (( $? == 0 )); then
  CURL_RETRY="--retry 5 --retry-max-time 10 --retry-connrefused"
else
  CURL_RETRY="--retry 5 --retry-max-time 10"
fi

if [[ "${ver}" == "latest" ]]; then
  cmd="curl $CURL_QUIET -s $CURL_RETRY \
    https://api.github.com/repos/bazelbuild/bazel/releases \
    | grep -i tag_name | grep -v pre | head -1 | awk '{print \$2}' \
    | awk -F '\"' '{print \$2}'"
  ver=$(eval "$cmd")
  if [[ -z "${ver}" ]]; then
    echo "Failed fetching the latest version number."
    exit 1
  fi
fi

if [[ "${arch}" == "linux" ]]; then
  bazelroot="/usr/local/bazel/${ver}"
elif [[ "${arch}" == "darwin" ]]; then
  bazelroot="/usr/local/lib/bazel-${ver}"
else
  echo "${arch} not supported for this script."
  exit 1
fi

set_bazel() {
  sudo ln -sf "${bazelroot}/bin/bazel" "/usr/local/bin/bazel"
  exit 0
}

if [[ -e "${bazelroot}/bin/bazel" ]]; then
  set_bazel
fi

set -e -x
filename="bazel-${ver}-installer-${arch}-x86_64.sh"
cmd="curl $CURL_QUIET -sLO $CURL_RETRY \
  https://storage.googleapis.com/bazel/${ver}/release/${filename}"
eval "$cmd"
sudo mkdir -p "${bazelroot}"
chmod 755 "${filename}"
command="sudo \"./${filename}\" \"--prefix=${bazelroot}\" $LOG_TO_DEV_NULL"
eval "$command"
rm -f "${filename}"
set_bazel
