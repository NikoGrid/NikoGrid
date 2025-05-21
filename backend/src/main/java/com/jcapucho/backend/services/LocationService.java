package com.jcapucho.backend.services;

import com.jcapucho.backend.dto.InterestPointBaseDTO;
import com.jcapucho.backend.entities.Location;
import com.jcapucho.backend.exceptions.ResourceNotFound;

import java.util.Collection;

public interface LocationService {
    Location createLocation(Location location);

    Location getLocationById(long id) throws ResourceNotFound;

    Location getClosestAvailable(float longitude, float latitude) throws ResourceNotFound;

    Collection<InterestPointBaseDTO> getNearbyLocations(
            float minLongitude,
            float minLatitude,
            float maxLongitude,
            float maxLatitude,
            int zoomLevel
    );
}
