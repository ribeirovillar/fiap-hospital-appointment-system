package com.fiap.hospital.auth.application.adapters.rest;

import com.fiap.hospital.auth.application.dto.LoginRequestDTO;
import com.fiap.hospital.auth.application.dto.LoginResponseDTO;
import com.fiap.hospital.auth.application.dto.RegisterRequestDTO;
import com.fiap.hospital.auth.application.dto.UserResponseDTO;
import com.fiap.hospital.auth.domain.entities.User;
import com.fiap.hospital.auth.domain.ports.auth.AuthPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for user login and registration")
public class AuthController {

    private final AuthPort authService;

    @PostMapping("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticate a user with username and password and return a JWT token"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User authenticated successfully",
            content = @Content(schema = @Schema(implementation = LoginResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Invalid username or password",
            content = @Content(schema = @Schema(implementation = com.fiap.hospital.auth.application.exception.ApiError.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(schema = @Schema(implementation = com.fiap.hospital.auth.application.exception.ApiError.class))
        )
    })
    public ResponseEntity<LoginResponseDTO> login(
            @Parameter(description = "Login credentials", required = true)
            @Valid @RequestBody LoginRequestDTO loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }
    
    @PostMapping("/register")
    @Operation(
        summary = "Register user",
        description = "Register a new user with provided information"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "User registered successfully",
            content = @Content(schema = @Schema(implementation = UserResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Username already exists",
            content = @Content(schema = @Schema(implementation = com.fiap.hospital.auth.application.exception.ApiError.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid request parameters",
            content = @Content(schema = @Schema(implementation = com.fiap.hospital.auth.application.exception.ApiError.class))
        )
    })
    public ResponseEntity<UserResponseDTO> register(
            @Parameter(description = "User registration information", required = true)
            @Valid @RequestBody RegisterRequestDTO registerRequest) {
        User user = authService.register(registerRequest);
        
        UserResponseDTO response = UserResponseDTO.builder()
            .id(user.getId())
            .username(user.getUsername())
            .name(user.getName())
            .role(user.getRole())
            .createdAt(user.getCreatedAt())
            .build();
            
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 