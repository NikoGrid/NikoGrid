package com.nikogrid.backend.services;

import com.nikogrid.backend.dto.InterestPointBaseDTO;
import com.nikogrid.backend.entities.InterestPoint;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.repositories.LocationRepository;
import com.nikogrid.backend.repositories.projections.LocationListing;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Stream;

@Service
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Autowired
    public LocationServiceImpl(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

    @Override
    public Location createLocation(Location location) {
        return this.locationRepository.save(location);
    }

    @Override
    public Location getLocationById(long id) throws ResourceNotFound {
        return this.locationRepository.findById(id).orElseThrow(ResourceNotFound::new);
    }

    @Override
    public Location getClosestAvailable(float longitude, float latitude) throws ResourceNotFound {
        return this.locationRepository.findClosestAvailable(longitude, latitude).orElseThrow(ResourceNotFound::new);
    }

    @Override
    @Transactional
    public Collection<InterestPointBaseDTO> getNearbyLocations(float minLongitude, float minLatitude, float maxLongitude, float maxLatitude, int zoomLevel) {
        if (zoomLevel >= 18) {
            try (Stream<LocationListing> stream = this.locationRepository.getLocationsInEnvelope(
                    minLongitude,
                    minLatitude,
                    maxLongitude,
                    maxLatitude
            )) {
                return stream
                        .map(InterestPointBaseDTO::fromLocationListing)
                        .toList();
            }
        } else {
            final double clusterRadius = 10 / Math.pow(2.0, zoomLevel);
            try (Stream<InterestPoint> stream = this.locationRepository.findInterestPoints(
                    minLongitude,
                    minLatitude,
                    maxLongitude,
                    maxLatitude,
                    (float) clusterRadius
            )) {
                return stream
                        .map(InterestPointBaseDTO::fromInterestPoint)
                        .toList();
            }
        }
    }
}
