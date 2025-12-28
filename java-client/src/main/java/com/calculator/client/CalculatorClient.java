package com.calculator.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import calculator.CalculatorServiceGrpc;
import calculator.Calculator.OperationRequest;
import calculator.Calculator.OperationResponse;

import java.util.Scanner;

public class CalculatorClient {
    private final ManagedChannel channel;
    private final CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub;

    /** Construct client connecting to server at {@code host:port}. */
    public CalculatorClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()  // No encryption for simplicity
                .build());
    }

    /** Construct client using existing channel. */
    public CalculatorClient(ManagedChannel channel) {
        this.channel = channel;
        blockingStub = CalculatorServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown();
    }

    /** Add two numbers */
    public double add(double a, double b) {
        OperationRequest request = OperationRequest.newBuilder()
                .setNumber1(a)
                .setNumber2(b)
                .build();
        OperationResponse response;
        try {
            response = blockingStub.add(request);
            return response.getResult();
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            throw new RuntimeException("RPC failed", e);
        }
    }

    /** Subtract two numbers */
    public double subtract(double a, double b) {
        OperationRequest request = OperationRequest.newBuilder()
                .setNumber1(a)
                .setNumber2(b)
                .build();
        OperationResponse response;
        try {
            response = blockingStub.subtract(request);
            return response.getResult();
        } catch (StatusRuntimeException e) {
            System.err.println("RPC failed: " + e.getStatus());
            throw new RuntimeException("RPC failed", e);
        }
    }

    /** Interactive calculator menu */
    public static void main(String[] args) throws InterruptedException {
        String serverHost = "localhost";
        int serverPort = 50051;
        
        System.out.println("=== gRPC Calculator Client ===");
        System.out.println("Connecting to server at " + serverHost + ":" + serverPort);
        
        CalculatorClient client = new CalculatorClient(serverHost, serverPort);
        Scanner scanner = new Scanner(System.in);
        
        try {
            while (true) {
                printMenu();
                System.out.print("Select option (1-3): ");
                String choice = scanner.nextLine().trim();
                
                if (choice.equals("3")) {
                    System.out.println("Goodbye!");
                    break;
                }
                
                if (!choice.equals("1") && !choice.equals("2")) {
                    System.out.println("Invalid choice. Please enter 1, 2, or 3.");
                    continue;
                }
                
                double num1 = getNumber(scanner, "Enter first number: ");
                double num2 = getNumber(scanner, "Enter second number: ");
                
                try {
                    double result;
                    if (choice.equals("1")) {
                        result = client.add(num1, num2);
                        System.out.printf("Result: %.2f + %.2f = %.2f\n", num1, num2, result);
                    } else {
                        result = client.subtract(num1, num2);
                        System.out.printf("Result: %.2f - %.2f = %.2f\n", num1, num2, result);
                    }
                } catch (Exception e) {
                    System.out.println("Error: " + e.getMessage());
                    System.out.println("Make sure the C++ server is running!");
                }
                
                System.out.println();  // Empty line for readability
            }
        } finally {
            client.shutdown();
            scanner.close();
        }
    }
    
    private static void printMenu() {
        System.out.println("\n=== Calculator Menu ===");
        System.out.println("1. Add two numbers");
        System.out.println("2. Subtract two numbers");
        System.out.println("3. Exit");
    }
    
    private static double getNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Double.parseDouble(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid number.");
            }
        }
    }
}