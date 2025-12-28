gRPC Calculator Project Setup Guide
📋 Prerequisites Installation
1. Install Protocol Buffers (protoc)
bash
# macOS
brew install protobuf

# Ubuntu/Debian
sudo apt-get update
sudo apt-get install -y protobuf-compiler

# Verify installation
protoc --version
# Should show: libprotoc 3.x.x or higher
2. Install gRPC and Dependencies
bash
# macOS
brew install grpc abseil c-ares re2 upb

# Ubuntu/Debian
sudo apt-get install -y \
    libgrpc++-dev \
    libgrpc-dev \
    libabsl-dev \
    libc-ares-dev \
    libre2-dev \
    libupb-dev

# Verify gRPC installation
which grpc_cpp_plugin
# Should show: /usr/local/bin/grpc_cpp_plugin (macOS) or /usr/bin/grpc_cpp_plugin (Linux)
3. Install Build Tools
bash
# macOS
brew install cmake pkg-config autoconf automake libtool

# Ubuntu/Debian
sudo apt-get install -y \
    build-essential \
    cmake \
    pkg-config \
    autoconf \
    automake \
    libtool

# Verify CMake
cmake --version
# Should show: cmake version 3.10 or higher
4. Install Java and Maven (for Java client)
bash
# macOS
brew install openjdk@11 maven

# Ubuntu/Debian
sudo apt-get install -y openjdk-11-jdk maven

# Set JAVA_HOME (macOS)
echo 'export JAVA_HOME=$(/usr/libexec/java_home)' >> ~/.zshrc
source ~/.zshrc

# Verify installations
java -version
mvn -version
🏗️ Project Setup
1. Create Project Structure
bash
# Create project directory
mkdir calculator-grpc-project
cd calculator-grpc-project

# Create directory structure
mkdir -p proto cpp-server/src java-client/src/main/java/com/calculator/client
2. Create Protocol Buffer Definition
Create proto/calculator.proto:

proto
syntax = "proto3";

package calculator;

message OperationRequest {
  double number1 = 1;
  double number2 = 2;
}

message OperationResponse {
  double result = 1;
}

service CalculatorService {
  rpc Add(OperationRequest) returns (OperationResponse) {}
  rpc Subtract(OperationRequest) returns (OperationResponse) {}
}
⚙️ C++ Server Setup
1. Generate C++ gRPC Code
bash
cd cpp-server

# Generate protobuf and gRPC code
protoc --cpp_out=. --grpc_out=. \
    --plugin=protoc-gen-grpc=/usr/local/bin/grpc_cpp_plugin \
    -I../proto ../proto/calculator.proto

# Verify files were generated
ls -la *.pb.*
# Should see: calculator.pb.cc, calculator.pb.h, calculator.grpc.pb.cc, calculator.grpc.pb.h
2. Create C++ Server Implementation
Create cpp-server/src/calculator_server.cpp:

cpp
#include <iostream>
#include <memory>
#include <string>
#include <grpcpp/grpcpp.h>
#include "calculator.grpc.pb.h"

using grpc::Server;
using grpc::ServerBuilder;
using grpc::ServerContext;
using grpc::Status;
using calculator::OperationRequest;
using calculator::OperationResponse;
using calculator::CalculatorService;

class CalculatorServiceImpl final : public CalculatorService::Service {
public:
    Status Add(ServerContext* context, const OperationRequest* request,
               OperationResponse* reply) override {
        double result = request->number1() + request->number2();
        reply->set_result(result);
        std::cout << "[Server] Add: " << request->number1() 
                  << " + " << request->number2() 
                  << " = " << result << std::endl;
        return Status::OK;
    }

    Status Subtract(ServerContext* context, const OperationRequest* request,
                    OperationResponse* reply) override {
        double result = request->number1() - request->number2();
        reply->set_result(result);
        std::cout << "[Server] Subtract: " << request->number1() 
                  << " - " << request->number2() 
                  << " = " << result << std::endl;
        return Status::OK;
    }
};

void RunServer() {
    std::string server_address("0.0.0.0:50051");
    CalculatorServiceImpl service;

    ServerBuilder builder;
    builder.AddListeningPort(server_address, grpc::InsecureServerCredentials());
    builder.RegisterService(&service);

    std::unique_ptr<Server> server(builder.BuildAndStart());
    std::cout << "[Server] Listening on " << server_address << std::endl;
    server->Wait();
}

