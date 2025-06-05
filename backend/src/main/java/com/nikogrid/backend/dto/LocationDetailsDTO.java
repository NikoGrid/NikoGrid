package com.nikogrid.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class LocationDetailsDTO {
    @NotNull
    public final long id;

    @NotNull
    public final String name;

    @NotNull
    public final float lat;

    @NotNull
    public final float lon;

    @NotNull
    public final List<ChargerDTO> chargers;
}
