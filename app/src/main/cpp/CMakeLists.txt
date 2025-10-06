cmake_minimum_required(VERSION 3.22.1)
project("edgeviewer")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set OpenCV paths
set(OpenCV_INCLUDE ${CMAKE_SOURCE_DIR}/opencv-include)
set(OpenCV_LIB_DIR ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI})

# Find OpenCV library
find_library(opencv-lib opencv_java4 PATHS ${OpenCV_LIB_DIR} NO_CMAKE_FIND_ROOT_PATH)

# Add native source files
add_library(edgeviewer SHARED
        native-lib.cpp
        ImageProcessor.cpp
)

# Include OpenCV headers
target_include_directories(edgeviewer PRIVATE
        ${OpenCV_INCLUDE}
)

# Link libraries
target_link_libraries(edgeviewer
        ${opencv-lib}
        android
        log
        GLESv2
)