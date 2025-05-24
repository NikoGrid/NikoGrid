package com.jcapucho.backend.auth.services;

import com.jcapucho.backend.auth.entities.Role;
import com.jcapucho.backend.auth.entities.RoleName;
import com.jcapucho.backend.auth.entities.User;
import com.jcapucho.backend.auth.entities.UserDetailsImpl;
import com.jcapucho.backend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthorizationService Tests")
class AuthorizationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthorizationService authorizationService;

    private User testUser;
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "password123";
    private final UUID TEST_USER_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        testUser = createTestUser();
    }

    @Test
    @DisplayName("Should successfully load user by username when user exists")
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserDetails result = authorizationService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        assertEquals(TEST_EMAIL, result.getUsername());

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should throw UsernameNotFoundException when user does not exist")
    void loadUserByUsername_WhenUserDoesNotExist_ShouldThrowUsernameNotFoundException() {
        String nonExistentEmail = "nonexistent@example.com";
        when(userRepository.findByEmail(nonExistentEmail)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authorizationService.loadUserByUsername(nonExistentEmail)
        );

        assertEquals("User " + nonExistentEmail + "Not Found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(nonExistentEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    @DisplayName("Should handle null username parameter")
    void loadUserByUsername_WhenUsernameIsNull_ShouldCallRepositoryWithNull() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> authorizationService.loadUserByUsername(null)
        );

        verify(userRepository, times(1)).findByEmail(null);
    }

    @Test
    @DisplayName("Should handle empty username parameter")
    void loadUserByUsername_WhenUsernameIsEmpty_ShouldThrowUsernameNotFoundException() {
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authorizationService.loadUserByUsername(emptyEmail)
        );

        assertEquals("User " + emptyEmail + "Not Found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(emptyEmail);
    }

    @Test
    @DisplayName("Should handle whitespace-only username parameter")
    void loadUserByUsername_WhenUsernameIsWhitespace_ShouldThrowUsernameNotFoundException() {
        String whitespaceEmail = "   ";
        when(userRepository.findByEmail(whitespaceEmail)).thenReturn(Optional.empty());

        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authorizationService.loadUserByUsername(whitespaceEmail)
        );

        assertEquals("User " + whitespaceEmail + "Not Found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(whitespaceEmail);
    }

    @Test
    @DisplayName("Should handle case sensitivity in email")
    void loadUserByUsername_WithDifferentCase_ShouldUseExactInput() {
        String upperCaseEmail = "TEST@EXAMPLE.COM";
        when(userRepository.findByEmail(upperCaseEmail)).thenReturn(Optional.empty());

        assertThrows(
                UsernameNotFoundException.class,
                () -> authorizationService.loadUserByUsername(upperCaseEmail)
        );

        verify(userRepository, times(1)).findByEmail(upperCaseEmail);
        verify(userRepository, never()).findByEmail(TEST_EMAIL.toLowerCase());
    }

    @Test
    @DisplayName("Should propagate repository exceptions")
    void loadUserByUsername_WhenRepositoryThrowsException_ShouldPropagateException() {
        RuntimeException repositoryException = new RuntimeException("Database connection failed");
        when(userRepository.findByEmail(anyString())).thenThrow(repositoryException);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> authorizationService.loadUserByUsername(TEST_EMAIL)
        );

        assertEquals("Database connection failed", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should create UserDetailsImpl with correct user data")
    void loadUserByUsername_ShouldCreateUserDetailsImplWithCorrectUserData() {
        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserDetails result = authorizationService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        assertEquals(TEST_EMAIL, result.getUsername());

        UserDetailsImpl userDetailsImpl = (UserDetailsImpl) result;
        assertNotNull(userDetailsImpl);

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should handle user with roles correctly")
    void loadUserByUsername_WithUserHavingRoles_ShouldReturnUserDetailsWithRoles() {
        List<Role> roles = createTestRoles();
        User userWithRoles = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(roles)
                .created_at(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userWithRoles));

        UserDetails result = authorizationService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        assertEquals(TEST_EMAIL, result.getUsername());

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should handle user with empty roles list")
    void loadUserByUsername_WithEmptyRolesList_ShouldReturnUserDetails() {
        User userWithEmptyRoles = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(new ArrayList<>())
                .created_at(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userWithEmptyRoles));

        UserDetails result = authorizationService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        assertEquals(TEST_EMAIL, result.getUsername());

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should handle user with null roles")
    void loadUserByUsername_WithNullRoles_ShouldReturnUserDetails() {
        User userWithNullRoles = User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(null)
                .created_at(LocalDateTime.now())
                .build();

        when(userRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(userWithNullRoles));

        UserDetails result = authorizationService.loadUserByUsername(TEST_EMAIL);

        assertNotNull(result);
        assertInstanceOf(UserDetailsImpl.class, result);
        assertEquals(TEST_EMAIL, result.getUsername());

        verify(userRepository, times(1)).findByEmail(TEST_EMAIL);
    }

    private List<Role> createTestRoles() {
        List<Role> roles = new ArrayList<>();

        Role adminRole = Role.builder().name(RoleName.ADMIN).build();
        Role ownerRole = Role.builder().name(RoleName.OWNER).build();
        Role collabRole = Role.builder().name(RoleName.COLLABORATOR).build();
        roles.add(adminRole);
        roles.add(ownerRole);
        roles.add(collabRole);
        return roles;
    }

    @Test
    @DisplayName("Constructor should initialize userRepository dependency")
    void constructor_ShouldInitializeUserRepository() {
        UserRepository mockRepository = mock(UserRepository.class);

        AuthorizationService service = new AuthorizationService(mockRepository);

        assertNotNull(service);
    }

    private User createTestUser() {
        return User.builder()
                .id(TEST_USER_ID)
                .email(TEST_EMAIL)
                .password(TEST_PASSWORD)
                .roles(new ArrayList<>())
                .created_at(LocalDateTime.now())
                .build();
    }
}