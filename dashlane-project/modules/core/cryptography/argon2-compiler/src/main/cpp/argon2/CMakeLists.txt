#
# Copyright (C) 2016 The Android Open Source Project
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

# Build a static lib and copy it into distribution place for independent app to use
# we could not delete the bin directory is because android studio would error out
# when it checks target's existance. Only distribution place is used as import to app

cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_VERBOSE_MAKEFILE on)

add_library(argon2 STATIC
src/blake2/blake2b.c
src/argon2.c
src/core.c
src/encoding.c
src/genkat.c
src/ref.c
src/thread.c
)

# copy out the lib binary... need to leave the static lib around to pass gradle check
set(distribution_DIR ${CMAKE_CURRENT_SOURCE_DIR}/../../../../../distribution)
set_target_properties(argon2
                      PROPERTIES
                      ARCHIVE_OUTPUT_DIRECTORY
                      "${distribution_DIR}/argon2/lib/${ANDROID_ABI}")

# copy out lib header files...
add_custom_command(TARGET argon2 POST_BUILD
                   COMMAND "${CMAKE_COMMAND}" -E
                   make_directory "${distribution_DIR}/argon2/include/")

add_custom_command(TARGET argon2 POST_BUILD
                   COMMAND "${CMAKE_COMMAND}" -E
                   make_directory "${distribution_DIR}/argon2/include/blake2/")

add_custom_command(TARGET argon2 POST_BUILD
                   COMMAND "${CMAKE_COMMAND}" -E
                   copy "${CMAKE_CURRENT_SOURCE_DIR}/src/**.*h"
                   "${distribution_DIR}/argon2/include/")

add_custom_command(TARGET argon2 POST_BUILD
                   COMMAND "${CMAKE_COMMAND}" -E
                   copy "${CMAKE_CURRENT_SOURCE_DIR}/src/blake2/**.*h"
                   "${distribution_DIR}/argon2/include/blake2/")