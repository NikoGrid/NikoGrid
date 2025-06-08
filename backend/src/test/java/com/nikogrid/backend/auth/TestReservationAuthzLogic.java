package com.nikogrid.backend.auth;

import com.nikogrid.backend.entities.BackendUserDetails;
import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.ReservationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TestReservationAuthzLogic {

    @Mock
    private ReservationRepository reservationRepository;

    @InjectMocks
    private ReservationAuthzLogic reservationAuthzLogic;

    @Test
    void isReservationOwnerOk() {
        final var user = new User();
        final var userUuid = UUID.randomUUID();
        user.setId(userUuid);

        final var backendUser = new BackendUserDetails(user);

        final var reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);

        Mockito.when(reservationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(reservation));

        final var decision = reservationAuthzLogic.isReservationOwner(backendUser, reservation.getId());

        assertThat(decision.isGranted()).isTrue();
    }

    @Test
    void isReservationOwnerNoReservation() {
        final var user = new User();
        final var userUuid = UUID.randomUUID();
        user.setId(userUuid);

        final var backendUser = new BackendUserDetails(user);

        Mockito.when(reservationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.empty());

        final var decision = reservationAuthzLogic.isReservationOwner(backendUser, 1L);

        assertThat(decision.isGranted()).isFalse();
    }

    @Test
    void isReservationOwnerNotOwner() {
        var user = new User();
        var userUuid = UUID.randomUUID();
        user.setId(userUuid);

        final var backendUser = new BackendUserDetails(user);

        user = new User();
        userUuid = UUID.randomUUID();
        user.setId(userUuid);

        final var reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);

        Mockito.when(reservationRepository.findById(Mockito.anyLong()))
                .thenReturn(Optional.of(reservation));

        final var decision = reservationAuthzLogic.isReservationOwner(backendUser, reservation.getId());

        assertThat(decision.isGranted()).isFalse();
    }
}
