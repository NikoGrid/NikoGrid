package com.jcapucho.backend.auth.entities.dtos;

import com.jcapucho.backend.auth.entities.Role;

import java.util.List;
import java.util.UUID;

public record RecoveryUserDto(
        UUID id,
        String email,
        List<Role> roles
) {
}
