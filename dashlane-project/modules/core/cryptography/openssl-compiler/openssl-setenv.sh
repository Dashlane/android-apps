NDK=$1

# Set compiler clang, instead of fcc  by default
CC=clang

# Set ANDROID_NDK_HOME to standlone toolchain for arch
export ANDROID_NDK_HOME=$NDK

toolchains_path=$(python toolchains_path.py --ndk ${ANDROID_NDK_HOME})

# Add toolchains bin directory to PATH
PATH=$toolchains_path/bin:$PATH