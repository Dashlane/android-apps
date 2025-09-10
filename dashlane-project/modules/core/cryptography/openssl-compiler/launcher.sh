#!/bin/bash
set -e
set -x

# Configure openssl
source openssl.config

# Download openssl
./openssl-download.sh

# Verify the NDK version
./ndk-verification.sh

# Build
./openssl-build.sh

# Copy output
cp -a output/. ../distribution/openssl
# Clean up output folder
rm -rf output
# Clean up temp folder
rm -rf temp