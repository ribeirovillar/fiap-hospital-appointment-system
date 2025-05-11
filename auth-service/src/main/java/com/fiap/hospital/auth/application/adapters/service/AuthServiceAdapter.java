package com.fiap.hospital.auth.application.adapters.service;

import com.fiap.hospital.auth.application.dto.LoginRequestDTO;
import com.fiap.hospital.auth.application.dto.LoginResponseDTO;
import com.fiap.hospital.auth.application.dto.RegisterRequestDTO;
import com.fiap.hospital.auth.application.exception.UserAlreadyExistsException;
import com.fiap.hospital.auth.domain.entities.User;
import com.fiap.hospital.auth.domain.ports.auth.AuthPort;
import com.fiap.hospital.auth.domain.ports.persistence.UserRepositoryPort;
import com.fiap.hospital.auth.infrastructure.adapters.security.JwtTokenAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceAdapter implements AuthPort {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenAdapter jwtService;

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername());

        return LoginResponseDTO.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole())
                .name(user.getName())
                .build();
    }

    @Override
    public User register(RegisterRequestDTO registerRequest) {
        if (userRepository.findByUsername(registerRequest.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Username already exists: " + registerRequest.getUsername());
        }

        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .name(registerRequest.getName())
                .role(registerRequest.getRole())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }
} 