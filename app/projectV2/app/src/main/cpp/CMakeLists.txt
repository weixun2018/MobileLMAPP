# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("llama-android")

include(FetchContent)

FetchContent_Declare(
    llama
        GIT_REPOSITORY https://github.com/ggml-org/llama.cpp
    GIT_TAG b6e4ff69b8abd509647b531bd5b4e86950204f66  # 使用特定的提交哈希
)

# Also provides "common"
#FetchContent_MakeAvailable(llama)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.
#
# In this top level CMakeLists.txt, ${CMAKE_PROJECT_NAME} is used to define
# the target library name; in the sub-module's CMakeLists.txt, ${PROJECT_NAME}
# is preferred for the same purpose.
#

#load local llama.cpp
# 下载并配置 llama.cpp
#add_subdirectory(D:/study_space/java_project/tmp/llama.cpp build-llama)
set(LLAMA_CURL OFF CACHE BOOL "Disable CURL support" FORCE)
FetchContent_MakeAvailable(llama)
set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} -DGGML_USE_ACCELERATE=OFF -DGGML_USE_METAL=OFF -DGGML_USE_CUBLAS=OFF")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -DGGML_USE_ACCELERATE=OFF -DGGML_USE_METAL=OFF -DGGML_USE_CUBLAS=OFF")
# In order to load a library into your app from Java/Kotlin, you must call
# System.loadLibrary() and pass the name of the library defined here;
# for GameActivity/NativeActivity derived applications, the same library name must be
# used in the AndroidManifest.xml file.
add_library(${CMAKE_PROJECT_NAME} SHARED
        # List C/C++ source files with relative paths to this CMakeLists.txt.
        llama-android.cpp)

# Specifies libraries CMake should link to your target library. You
# can link libraries from various origins, such as libraries defined in this
# build script, prebuilt third-party libraries, or Android system libraries.
target_link_libraries(${CMAKE_PROJECT_NAME}
        # List libraries link to the target library
        llama
        ggml-base
        ggml-cpu
        ggml
        common
        android
        log)
