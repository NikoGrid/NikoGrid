package com.nikogrid.backend.repositories;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ChargerRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ChargerRepository chargerRepository;

    @Test
    @Requirement("NIK-20")
    void chargerSaveWorks() {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.entityManager.persistAndFlush(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        final Charger savedCharger = this.chargerRepository.save(charger);
        assertThat(savedCharger.getId()).isNotNull();
    }
}
