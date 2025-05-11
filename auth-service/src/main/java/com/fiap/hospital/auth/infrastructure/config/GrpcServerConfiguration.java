package com.fiap.hospital.auth.infrastructure.config;

import com.fiap.hospital.auth.infrastructure.adapters.grpc.AuthGrpcAdapter;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class GrpcServerConfiguration {

    private final AuthGrpcAdapter authGrpcService;

    @Value("${grpc.server.port:9090}")
    private int grpcPort;

    @Bean
    public Server grpcServer() {
        Server server = ServerBuilder.forPort(grpcPort)
                .addService(authGrpcService)
                .addService(ProtoReflectionService.newInstance())
                .build();

        try {
            server.start();
            log.info("gRPC server started on port {}", grpcPort);


            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                log.info("Shutting down gRPC server");
                server.shutdown();
                log.info("gRPC server shut down successfully");
            }));
        } catch (IOException e) {
            log.error("Failed to start gRPC server", e);
        }

        return server;
    }
} 