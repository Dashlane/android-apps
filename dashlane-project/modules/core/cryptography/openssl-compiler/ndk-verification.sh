#!/bin/bash
# Make standalone toolchains
set -e
set -x

source openssl.config

function getMajorVersion() {
  local version=$(echo "$1" | sed 's/r\([0-9]\{1,2\}[a-z]\).*/\1/')
  local major=$(echo "$version" | sed 's/\([0-9]\{1,2\}\).*/\1/')
  echo "$major"
}

# Read Property from PROPERTY_FILE
function getProperty {
   PROP_KEY=$1
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}

# Find current NDK
ANDROID_NDK_ROOT=$(dirname `which ndk-build`) 
if [ -z "$ANDROID_NDK_ROOT" ]; then
	echo "\n Failed to make standalone toolchains"
	exit 1;
else
	echo "Using NDK at ${ANDROID_NDK_ROOT}"
fi
PROPERTY_FILE=${ANDROID_NDK_ROOT}/source.properties

current_ndk_version=$(getProperty "Pkg.Revision")
current_ndk_version_major=$(getMajorVersion ${current_ndk_version})

# Check current NDK version
if [ "$required_ndk_version" != "$current_ndk_version_major" ]; then
	echo "Current NDK version $current_ndk_version_major doesn't match the require NDK version $required_ndk_version" | tee -a ${OPENSSL_BUILD_LOG}
	exit 1;
else
	echo "NDK version meets required version" | tee -a ${OPENSSL_BUILD_LOG}
fi

# Make standalone toolchains
# make_standalone_toolchain.py is no longer necessary. The
# $NDK/toolchains/llvm/prebuilt/darwin-x86_64/bin directory contains target-specific scripts that perform the same task