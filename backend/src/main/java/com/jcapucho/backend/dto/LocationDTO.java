package com.jcapucho.backend.dto;

import com.jcapucho.backend.entities.Location;
import lombok.Data;

@Data
public class LocationDTO {
    public final long id;
    public final String name;
    public final float lat;
    public final float lon;

    public static LocationDTO fromLocation(Location location) {
        return new LocationDTO(location.getId(), location.getName(), location.getLat(), location.getLon());
    }
}
