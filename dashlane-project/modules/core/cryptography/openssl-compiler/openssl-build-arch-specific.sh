#!/bin/bash
set -e
set -x

# Get android Abi name from $arch
function getAndroidAbiName {
	case $1 in
		"armeabi-v7a")
			echo "android-arm"
			;;
		"arm64-v8a")
			echo "android-arm64"
			;;
		*)
			echo "android-$1"
			;;
	esac
}

android_arch=$1
arch=$(getAndroidAbiName $android_arch)

if [ -z $arch ]; then
	echo "No commandline arguments, please provide an architecture to build for"
	exit 1;
fi

echo "Reading OpenSSL config...." >&2
source openssl.config
echo $OPENSSL_BUILD_LOG

# Set environment
ANDROID_NDK_ROOT=$(dirname `which ndk-build`) 
source ./openssl-setenv.sh ${ANDROID_NDK_ROOT}
echo $PATH

#copy the includes for SSL to output
mkdir -p output/include
cd temp/openssl
cd ${OPENSSL_DIR}

# Configure Openssl
./Configure shared no-comp no-hw no-asm ${arch} -D__ANDROID_API__=$target_api
# Cleanup
echo "cleanup before ${arch}" 
make clean
# Build
make

# Copying output to appropriate folder
echo "copying libs to outupt for ${arch}"
cp -RL ./include/openssl $SCRIPTPATH/output/include

mkdir -p ${SCRIPTPATH}/output/lib/${android_arch}

cp libcrypto.a ${SCRIPTPATH}/output/lib/${android_arch}
cp libssl.a ${SCRIPTPATH}/output/lib/${android_arch}
# We don't need so files
# cp libcrypto.so ${SCRIPTPATH}/output/lib/${android_arch}
# cp libssl.so ${SCRIPTPATH}/output/lib/${android_arch}

echo "Done \o/ for ${arch}!" | tee -a ${OPENSSL_BUILD_LOG}