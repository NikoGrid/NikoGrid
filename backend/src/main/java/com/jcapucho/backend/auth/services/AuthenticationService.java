package com.jcapucho.backend.auth.services;

import com.jcapucho.backend.auth.configs.SecurityConfiguration;
import com.jcapucho.backend.auth.entities.Role;
import com.jcapucho.backend.auth.entities.User;
import com.jcapucho.backend.auth.entities.UserDetailsImpl;
import com.jcapucho.backend.auth.entities.dtos.AuthenticationDTO;
import com.jcapucho.backend.auth.entities.dtos.RecoveryJwtTokenDTO;
import com.jcapucho.backend.auth.entities.dtos.RegisterDTO;
import com.jcapucho.backend.auth.exceptions.DuplicateUserException;
import com.jcapucho.backend.auth.repository.UserRepository;
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
    private SecurityConfiguration securityConfiguration;

    public AuthenticationService(AuthenticationManager authenticationManager, JwtTokenService jwtTokenService, UserRepository userRepository, SecurityConfiguration securityConfiguration) {
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
