cmake_minimum_required(VERSION 3.4.1)

set(distribution_DIR ${CMAKE_SOURCE_DIR}/../../../../distribution)

add_library(libsodium STATIC IMPORTED)
set_target_properties(libsodium PROPERTIES IMPORTED_LOCATION
        ${distribution_DIR}/libsodium/lib/${ANDROID_ABI}/libsodium.a)

add_library(sodium SHARED
        sodium.cpp)

target_include_directories(sodium PRIVATE
        ${distribution_DIR}/libsodium/include)

target_link_libraries(sodium
        android
        libsodium)
