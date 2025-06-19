package com.nikogrid.backend.dto;

import com.nikogrid.backend.entities.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    @NotNull
    private UUID id;

    @NotNull
    private String email;

    @NotNull
    private boolean isAdmin = false;

    public static UserDTO fromUser(User user) {
        return new UserDTO(user.getId(), user.getEmail(), user.isAdmin());
    }
}
