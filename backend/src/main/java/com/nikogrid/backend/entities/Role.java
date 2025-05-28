package com.nikogrid.backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Enumerated(EnumType.STRING)
    private RoleName name;
}
