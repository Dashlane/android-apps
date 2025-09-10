
#
# Copyright (C) The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

cmake_minimum_required(VERSION 3.4.1)

# configure import libs
set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../distribution)

add_library(lib_ssl STATIC IMPORTED)
set_target_properties(lib_ssl PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/openssl/lib/${ANDROID_ABI}/libssl.a)

add_library(lib_crypto STATIC IMPORTED)
set_target_properties(lib_crypto PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/openssl/lib/${ANDROID_ABI}/libcrypto.a)

add_library(lib_argon2 STATIC IMPORTED)
set_target_properties(lib_argon2 PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/argon2/lib/${ANDROID_ABI}/libargon2.a)

add_library(cryptography SHARED
        cryptography.cpp
        SharingV2Crypto.cpp
        RSACipherUtils.cpp)

target_include_directories(cryptography PRIVATE
        ${distribution_DIR}/openssl/include
        ${distribution_DIR}/argon2/include)

target_link_libraries(cryptography
        android
        lib_ssl
        lib_crypto
        lib_argon2
        log)
