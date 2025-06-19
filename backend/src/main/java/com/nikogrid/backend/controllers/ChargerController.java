package com.nikogrid.backend.controllers;

import com.nikogrid.backend.dto.ChargerDTO;
import com.nikogrid.backend.dto.CreateCharger;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.services.ChargerService;
import com.nikogrid.backend.services.LocationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping(value = "/api/v1/locations/{locationId}/chargers", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class ChargerController {

    private final LocationService locationService;
    private final ChargerService chargerService;

    @Autowired
    public ChargerController(LocationService locationService, ChargerService chargerService) {
        this.locationService = locationService;
        this.chargerService = chargerService;
    }

    @PostMapping
    @PreAuthorize("principal.getUser().isAdmin()")
    @ResponseStatus(HttpStatus.CREATED)
    public ChargerDTO createCharger(@PathVariable long locationId, @Valid @RequestBody CreateCharger req) throws ResourceNotFound {
        final var location = locationService.getLocationById(locationId);
        final var charger = new Charger();
        charger.setMaxPower(req.maxPower);
        charger.setName(req.name);
        charger.setAvailable(req.isAvailable());
        charger.setLocation(location);
        return ChargerDTO.fromCharger(chargerService.createCharger(charger));
    }

}
