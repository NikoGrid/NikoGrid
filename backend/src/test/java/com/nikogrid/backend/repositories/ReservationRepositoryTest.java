package com.nikogrid.backend.repositories;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.entities.User;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ReservationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ReservationRepository reservationRepository;

    private Location location;
    private Charger charger;
    private User user;

    @BeforeEach
    void setUp() {
        this.location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.entityManager.persistAndFlush(location);

        this.charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.entityManager.persistAndFlush(charger);

        this.user = new User();
        user.setEmail("test@test.test");
        user.setPassword("test");
        user.setAdmin(true);

        this.entityManager.persistAndFlush(user);
    }

    @Test
    @Requirement("NIK-12")
    void reservationSaveWorks() {
        final Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setCharger(charger);

        reservation.setStartsAt(Instant.parse("2025-01-01T14:30:00.000+00:00"));
        reservation.setEndsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));

        final Reservation result = this.entityManager.persistAndFlush(reservation);
        assertThat(result.getId()).isNotNull();
    }

    @Test
    @Requirement("NIK-12")
    void reservationOverlaps() {
        final Reservation reservation1 = new Reservation();
        reservation1.setUser(user);
        reservation1.setCharger(charger);

        reservation1.setStartsAt(Instant.parse("2025-01-01T14:30:00.000+00:00"));
        reservation1.setEndsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));

        this.entityManager.persistAndFlush(reservation1);

        final Reservation reservation2 = new Reservation();
        reservation2.setUser(user);
        reservation2.setCharger(charger);

        reservation2.setStartsAt(Instant.parse("2025-01-01T14:40:00.000+00:00"));
        reservation2.setEndsAt(Instant.parse("2025-01-01T14:50:00.000+00:00"));

        assertThatThrownBy(() -> entityManager.persistAndFlush(reservation2))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @Requirement("NIK-12")
    void reservationNoOverlaps() {
        final Reservation reservation1 = new Reservation();
        reservation1.setUser(user);
        reservation1.setCharger(charger);

        reservation1.setStartsAt(Instant.parse("2025-01-01T14:30:00.000+00:00"));
        reservation1.setEndsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));

        this.entityManager.persistAndFlush(reservation1);

        final Reservation reservation2 = new Reservation();
        reservation2.setUser(user);
        reservation2.setCharger(charger);

        reservation2.setStartsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));
        reservation2.setEndsAt(Instant.parse("2025-01-01T14:50:00.000+00:00"));

        assertDoesNotThrow(() -> entityManager.persistAndFlush(reservation2));
    }

    @Test
    @Requirement("NIK-12")
    void reservationOverlapsDifferentChargers() {
        final Reservation reservation1 = new Reservation();
        reservation1.setUser(user);
        reservation1.setCharger(charger);

        reservation1.setStartsAt(Instant.parse("2025-01-01T14:30:00.000+00:00"));
        reservation1.setEndsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));

        this.entityManager.persistAndFlush(reservation1);

        final Charger charger2 = new Charger();
        charger2.setName("AAA2");
        charger2.setAvailable(true);
        charger2.setMaxPower(22.2F);
        charger2.setLocation(this.location);

        this.entityManager.persistAndFlush(charger2);

        final Reservation reservation2 = new Reservation();
        reservation2.setUser(user);
        reservation2.setCharger(charger2);

        reservation2.setStartsAt(Instant.parse("2025-01-01T14:30:00.000+00:00"));
        reservation2.setEndsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));

        assertDoesNotThrow(() -> entityManager.persistAndFlush(reservation2));
    }
}
