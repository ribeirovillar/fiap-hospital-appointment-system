package com.fiap.hospital.auth.infrastructure.adapters.grpc;

import com.fiap.hospital.auth.proto.AuthServiceGrpc;
import com.fiap.hospital.auth.proto.TokenValidationRequest;
import com.fiap.hospital.auth.proto.TokenValidationResponse;
import com.fiap.hospital.auth.domain.entities.User;
import com.fiap.hospital.auth.domain.enums.UserRole;
import com.fiap.hospital.auth.domain.ports.persistence.UserRepositoryPort;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class AuthGrpcServiceTest {

    private ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub blockingStub;

    @Autowired
    private UserRepositoryPort userRepository;

    @BeforeEach
    void setUp() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        blockingStub = AuthServiceGrpc.newBlockingStub(channel);

        userRepository.findByUsername("demostenis_villar2").orElseGet(() -> {
            User testUser = User.builder()
                    .username("demostenis_villar2")
                    .name("Demostenis Villar")
                    .password("password")
                    .role(UserRole.DOCTOR)
                    .build();
            return userRepository.save(testUser);
        });
    }

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Test
    void testValidateTokenAndGetRole_InvalidToken() {
        String invalidToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken(invalidToken)
                .build();

        TokenValidationResponse response = blockingStub.validateTokenAndGetRole(request);

        assertNotNull(response);
        assertFalse(response.getIsValid());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("JWT signature does not match"));
    }

    @Test
    void testValidateTokenAndGetRole_MalformedToken() {
        String malformedToken = "not.a.valid.jwt.token";

        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken(malformedToken)
                .build();

        TokenValidationResponse response = blockingStub.validateTokenAndGetRole(request);

        assertNotNull(response);
        assertFalse(response.getIsValid());
        assertNotNull(response.getErrorMessage());
        assertTrue(response.getErrorMessage().contains("JWT strings must contain exactly 2 period characters"));
    }

    @Test
    void testValidateTokenAndGetRole_EmptyToken() {
        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken("")
                .build();

        TokenValidationResponse response = blockingStub.validateTokenAndGetRole(request);

        assertNotNull(response);
        assertFalse(response.getIsValid());
        assertNotNull(response.getErrorMessage());
    }

    @Test
    void testValidateTokenAndGetRole_ValidToken() {
        String validToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJkZW1vc3RlbmlzX3ZpbGxhcjIiLCJpYXQiOjE3NDY5OTg3NDAsImV4cCI6MTgzMzM5ODc0MH0.yPrMnJ5OT9HrIoLZNtOyCHFlbBguAJHW3cmfL955fVw";

        TokenValidationRequest request = TokenValidationRequest.newBuilder()
                .setToken(validToken)
                .build();

        TokenValidationResponse response = blockingStub.validateTokenAndGetRole(request);

        assertNotNull(response);
        assertTrue(response.getIsValid());
        assertTrue(response.getErrorMessage() == null || response.getErrorMessage().isEmpty());
        assertNotNull(response.getRole());
    }
} 