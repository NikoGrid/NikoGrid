package com.nikogrid.backend.services;

import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.repositories.ChargerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ChargerServiceImpl implements ChargerService {
    private final ChargerRepository chargerRepository;

    @Autowired
    public ChargerServiceImpl(ChargerRepository chargerRepository) {
        this.chargerRepository = chargerRepository;
    }

    @Override
    public Charger findChargerById(Long id) throws ResourceNotFound {
        return this.chargerRepository.findById(id)
                .orElseThrow(ResourceNotFound::new);
    }

    @Override
    public Charger createCharger(Charger charger) {
        return this.chargerRepository.save(charger);
    }
}
