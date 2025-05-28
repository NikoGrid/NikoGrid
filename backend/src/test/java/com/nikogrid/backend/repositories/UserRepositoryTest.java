package com.nikogrid.backend.repositories;

import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.entities.Role;
import com.nikogrid.backend.entities.RoleName;
import com.nikogrid.backend.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;
    private Role adminRole;
    private Role ownerRole;

    @BeforeEach
    void setUp() {
        adminRole = Role.builder()
                .name(RoleName.ADMIN)
                .build();

        ownerRole = Role.builder()
                .name(RoleName.OWNER)
                .build();

        entityManager.persistAndFlush(adminRole);
        entityManager.persistAndFlush(ownerRole);

        testUser1 = User.builder()
                .email("test1@example.com")
                .password("password123")
                .roles(new ArrayList<>(List.of(adminRole)))
                .created_at(LocalDateTime.now())
                .build();

        testUser2 = User.builder()
                .email("test2@example.com")
                .password("password456")
                .roles(new ArrayList<>(List.of(ownerRole)))
                .created_at(LocalDateTime.now().minusDays(1))
                .build();
    }

    @Test
    void testSaveUser() {
        User savedUser = userRepository.save(testUser1);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail()).isEqualTo("test1@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("password123");
        assertThat(savedUser.getRoles()).containsExactly(adminRole);
        assertThat(savedUser.getCreated_at()).isNotNull();
    }

    @Test
    void testFindByEmail_ExistingUser() {
        userRepository.save(testUser1);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("test1@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("test1@example.com");
        assertThat(foundUser.get().getPassword()).isEqualTo("password123");
    }

    @Test
    void testFindByEmail_NonExistingUser() {
        Optional<User> foundUser = userRepository.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isNotPresent();
    }

    @Test
    void testFindByEmail_CaseSensitive() {
        userRepository.save(testUser1);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("TEST1@EXAMPLE.COM");

        assertThat(foundUser).isNotPresent();
    }

    @Test
    void testFindById() {
        User savedUser = userRepository.save(testUser1);
        UUID userId = savedUser.getId();
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(userId);

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(userId);
        assertThat(foundUser.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    void testFindAll() {
        userRepository.save(testUser1);
        userRepository.save(testUser2);
        entityManager.flush();

        List<User> allUsers = userRepository.findAll();

        assertThat(allUsers).hasSize(2);
        assertThat(allUsers).extracting(User::getEmail)
                .containsExactlyInAnyOrder("test1@example.com", "test2@example.com");
    }

    @Test
    void testDeleteUser() {
        User savedUser = userRepository.save(testUser1);
        UUID userId = savedUser.getId();
        entityManager.flush();

        userRepository.deleteById(userId);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findById(userId);
        assertThat(foundUser).isNotPresent();
    }

    @Test
    void testUpdateUser() {
        User savedUser = userRepository.save(testUser1);
        entityManager.flush();

        savedUser.setEmail("updated@example.com");
        savedUser.setPassword("newPassword");

        User updatedUser = userRepository.save(savedUser);
        entityManager.flush();

        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getPassword()).isEqualTo("newPassword");

        Optional<User> foundUser = userRepository.findByEmail("updated@example.com");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getPassword()).isEqualTo("newPassword");
    }

    @Test
    void testUserWithMultipleRoles() {
        Role collaboratorRole = Role.builder()
                .name(RoleName.COLLABORATOR)
                .build();
        entityManager.persistAndFlush(collaboratorRole);

        User userWithMultipleRoles = User.builder()
                .email("multirole@example.com")
                .password("password")
                .roles(new ArrayList<>(List.of(adminRole, ownerRole, collaboratorRole)))
                .created_at(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(userWithMultipleRoles);
        entityManager.flush();

        Optional<User> foundUser = userRepository.findByEmail("multirole@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getRoles()).hasSize(3);
        assertThat(foundUser.get().getRoles()).extracting(Role::getName)
                .containsExactlyInAnyOrder(RoleName.ADMIN, RoleName.OWNER, RoleName.COLLABORATOR);
    }

    @Test
    void testUniqueEmailConstraint() {
        userRepository.save(testUser1);
        entityManager.flush();

        User duplicateEmailUser = User.builder()
                .email("test1@example.com") // Same email as testUser1
                .password("differentPassword")
                .roles(new ArrayList<>(List.of(ownerRole)))
                .created_at(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> {
            userRepository.save(duplicateEmailUser);
            entityManager.flush();
        }).isInstanceOf(Exception.class); // Could be DataIntegrityViolationException or similar
    }

    @Test
    void testCount() {
        assertThat(userRepository.count()).isEqualTo(0);

        userRepository.save(testUser1);
        entityManager.flush();
        assertThat(userRepository.count()).isEqualTo(1);

        userRepository.save(testUser2);
        entityManager.flush();
        assertThat(userRepository.count()).isEqualTo(2);
    }

    @Test
    void testExistsById() {
        User savedUser = userRepository.save(testUser1);
        UUID userId = savedUser.getId();
        entityManager.flush();

        assertThat(userRepository.existsById(userId)).isTrue();
        assertThat(userRepository.existsById(UUID.randomUUID())).isFalse();
    }

    @Test
    void testUserRolesCascadePersist() {
        Role newRole = Role.builder()
                .name(RoleName.COLLABORATOR)
                .build();

        User userWithNewRole = User.builder()
                .email("newrole@example.com")
                .password("password")
                .roles(new ArrayList<>(List.of(newRole)))
                .created_at(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(userWithNewRole);
        entityManager.flush();

        assertThat(savedUser.getRoles()).hasSize(1);
        assertThat(savedUser.getRoles().get(0).getId()).isNotEqualTo(0L);
        assertThat(savedUser.getRoles().get(0).getName()).isEqualTo(RoleName.COLLABORATOR);
    }
}