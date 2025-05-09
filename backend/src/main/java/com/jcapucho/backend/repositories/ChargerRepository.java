package com.jcapucho.backend.repositories;

import com.jcapucho.backend.entities.Charger;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargerRepository extends CrudRepository<Charger, Long> {
}
