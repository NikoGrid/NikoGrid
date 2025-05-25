package com.nikogrid.backend.dto;

import com.nikogrid.backend.entities.Location;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LocationDTO {
    @NotNull
    public final long id;

    @NotNull
    public final String name;

    @NotNull
    public final float lat;

    @NotNull
    public final float lon;

    public static LocationDTO fromLocation(Location location) {
        return new LocationDTO(location.getId(), location.getName(), location.getLat(), location.getLon());
    }
}
