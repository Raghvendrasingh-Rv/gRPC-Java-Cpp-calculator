cmake_minimum_required(VERSION 3.10)
project(calculator_server)

set(CMAKE_CXX_STANDARD 17)

# Find ALL Abseil libraries
file(GLOB_RECURSE ABSL_LIBS /usr/local/lib/libabsl*.dylib)

# Debug: Show found libraries
message("Found Abseil libraries: ${ABSL_LIBS}")

# Add executable
add_executable(calculator_server
    src/calculator_server.cpp
    calculator.pb.cc
    calculator.grpc.pb.cc
)

# Include directories
target_include_directories(calculator_server PRIVATE 
    .
    /usr/local/include
)

# Link CORE libraries
target_link_libraries(calculator_server
    # gRPC core
    /usr/local/lib/libgrpc++.dylib
    /usr/local/lib/libgrpc.dylib
    /usr/local/lib/libgpr.dylib
    
    # Protobuf
    /usr/local/lib/libprotobuf.dylib
    
    # System libraries
    pthread
    ssl
    crypto
    z
)

# Link ALL Abseil libraries
foreach(absl_lib ${ABSL_LIBS})
    target_link_libraries(calculator_server ${absl_lib})
endforeach()