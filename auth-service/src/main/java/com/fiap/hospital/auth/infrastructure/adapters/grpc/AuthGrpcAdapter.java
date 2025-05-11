package com.fiap.hospital.auth.infrastructure.adapters.grpc;

import com.fiap.hospital.auth.domain.entities.User;
import com.fiap.hospital.auth.domain.ports.persistence.UserRepositoryPort;
import com.fiap.hospital.auth.infrastructure.adapters.security.JwtTokenAdapter;
import com.fiap.hospital.auth.proto.AuthServiceGrpc;
import com.fiap.hospital.auth.proto.TokenValidationRequest;
import com.fiap.hospital.auth.proto.TokenValidationResponse;
import com.fiap.hospital.auth.proto.UserRole;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AuthGrpcAdapter extends AuthServiceGrpc.AuthServiceImplBase {

    private final JwtTokenAdapter jwtService;
    private final UserRepositoryPort userRepository;

    public AuthGrpcAdapter(JwtTokenAdapter jwtService, UserRepositoryPort userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public void validateTokenAndGetRole(TokenValidationRequest request,
            StreamObserver<TokenValidationResponse> responseObserver) {
        String token = request.getToken();
        log.debug("Received token validation request for token: {}", token);
        TokenValidationResponse.Builder responseBuilder = TokenValidationResponse.newBuilder();

        try {
            String username = jwtService.extractUsername(token);
            log.debug("Extracted username from token: {}", username);

            if (jwtService.validateToken(token, username)) {
                log.debug("Token is valid, looking up user: {}", username);
                User user = userRepository.findByUsername(username)
                        .orElseThrow(() -> {
                            log.error("User not found: {}", username);
                            return new RuntimeException("User not found: " + username);
                        });
                
                log.debug("Found user with role: {}", user.getRole());
                UserRole role = convertToGrpcRole(user.getRole().name());
                log.debug("Converted role to gRPC role: {}", role);

                responseBuilder
                        .setIsValid(true)
                        .setRole(role);
                log.debug("Built response with isValid=true and role={}", role);
            } else {
                log.warn("Token validation failed for username: {}", username);
                responseBuilder
                        .setIsValid(false)
                        .setErrorMessage("Invalid token");
            }
        } catch (Exception e) {
            log.error("Error validating token", e);
            responseBuilder
                    .setIsValid(false)
                    .setErrorMessage("Error validating token: " + e.getMessage());
        }

        TokenValidationResponse response = responseBuilder.build();
        log.debug("Sending response: isValid={}, role={}, errorMessage={}", 
                response.getIsValid(), response.getRole(), response.getErrorMessage());
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    private UserRole convertToGrpcRole(String role) {
        log.debug("Converting role: {}", role);
        try {
            return switch (role.toUpperCase()) {
                case "DOCTOR" -> UserRole.DOCTOR;
                case "NURSE" -> UserRole.NURSE;
                case "PATIENT" -> UserRole.PATIENT;
                default -> {
                    log.warn("Unknown role received: {}", role);
                    yield UserRole.UNKNOWN;
                }
            };
        } catch (Exception e) {
            log.error("Error converting role: {}", role, e);
            return UserRole.UNKNOWN;
        }
    }
}