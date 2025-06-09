package com.nikogrid.backend.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCharger {
    @NotBlank
    public String name;

    public boolean available = true;

    @NotNull
    public float maxPower;
}
