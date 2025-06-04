package com.nikogrid.backend.controllers;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.auth.JwtGenerator;
import com.nikogrid.backend.auth.SecurityConstants;
import com.nikogrid.backend.dto.LoginDTO;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.UserRepository;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestcontainersConfiguration.class)
class AuthenticationControllerIT {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void resetDb() {
        this.userRepository.deleteAll();
    }

    @Test
    @Requirement("NIK-41")
    void createAccountOk() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

        assertThat(this.userRepository.findAll())
                .hasSize(1)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder(req.getEmail());
    }

    @Test
    @Requirement("NIK-41")
    void createAccountConflict() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        final var user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode("password"));
        this.userRepository.save(user);

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        assertThat(this.userRepository.findAll())
                .hasSize(1);
    }

    @Test
    @Requirement("NIK-41")
    void createAccountForbidden() throws Exception {
        final var req = new LoginDTO(
                "test2@example.com", "test123"
        );

        final var user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        this.userRepository.save(user);

        final var token = jwtGenerator.generateToken(user.getEmail());
        final var cookie = new Cookie(SecurityConstants.AUTH_COOKIE, token.token());

        mvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        assertThat(this.userRepository.findAll())
                .hasSize(1);
    }

    @Test
    @Requirement("NIK-42")
    void loginOk() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        final var user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        this.userRepository.save(user);

        final var res = mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(cookie().maxAge(SecurityConstants.AUTH_COOKIE, greaterThan(0)))
                .andReturn();

        final var cookie = Objects.requireNonNull(res.getResponse().getCookie(SecurityConstants.AUTH_COOKIE)).getValue();

        assertThat(jwtGenerator.getEmailFromToken(cookie)).isEqualTo(req.getEmail());
    }

    @Test
    @Requirement("NIK-42")
    void loginInvalidCredentials() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        final var user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode("password"));
        this.userRepository.save(user);

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(cookie().doesNotExist(SecurityConstants.AUTH_COOKIE));
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
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(cookie().doesNotExist(SecurityConstants.AUTH_COOKIE));
    }

    @Test
    @Requirement("NIK-42")
    void loginForbidden() throws Exception {
        final var req = new LoginDTO(
                "test@example.com", "test123"
        );

        final var user = new User();
        user.setEmail(req.getEmail());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        this.userRepository.save(user);

        final var token = jwtGenerator.generateToken(user.getEmail());
        final var cookie = new Cookie(SecurityConstants.AUTH_COOKIE, token.token());

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req))
                        .cookie(cookie))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andExpect(cookie().doesNotExist(SecurityConstants.AUTH_COOKIE));
    }

    @Test
    @Requirement("NIK-42")
    void logoutOk() throws Exception {
        final var user = new User();
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("test123"));
        this.userRepository.save(user);

        final var token = jwtGenerator.generateToken(user.getEmail());
        final var cookie = new Cookie(SecurityConstants.AUTH_COOKIE, token.token());

        mvc.perform(get("/api/v1/auth/logout")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(cookie().exists(SecurityConstants.AUTH_COOKIE))
                .andExpect(cookie().maxAge(SecurityConstants.AUTH_COOKIE, 0));
    }

    @Test
    @Requirement("NIK-42")
    void logoutUnauthorized() throws Exception {
        mvc.perform(get("/api/v1/auth/logout"))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().doesNotExist(SecurityConstants.AUTH_COOKIE));
    }
}