int main(int argc, char** argv) {
    RunServer();
    return 0;
}
3. Create CMakeLists.txt
Create cpp-server/CMakeLists.txt:

cmake
cmake_minimum_required(VERSION 3.10)
project(calculator_server)

set(CMAKE_CXX_STANDARD 17)

# Add executable
add_executable(calculator_server
    src/calculator_server.cpp
    calculator.pb.cc
    calculator.grpc.pb.cc
)

# Include directories
target_include_directories(calculator_server PRIVATE 
    ${CMAKE_CURRENT_SOURCE_DIR}
    /usr/local/include
)

# Link libraries
target_link_libraries(calculator_server
    /usr/local/lib/libgrpc++.dylib
    /usr/local/lib/libgrpc.dylib
    /usr/local/lib/libgpr.dylib
    /usr/local/lib/libprotobuf.dylib
    pthread
    ssl
    crypto
    z
)
4. Build C++ Server
bash
# Build with CMake
mkdir -p build
cd build
cmake ..
make

# Verify build
ls -la calculator_server
☕ Java Client Setup
1. Create Maven Project
Create java-client/pom.xml:

xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.calculator</groupId>
    <artifactId>grpc-client</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <grpc.version>1.76.0</grpc.version>
        <protobuf.version>3.25.3</protobuf.version>
    </properties>

    <dependencies>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-netty-shaded</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-protobuf</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>io.grpc</groupId>
            <artifactId>grpc-stub</artifactId>
            <version>${grpc.version}</version>
        </dependency>
        <dependency>
            <groupId>javax.annotation</groupId>
            <artifactId>javax.annotation-api</artifactId>
            <version>1.3.2</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                    <release>11</release>
                </configuration>
            </plugin>
            
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
                    <protoSourceRoot>${project.basedir}/../proto</protoSourceRoot>
                    <outputDirectory>${project.build.directory}/generated-sources/protobuf</outputDirectory>
                    <clearOutputDirectory>false</clearOutputDirectory>
                    <protocArtifact>com.google.protobuf:protoc:${protobuf.version}:exe:${os.detected.classifier}</protocArtifact>
                    <pluginId>grpc-java</pluginId>
                    <pluginArtifact>io.grpc:protoc-gen-grpc-java:${grpc.version}:exe:${os.detected.classifier}</pluginArtifact>
                </configuration>
                <executions>
                    <execution>
                        <goals>
                            <goal>compile</goal>
                            <goal>compile-custom</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>
2. Generate Java gRPC Code
bash
cd java-client

# Copy proto file to accessible location
mkdir -p src/main/proto
cp ../proto/calculator.proto src/main/proto/

# Generate Java gRPC code
mvn protobuf:compile protobuf:compile-custom

# Verify files were generated
find target -name "*.java" | grep -i calculator
# Should see: CalculatorServiceGrpc.java and Calculator.java
3. Create Java Client
Create java-client/src/main/java/com/calculator/client/SimpleTestClient.java:

java
package com.calculator.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import calculator.CalculatorServiceGrpc;
import calculator.OperationRequest;
import calculator.OperationResponse;

public class SimpleTestClient {
    public static void main(String[] args) {
        System.out.println("=== Simple gRPC Calculator Test ===");
        
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        
        CalculatorServiceGrpc.CalculatorServiceBlockingStub stub = 
            CalculatorServiceGrpc.newBlockingStub(channel);
        
        try {
            // Test 1: Add
            System.out.println("Testing addition...");
            OperationRequest addRequest = OperationRequest.newBuilder()
                .setNumber1(10.5)
                .setNumber2(5.2)
                .build();
            OperationResponse addResponse = stub.add(addRequest);
            System.out.println("10.5 + 5.2 = " + addResponse.getResult());
            
            // Test 2: Subtract
            System.out.println("Testing subtraction...");
            OperationRequest subRequest = OperationRequest.newBuilder()
                .setNumber1(20.0)
                .setNumber2(7.5)
                .build();
            OperationResponse subResponse = stub.subtract(subRequest);
            System.out.println("20.0 - 7.5 = " + subResponse.getResult());
            
            System.out.println("\n✅ All tests passed!");
            
        } catch (Exception e) {
            System.err.println("Test failed: " + e.getMessage());
            System.err.println("Make sure the C++ server is running!");
            e.printStackTrace();
        } finally {
            channel.shutdown();
        }
    }
}
4. Compile Java Client
bash
# Compile
mvn compile

