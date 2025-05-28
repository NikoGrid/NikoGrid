package com.nikogrid.backend.services;

import com.nikogrid.backend.configurations.SecurityConfig;
import com.nikogrid.backend.dto.AuthenticationDTO;
import com.nikogrid.backend.dto.RecoveryJwtTokenDTO;
import com.nikogrid.backend.dto.RegisterDTO;
import com.nikogrid.backend.entities.Role;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.entities.UserDetailsImpl;
import com.nikogrid.backend.exceptions.DuplicateUserException;
import com.nikogrid.backend.repositories.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AuthenticationService {
    private AuthenticationManager authenticationManager;
    private JwtTokenService jwtTokenService;
    private UserRepository userRepository;
    private SecurityConfig securityConfiguration;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, UserRepository userRepository, SecurityConfig securityConfiguration) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
        this.userRepository = userRepository;
        this.securityConfiguration = securityConfiguration;
    }

    public RecoveryJwtTokenDTO authenticateUser(AuthenticationDTO authenticationDTO) {
        try{
            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                    new UsernamePasswordAuthenticationToken(authenticationDTO.email(), authenticationDTO.password());
            Authentication authentication = authenticationManager.authenticate(usernamePasswordAuthenticationToken);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            return new RecoveryJwtTokenDTO(jwtTokenService.generateToken(userDetails));
        }catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid credentials");
        }
    }

    public void registerUser(RegisterDTO registerDTO) throws DuplicateUserException {
        if (userRepository.findByEmail(registerDTO.email()).isPresent()) {
            throw new DuplicateUserException("User with email " + registerDTO.email() + " already exists");
        }
        User newUser = User.builder()
                .email(registerDTO.email())
                .password(securityConfiguration.passwordEncoder().encode(registerDTO.password()))
                .created_at(LocalDateTime.now())
                .roles(List.of(Role.builder().name(registerDTO.role()).build()))
                .build();
        userRepository.save(newUser);
    }
}

