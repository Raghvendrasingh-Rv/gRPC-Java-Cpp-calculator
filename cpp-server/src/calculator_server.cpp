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
    
    // Keep server running
    server->Wait();
}

int main(int argc, char** argv) {
    RunServer();
    return 0;
}