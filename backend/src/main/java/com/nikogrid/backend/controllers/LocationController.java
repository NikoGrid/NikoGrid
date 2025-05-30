package com.nikogrid.backend.controllers;

import com.nikogrid.backend.dto.CreateLocation;
import com.nikogrid.backend.dto.InterestPointBaseDTO;
import com.nikogrid.backend.dto.LocationDTO;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.services.LocationService;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/locations", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class LocationController {
    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public LocationDTO createLocation(@Valid @RequestBody CreateLocation req) {
        final Location location = new Location();
        location.setName(req.getName());
        location.setLat(req.getLat());
        location.setLon(req.getLon());
        return LocationDTO.fromLocation(this.locationService.createLocation(location));
    }

    @GetMapping("/nearby")
    public Collection<InterestPointBaseDTO> getNearbyLocations(
            @RequestParam("w") @Min(-180) @Max(180) float west,
            @RequestParam("e") @Min(-180) @Max(180) float east,
            @RequestParam("s") @Min(-90) @Max(90) float south,
            @RequestParam("n") @Min(-90) @Max(90) float north,
            @RequestParam("z") @Min(0) @Max(18) int zoom
    ) {
        if (west > east)
            throw new ConstraintViolationException("East must be greater than west", Set.of());

        if (south > north)
            throw new ConstraintViolationException("North must be greater than south", Set.of());

        return this.locationService.getNearbyLocations(west, south, east, north, zoom);
    }

    @GetMapping("/closest")
    public LocationDTO getClosestAvailableLocation(
            @RequestParam("lon") @Min(-180) @Max(180) float longitude,
            @RequestParam("lat") @Min(-90) @Max(90) float latitude
    ) throws ResourceNotFound {
        return LocationDTO.fromLocation(this.locationService.getClosestAvailable(longitude, latitude));
    }

    @GetMapping("/{id}")
    public LocationDTO getLocationById(@PathVariable("id") long id) throws ResourceNotFound {
        return LocationDTO.fromLocation(this.locationService.getLocationById(id));
    }
}
