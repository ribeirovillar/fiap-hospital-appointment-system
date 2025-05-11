package com.fiap.hospital.auth.domain.ports.auth;

import com.fiap.hospital.auth.application.dto.LoginRequestDTO;
import com.fiap.hospital.auth.application.dto.LoginResponseDTO;
import com.fiap.hospital.auth.application.dto.RegisterRequestDTO;
import com.fiap.hospital.auth.domain.entities.User;

public interface AuthPort {
    LoginResponseDTO login(LoginRequestDTO loginRequest);
    User register(RegisterRequestDTO registerRequest);
} 