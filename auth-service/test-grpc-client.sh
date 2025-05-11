#!/bin/bash

# Ensure we're in the right directory
cd "$(dirname "$0")"

# Function to show usage
usage() {
  echo "Usage: $0 [valid|invalid]"
  echo "  valid   - Test with a valid token"
  echo "  invalid - Test with an invalid token"
  exit 1
}

# Check command line arguments
if [ $# -ne 1 ]; then
  usage
fi

# Set up variables
GRPC_PORT=9090

# First, let's register a user to get a valid token
echo "Registering a user to get a valid token..."
REGISTER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"grpctest","password":"password123","name":"GRPC Test User","role":"DOCTOR"}')

echo "Registration response: $REGISTER_RESPONSE"
# Registration might fail if user already exists, but that's OK

# Login to get the token
echo "Logging in to get token..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"grpctest","password":"password123"}')

echo "Login response: $LOGIN_RESPONSE"

# Extract token from login response
TOKEN=$(echo $LOGIN_RESPONSE | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

echo "Extracted token: $TOKEN"

# If no token found, exit
if [ -z "$TOKEN" ]; then
  echo "Failed to get token, exiting..."
  exit 1
fi

# Create a simple gRPC client to test token validation
echo "Creating gRPC test client..."

# Manual test using grpcurl if available
if command -v grpcurl &> /dev/null; then
  echo "Using grpcurl to test the gRPC service..."
  
  # Create a temporary proto file
  PROTO_FILE="$(mktemp).proto"
  cat > $PROTO_FILE << 'EOF'
syntax = "proto3";

package com.fiap.hospital.auth.infrastructure.grpc;

service AuthService {
  rpc ValidateTokenAndGetRole (TokenValidationRequest) returns (TokenValidationResponse);
}

message TokenValidationRequest {
  string token = 1;
}

message TokenValidationResponse {
  bool valid = 1;
  string username = 2;
  string role = 3;
  string errorMessage = 4;
}
EOF

  # Decide which test to run
  case "$1" in
    valid)
      # Test with valid token
      echo "Testing gRPC server with valid token using grpcurl..."
      grpcurl -plaintext -proto $PROTO_FILE -d "{\"token\":\"$TOKEN\"}" localhost:$GRPC_PORT com.fiap.hospital.auth.infrastructure.grpc.AuthService/ValidateTokenAndGetRole
      ;;
    invalid)
      # Test with invalid token
      echo "Testing gRPC server with invalid token using grpcurl..."
      INVALID_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJpbnZhbGlkIiwicm9sZSI6IlVTRVIiLCJleHAiOjE5MTYyMzkwMjJ9.invalid_signature"
      grpcurl -plaintext -proto $PROTO_FILE -d "{\"token\":\"$INVALID_TOKEN\"}" localhost:$GRPC_PORT com.fiap.hospital.auth.infrastructure.grpc.AuthService/ValidateTokenAndGetRole
      ;;
    *)
      usage
      ;;
  esac

  rm $PROTO_FILE
else
  echo "grpcurl not found, using Java client..."
  mkdir -p ./grpc-test
  cat > ./grpc-test/pom.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.fiap.test</groupId>
    <artifactId>grpc-test</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <grpc.version>1.59.0</grpc.version>
        <protobuf.version>3.23.4</protobuf.version>
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
        <extensions>
            <extension>
                <groupId>kr.motd.maven</groupId>
                <artifactId>os-maven-plugin</artifactId>
                <version>1.7.1</version>
            </extension>
        </extensions>
        <plugins>
            <plugin>
                <groupId>org.xolstice.maven.plugins</groupId>
                <artifactId>protobuf-maven-plugin</artifactId>
                <version>0.6.1</version>
                <configuration>
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
            <plugin>
                <groupId>org.codehaus.mojo</groupId>
                <artifactId>exec-maven-plugin</artifactId>
                <version>3.1.0</version>
                <configuration>
                    <mainClass>com.fiap.test.GrpcClient</mainClass>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
EOF

  # Copy proto file
  mkdir -p ./grpc-test/src/main/proto
  cp src/main/proto/auth_service.proto ./grpc-test/src/main/proto/

  # Create test client
  mkdir -p ./grpc-test/src/main/java/com/fiap/test
  cat > ./grpc-test/src/main/java/com/fiap/test/GrpcClient.java << 'EOF'
package com.fiap.test;

import com.fiap.hospital.auth.infrastructure.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import java.util.concurrent.TimeUnit;

public class GrpcClient {
    private final ManagedChannel channel;
    private final AuthServiceGrpc.AuthServiceBlockingStub blockingStub;

    public GrpcClient(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        blockingStub = AuthServiceGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void validateToken(String token) {
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken(token)
                .build();

        TokenValidationResponse response = blockingStub.validateTokenAndGetRole(request);

        System.out.println("\n======= gRPC Response =======");
        System.out.println("Valid: " + response.getValid());
        System.out.println("Username: " + response.getUsername());
        System.out.println("Role: " + response.getRole());
        if (!response.getValid()) {
            System.out.println("Error: " + response.getErrorMessage());
        }
        System.out.println("============================\n");
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: GrpcClient <token>");
            System.exit(1);
        }

        GrpcClient client = new GrpcClient("localhost", 9090);
        try {
            client.validateToken(args[0]);
        } finally {
            client.shutdown();
        }
    }
}
EOF

  # Build the gRPC test client
  echo "Building gRPC test client..."
  cd ./grpc-test
  mvn clean package

  # Decide which test to run
  case "$1" in
    valid)
      # Test with valid token
      echo "Testing gRPC server with valid token..."
      mvn exec:java -Dexec.args="$TOKEN"
      ;;
    invalid)
      # Test with invalid token
      echo "Testing gRPC server with invalid token..."
      INVALID_TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJpbnZhbGlkIiwicm9sZSI6IlVTRVIiLCJleHAiOjE5MTYyMzkwMjJ9.invalid_signature"
      mvn exec:java -Dexec.args="$INVALID_TOKEN"
      ;;
    *)
      usage
      ;;
  esac

  cd ..
fi

echo "gRPC test completed." 