# If compilation fails due to missing generated files:
mvn clean compile
🚀 Running the Application
Terminal 1: Start C++ Server
bash
cd calculator-grpc-project/cpp-server/build
./calculator_server
Expected output:

text
[Server] Listening on 0.0.0.0:50051
Terminal 2: Run Java Client
bash
cd calculator-grpc-project/java-client
mvn exec:java -Dexec.mainClass="com.calculator.client.SimpleTestClient"
Expected output:

text
=== Simple gRPC Calculator Test ===
Testing addition...
10.5 + 5.2 = 15.7
Testing subtraction...
20.0 - 7.5 = 12.5

✅ All tests passed!
🔧 Troubleshooting Common Issues
1. Missing gRPC Libraries Error
text
Undefined symbols for architecture x86_64:
  "absl::lts_20250814::log_internal::LogMessage::OstreamView::stream()", ...
Solution:

bash
# Install Abseil
brew install abseil

# Rebuild C++ server
cd cpp-server/build
rm -rf *
cmake ..
make
2. Protoc Version Mismatch
text
Protobuf C++ gencode is built with an incompatible version of
Solution:

bash
# Remove old protoc
brew uninstall protobuf
brew cleanup

# Install fresh
brew install protobuf

# Regenerate files
cd cpp-server
rm -f *.pb.*
protoc --cpp_out=. --grpc_out=. \
    --plugin=protoc-gen-grpc=/usr/local/bin/grpc_cpp_plugin \
    -I../proto ../proto/calculator.proto
3. Java gRPC Files Not Generated
text
package CalculatorServiceGrpc does not exist
Solution:

bash
cd java-client

# Generate Java gRPC code
mvn protobuf:compile protobuf:compile-custom

# Verify generation
find target -name "*Calculator*.java"

# If still not generated, check proto file location
ls -la ../proto/calculator.proto
4. CMake Can't Find Libraries
text
CMake Error: Could NOT find gRPC (missing: gRPC_INCLUDE_DIR)
Solution:

bash
# Update CMakeLists.txt to use absolute paths
# Add these lines to CMakeLists.txt:
include_directories(/usr/local/include)
link_directories(/usr/local/lib)
5. Connection Refused
text
io.grpc.StatusRuntimeException: UNAVAILABLE: io exception
Solution:

bash
# Check if server is running
ps aux | grep calculator_server

# Check port 50051
lsof -i :50051

# Test connection
telnet localhost 50051
📝 Environment Variables (Optional)
bash
# Add to ~/.zshrc or ~/.bash_profile
export PATH="/usr/local/bin:$PATH"
export JAVA_HOME=$(/usr/libexec/java_home)  # macOS only
export PROTOBUF_ROOT="/usr/local"
export GRPC_ROOT="/usr/local"

# Reload shell
source ~/.zshrc
✅ Verification Script
Create verify_setup.sh:

bash
#!/bin/bash
echo "=== Setup Verification ==="
echo "1. protoc: $(which protoc) - $(protoc --version 2>/dev/null || echo 'Not found')"
echo "2. grpc_cpp_plugin: $(which grpc_cpp_plugin 2>/dev/null || echo 'Not found')"
echo "3. Java: $(java -version 2>&1 | head -1)"
echo "4. Maven: $(mvn -version 2>&1 | head -1)"
echo "5. CMake: $(cmake --version 2>&1 | head -1)"
echo "=== Libraries ==="
ls /usr/local/lib/libgrpc*.dylib 2>/dev/null | head -3 || \
ls /usr/lib/x86_64-linux-gnu/libgrpc*.so 2>/dev/null | head -3 || \
echo "No gRPC libraries found"
echo "=== Done ==="
Make executable and run:

bash
chmod +x verify_setup.sh
./verify_setup.sh
🎯 Quick Reference Commands
Task	Command
Generate C++ gRPC code	protoc --cpp_out=. --grpc_out=. --plugin=protoc-gen-grpc=/usr/local/bin/grpc_cpp_plugin -I../proto ../proto/calculator.proto
Generate Java gRPC code	mvn protobuf:compile protobuf:compile-custom
Build C++ server	mkdir build && cd build && cmake .. && make
Compile Java client	mvn compile
Run C++ server	./calculator_server
Run Java client	mvn exec:java -Dexec.mainClass="com.calculator.client.SimpleTestClient"
Note: Paths like /usr/local/bin/grpc_cpp_plugin are for macOS. On Linux, use /usr/bin/grpc_cpp_plugin or the appropriate path for your distribution.
