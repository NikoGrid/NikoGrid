package com.nikogrid.backend.services;

import com.nikogrid.backend.dto.RegisterDTO;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.exceptions.DuplicateUserException;
import com.nikogrid.backend.repositories.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public void registerUser(RegisterDTO registerDTO) throws DuplicateUserException {
        try {
            User user = new User();
            user.setEmail(registerDTO.getEmail());
            user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateUserException();
        }
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

