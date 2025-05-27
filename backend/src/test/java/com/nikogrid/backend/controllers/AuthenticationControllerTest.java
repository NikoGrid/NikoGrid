package com.nikogrid.backend.controllers;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.entities.RoleName;
import com.nikogrid.backend.dto.AuthenticationDTO;
import com.nikogrid.backend.dto.RecoveryJwtTokenDTO;
import com.nikogrid.backend.dto.RegisterDTO;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.exceptions.DuplicateUserException;
import com.nikogrid.backend.exceptions.GlobalExceptionHandler;
import com.nikogrid.backend.repositories.UserRepository;
import com.nikogrid.backend.services.AuthenticationService;
import com.nikogrid.backend.services.JwtTokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthenticationController.class,
        excludeAutoConfiguration = {
                    org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
        }
)
@Import(GlobalExceptionHandler.class)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationService authenticationService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtTokenService jwtTokenService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String LOGIN_ENDPOINT = "/api/v1/auth/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/auth/register";

    @Test
    @DisplayName("Should authenticate user successfully")
    void shouldAuthenticateUserSuccessfully() throws Exception {
        AuthenticationDTO authDTO = new AuthenticationDTO("user@example.com", "password123");
        RecoveryJwtTokenDTO token = new RecoveryJwtTokenDTO("jwt.token.here");

        when(authenticationService.authenticateUser(any(AuthenticationDTO.class))).thenReturn(token);

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should register user successfully")
    void shouldRegisterUserSuccessfully() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("new@example.com", "password", RoleName.OWNER);
        doNothing().when(authenticationService).registerUser(any(RegisterDTO.class));
        mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Should return 401 when authentication fails")
    void shouldReturn401WhenNotAuthenticated() throws Exception {
        AuthenticationDTO authDTO = new AuthenticationDTO("user@example.com", "wrongpassword");

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authenticationService).authenticateUser(any(AuthenticationDTO.class));

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("Login should fail with invalid email format")
    void loginShouldFailWithInvalidEmail() throws Exception {
        AuthenticationDTO authDTO = new AuthenticationDTO("invalid-email", "password123");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login should fail with short password")
    void loginShouldFailWithShortPassword() throws Exception {
        AuthenticationDTO authDTO = new AuthenticationDTO("user@example.com", "short");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Login should fail with missing email")
    void loginShouldFailWithMissingEmail() throws Exception {
        AuthenticationDTO authDTO = new AuthenticationDTO(null, "password123");

        mockMvc.perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authDTO)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("Registration should fail with duplicate email")
    void registrationShouldFailWithDuplicateEmail() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("existing@example.com", "password", RoleName.ADMIN);

        doThrow(new DuplicateUserException("User already exists"))
                .when(authenticationService).registerUser(any(RegisterDTO.class));

        mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Registration should fail with invalid role")
    void registrationShouldFailWithInvalidRole() throws Exception {
        String invalidRolePayload = """
        {
            "email": "test@example.com",
            "password": "validpass",
            "role": "INVALID_ROLE"
        }
        """;

        mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidRolePayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Registration should fail with missing role")
    void registrationShouldFailWithMissingRole() throws Exception {
        RegisterDTO registerDTO = new RegisterDTO("test@example.com", "password", null);

        mockMvc.perform(post(REGISTER_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());

    }
}