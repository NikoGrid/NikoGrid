package com.nikogrid.backend.services;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    @Requirement("NIK-43")
    void findUserByEmail() {
        final var user = new User();
        user.setAdmin(true);
        user.setEmail("test@test.com");
        user.setPassword("password");

        Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.of(user));

        this.authenticationService.getUserByEmail("test@test.com");

        Mockito.verify(this.userRepository, Mockito.times(1)).findByEmail("test@test.com");
    }

    @Test
    @Requirement("NIK-43")
    void findUserByEmail_NotFound() {
        Mockito.when(userRepository.findByEmail(Mockito.anyString())).thenReturn(Optional.empty());

        assertThat(this.authenticationService.getUserByEmail("test@test.com")).isEmpty();
        Mockito.verify(this.userRepository, Mockito.times(1)).findByEmail("test@test.com");
    }
}
