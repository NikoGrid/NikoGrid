package com.nikogrid.backend.services;

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
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationServiceImpl reservationServiceImpl;

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
        // Reservation in 2 hours
        final Reservation reservation1 = new Reservation();
        reservation1.setUser(user);
        reservation1.setCharger(charger);

        reservation1.setStartsAt(Instant.now().plus(2, ChronoUnit.HOURS));
        reservation1.setEndsAt(Instant.now().plus(3, ChronoUnit.HOURS));


        // Reservation was yesterday
        final Reservation reservation2 = new Reservation();
        reservation2.setUser(user);
        reservation2.setCharger(charger);

        reservation2.setStartsAt(Instant.now().minus(1, ChronoUnit.DAYS));
        reservation2.setEndsAt(Instant.now().plus(2, ChronoUnit.HOURS).minus(1, ChronoUnit.DAYS));


        // Reservation is in 2 hours and 30 minutes
        final Reservation reservation3 = new Reservation();
        reservation3.setUser(user);
        reservation3.setCharger(charger);

        reservation3.setStartsAt(Instant.now().plus(2, ChronoUnit.HOURS).plus(30, ChronoUnit.MINUTES));
        reservation3.setEndsAt(Instant.now().plus(3, ChronoUnit.HOURS));


        // Reservation was 2 days ago
        final Reservation reservation4 = new Reservation();
        reservation4.setUser(user);
        reservation4.setCharger(charger);

        reservation4.setStartsAt(Instant.now().minus(2, ChronoUnit.DAYS));
        reservation4.setEndsAt(Instant.now().plus(2, ChronoUnit.HOURS).minus(1, ChronoUnit.DAYS));

        this.reservationRepository.save(reservation1);
        this.reservationRepository.save(reservation2);
        this.reservationRepository.save(reservation3);
        this.reservationRepository.save(reservation4);

        this.reservationServiceImpl.getUserReservations(this.user);
        Mockito.verify(this.reservationRepository, Mockito.times(1)).getUserReservations(this.user.getId());
    }

}