package com.nikogrid.backend.services;

import com.nikogrid.backend.dto.InterestPointBaseDTO;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.exceptions.ResourceNotFound;

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
