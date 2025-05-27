package com.nikogrid.backend.controllers;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.TestSecurityBeans;
import com.nikogrid.backend.auth.SecurityConfig;
import com.nikogrid.backend.dto.LoginDTO;
import com.nikogrid.backend.dto.RegisterDTO;
import com.nikogrid.backend.exceptions.DuplicateUserException;
import com.nikogrid.backend.repositories.UserRepository;
import com.nikogrid.backend.services.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import({SecurityConfig.class, TestSecurityBeans.class})
class AuthenticationControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private UserRepository userRepository;


    @MockitoBean
    private AuthenticationService authenticationService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @Requirement("NIK-41")
    void createAccountOk() throws Exception {
        final RegisterDTO req = new RegisterDTO(
                "test@example.com", "test123"
        );

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());

        Mockito.verify(authenticationService, Mockito
                        .times(1))
                .registerUser(Mockito.any());
    }

    @Test
    @Requirement("NIK-41")
    void createAccountConflict() throws Exception {
        final RegisterDTO req = new RegisterDTO(
                "test@example.com", "test123"
        );

        Mockito.doThrow(new DuplicateUserException())
                .when(authenticationService)
                .registerUser(Mockito.any());

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict());

        Mockito.verify(authenticationService, Mockito
                        .times(1))
                .registerUser(Mockito.any());
    }


    @Test
    @WithMockUser
    @Requirement("NIK-41")
    void createAccountForbidden() throws Exception {
        final var req = new RegisterDTO(
                "test@example.com", "test123"
        );

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(authenticationService, Mockito.never()).registerUser(Mockito.any());
    }

    @Test
    @Requirement("NIK-42")
    void loginOk() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(new Authentication() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return List.of();
            }

            @Override
            public Object getCredentials() {
                return null;
            }

            @Override
            public Object getDetails() {
                return null;
            }

            @Override
            public Object getPrincipal() {
                return null;
            }

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
                // Should do nothing
            }

            @Override
            public String getName() {
                return "";
            }
        });

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(SecurityConfig.AUTH_COOKIE))
                .andExpect(cookie().maxAge(SecurityConfig.AUTH_COOKIE, greaterThan(0)));
    }

    @Test
    @Requirement("NIK-42")
    void loginInvalidCredentials() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenThrow(new AuthenticationException("") {

        });

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist(SecurityConfig.AUTH_COOKIE));
    }

    @Test
    @Requirement("NIK-42")
    void loginNoUser() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist(SecurityConfig.AUTH_COOKIE));
    }

    @Test
    @WithMockUser
    @Requirement("NIK-42")
    void loginForbidden() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(cookie().doesNotExist(SecurityConfig.AUTH_COOKIE));
    }


    @Test
    @Requirement("NIK-42")
    void logoutUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist(SecurityConfig.AUTH_COOKIE))
        ;
    }

    @Test
    @WithMockUser
    @Requirement("NIK-42")
    void logoutOk() throws Exception {
        mvc.perform(get("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(SecurityConfig.AUTH_COOKIE))
                .andExpect(cookie().maxAge(SecurityConfig.AUTH_COOKIE, 0));
    }

}