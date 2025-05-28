package com.nikogrid.backend.dto;

import com.nikogrid.backend.entities.Role;

import java.util.List;
import java.util.UUID;

public record RecoveryUserDTO(
        UUID id,
        String email,
        List<Role> roles
) {
}
