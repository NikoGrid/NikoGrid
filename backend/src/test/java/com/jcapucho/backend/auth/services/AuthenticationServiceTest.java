package com.jcapucho.backend.auth.services;

import com.jcapucho.backend.auth.configs.SecurityConfiguration;
import com.jcapucho.backend.auth.entities.Role;
import com.jcapucho.backend.auth.entities.RoleName;
import com.jcapucho.backend.auth.entities.User;
import com.jcapucho.backend.auth.entities.UserDetailsImpl;
import com.jcapucho.backend.auth.entities.dtos.AuthenticationDTO;
import com.jcapucho.backend.auth.entities.dtos.RecoveryJwtTokenDTO;
import com.jcapucho.backend.auth.entities.dtos.RegisterDTO;
import com.jcapucho.backend.auth.exceptions.DuplicateUserException;
import com.jcapucho.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationService Tests")
class AuthenticationServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SecurityConfiguration securityConfiguration;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationService authenticationService;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "password123";
    private final String ENCODED_PASSWORD = "$2a$10$encodedPassword";
    private final String JWT_TOKEN = "jwt.token.here";
    private final RoleName TEST_ROLE = RoleName.OWNER;
    private final UUID TEST_USER_ID = UUID.randomUUID();

    private User testUser;
    private UserDetailsImpl testUserDetails;
    private AuthenticationDTO authenticationDTO;
    private RegisterDTO registerDTO;
    private Authentication mockAuthentication;

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testUserDetails = new UserDetailsImpl(testUser);
        authenticationDTO = new AuthenticationDTO(TEST_EMAIL, TEST_PASSWORD);
        registerDTO = new RegisterDTO(TEST_EMAIL, TEST_PASSWORD, TEST_ROLE);
        mockAuthentication = mock(Authentication.class);
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @DisplayName("Should successfully authenticate user with valid credentials")
    void authenticateUser_WithValidCredentials_ShouldReturnJwtToken() {
        when(mockAuthentication.getPrincipal()).thenReturn(testUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtTokenService.generateToken(testUserDetails)).thenReturn(JWT_TOKEN);

        RecoveryJwtTokenDTO result = authenticationService.authenticateUser(authenticationDTO);

        assertNotNull(result);
        assertEquals(JWT_TOKEN, result.token());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager, times(1)).authenticate(tokenCaptor.capture());

        UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();
        assertEquals(TEST_EMAIL, capturedToken.getName());
        assertEquals(TEST_PASSWORD, capturedToken.getCredentials());

        verify(jwtTokenService, times(1)).generateToken(testUserDetails);
        verifyNoInteractions(userRepository, securityConfiguration);
    }

    @Test
    @DisplayName("Should throw exception when authentication fails")
    void authenticateUser_WithInvalidCredentials_ShouldThrowBadCredentialsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authenticationService.authenticateUser(authenticationDTO)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtTokenService, userRepository, securityConfiguration);
    }

    @Test
    @DisplayName("Should handle null authentication DTO")
    void authenticateUser_WithNullDTO_ShouldThrowException() {
        assertThrows(
                NullPointerException.class,
                () -> authenticationService.authenticateUser(null)
        );

        verifyNoInteractions(authenticationManager, jwtTokenService, userRepository, securityConfiguration);
    }

    @Test
    @DisplayName("Should handle authentication DTO with null email")
    void authenticateUser_WithNullEmail_ShouldCreateTokenWithNullEmail() {
        AuthenticationDTO dtoWithNullEmail = new AuthenticationDTO(null, TEST_PASSWORD);
        when(mockAuthentication.getPrincipal()).thenReturn(testUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mockAuthentication);
        when(jwtTokenService.generateToken(testUserDetails)).thenReturn(JWT_TOKEN);

        RecoveryJwtTokenDTO result = authenticationService.authenticateUser(dtoWithNullEmail);

        assertNotNull(result);
        assertEquals(JWT_TOKEN, result.token());

        ArgumentCaptor<UsernamePasswordAuthenticationToken> tokenCaptor =
                ArgumentCaptor.forClass(UsernamePasswordAuthenticationToken.class);
        verify(authenticationManager, times(1)).authenticate(tokenCaptor.capture());

        UsernamePasswordAuthenticationToken capturedToken = tokenCaptor.getValue();
        assertEquals("", capturedToken.getName());
    }

    // ==================== REGISTRATION TESTS ====================

    @Test
    @DisplayName("Should successfully register new user")
    void registerUser_WithNewUser_ShouldSaveUser() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(securityConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        authenticationService.registerUser(registerDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(TEST_EMAIL, savedUser.getEmail());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
        assertNotNull(savedUser.getRoles());
        assertEquals(1, savedUser.getRoles().size());
        assertEquals(TEST_ROLE, savedUser.getRoles().get(0).getName());

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verify(securityConfiguration, times(1)).passwordEncoder();
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when user with email already exists")
    void registerUser_WithExistingEmail_ShouldThrowUserAlreadyExistsException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        DuplicateUserException exception = assertThrows(
                DuplicateUserException.class,
                () -> authenticationService.registerUser(registerDTO)
        );

        assertEquals("User with email " + TEST_EMAIL + " already exists", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);

        verifyNoInteractions(securityConfiguration, passwordEncoder);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle null register DTO")
    void registerUser_WithNullDTO_ShouldThrowException() {
        assertThrows(
                NullPointerException.class,
                () -> authenticationService.registerUser(null)
        );

        verifyNoInteractions(userRepository, securityConfiguration, passwordEncoder);
    }

    @Test
    @DisplayName("Should handle register DTO with null email")
    void registerUser_WithNullEmail_ShouldHandleNullEmail() {
        RegisterDTO dtoWithNullEmail = new RegisterDTO(null, TEST_PASSWORD, TEST_ROLE);
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());
        when(securityConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        authenticationService.registerUser(dtoWithNullEmail);

        verify(userRepository, times(1)).findByEmail(null);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNull(savedUser.getEmail());
    }

    @Test
    @DisplayName("Should handle register DTO with null password")
    void registerUser_WithNullPassword_ShouldEncodeNullPassword() {
        RegisterDTO dtoWithNullPassword = new RegisterDTO(TEST_EMAIL, null, TEST_ROLE);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(securityConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(null)).thenReturn("encoded_null");

        authenticationService.registerUser(dtoWithNullPassword);

        verify(passwordEncoder, times(1)).encode(null);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals("encoded_null", savedUser.getPassword());
    }

    @Test
    @DisplayName("Should handle register DTO with null role")
    void registerUser_WithNullRole_ShouldCreateRoleWithNullName() {
        RegisterDTO dtoWithNullRole = new RegisterDTO(TEST_EMAIL, TEST_PASSWORD, null);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(securityConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        authenticationService.registerUser(dtoWithNullRole);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertNotNull(savedUser.getRoles());
        assertEquals(1, savedUser.getRoles().size());
        assertNull(savedUser.getRoles().get(0).getName());
    }

    @Test
    @DisplayName("Should handle different role types")
    void registerUser_WithDifferentRoles_ShouldCreateCorrectRole() {
        RoleName adminRole = RoleName.ADMIN;
        RegisterDTO adminRegisterDTO = new RegisterDTO(TEST_EMAIL, TEST_PASSWORD, adminRole);
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(securityConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);

        authenticationService.registerUser(adminRegisterDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();
        assertEquals(adminRole, savedUser.getRoles().get(0).getName());
    }

    @Test
    @DisplayName("Should handle repository save failure")
    void registerUser_WhenRepositoryFails_ShouldPropagateException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(securityConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authenticationService.registerUser(registerDTO)
        );

        assertEquals("Database error", exception.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle password encoding failure")
    void registerUser_WhenPasswordEncodingFails_ShouldPropagateException() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.empty());
        when(securityConfiguration.passwordEncoder()).thenReturn(passwordEncoder);
        when(passwordEncoder.encode(TEST_PASSWORD)).thenThrow(new RuntimeException("Encoding error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authenticationService.registerUser(registerDTO)
        );

        assertEquals("Encoding error", exception.getMessage());
        verify(passwordEncoder, times(1)).encode(TEST_PASSWORD);
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    @DisplayName("Constructor should initialize all dependencies")
    void constructor_ShouldInitializeAllDependencies() {
        AuthenticationManager mockAuthManager = mock(AuthenticationManager.class);
        JwtTokenService mockJwtService = mock(JwtTokenService.class);
        UserRepository mockUserRepo = mock(UserRepository.class);
        SecurityConfiguration mockSecurityConfig = mock(SecurityConfiguration.class);

        AuthenticationService service = new AuthenticationService(
                mockAuthManager, mockJwtService, mockUserRepo, mockSecurityConfig);

        assertNotNull(service);
    }

    private User createTestUser() {
        Role testRole = Role.builder()
                .name(TEST_ROLE)
                .build();

        return User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .password(ENCODED_PASSWORD)
                .roles(List.of(testRole))
                .created_at(LocalDateTime.now())
                .build();
    }
}