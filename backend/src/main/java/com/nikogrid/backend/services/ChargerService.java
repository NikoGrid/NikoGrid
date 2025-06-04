package com.nikogrid.backend.services;

import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.exceptions.ResourceNotFound;

public interface ChargerService {
    Charger findChargerById(Long id) throws ResourceNotFound;
}
