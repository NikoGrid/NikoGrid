package com.nikogrid.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateLocation {
    @NotBlank
    public final String name;

    @NotNull
    @Min(-90)
    @Max(90)
    public final Float lat;

    @NotNull
    @Min(-180)
    @Max(180)
    public final Float lon;
}
