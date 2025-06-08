package com.nikogrid.backend.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;

import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.exceptions.ChargerUnavailable;
import com.nikogrid.backend.exceptions.ReservationConflict;
import com.nikogrid.backend.repositories.ReservationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock private ReservationRepository reservationRepository;

    @InjectMocks private ReservationServiceImpl reservationServiceImpl;

    private Charger charger;
    private User user;

    @BeforeEach
    void init() {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.user = new User();
        user.setEmail("test@test.test");
        user.setPassword("test");
        user.setAdmin(true);
    }

    @Test
    @Requirement("NIK-12")
    void createReservationOk() throws ChargerUnavailable, ReservationConflict {
        final Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setCharger(charger);

        reservation.setStartsAt(Instant.parse("2025-01-01T14:30:00.000+00:00"));
        reservation.setEndsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));

        this.reservationServiceImpl.create(reservation);

        // Verify that the service persisted the reservation to the repo
        Mockito.verify(this.reservationRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    @Requirement("NIK-12")
    void createReservationChargerUnavailable() {
        charger.setAvailable(false);

        final Reservation reservation = new Reservation();
        reservation.setUser(user);
        reservation.setCharger(charger);

        reservation.setStartsAt(Instant.parse("2025-01-01T14:30:00.000+00:00"));
        reservation.setEndsAt(Instant.parse("2025-01-01T14:45:00.000+00:00"));

        assertThatThrownBy(() -> this.reservationServiceImpl.create(reservation))
                .isInstanceOf(ChargerUnavailable.class);

        Mockito.verify(this.reservationRepository, Mockito.never()).save(Mockito.any());
    }

    @Test
    @Requirement("NIK-13")
    void getUserReservations() {
        this.reservationServiceImpl.getUserReservations(user);
        Mockito.verify(this.reservationRepository, Mockito.times(1))
                .getUserReservations(user.getId());
    }
}
