package com.fiap.hospital.auth.infrastructure.adapters.security;

import com.fiap.hospital.auth.domain.entities.User;
import com.fiap.hospital.auth.domain.ports.persistence.UserRepositoryPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service("userDetailsService")
@Component
public class UserDetailsAdapter implements UserDetailsService {

    private final UserRepositoryPort userRepository;

    @Autowired
    public UserDetailsAdapter(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }
} 