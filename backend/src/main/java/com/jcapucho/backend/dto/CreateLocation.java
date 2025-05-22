package com.jcapucho.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

@Data
public class CreateLocation {
    @NotNull
    @NotBlank
    public final String name;

    @Range(min = -90, max = 90)
    public final float lat;

    @Range(min = -180, max = 180)
    public final float lon;
}
