package com.nikogrid.backend.repositories;

import com.nikogrid.backend.entities.Charger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargerRepository extends CrudRepository<Charger, Long> {
    Charger getChargerById(int i);
}
