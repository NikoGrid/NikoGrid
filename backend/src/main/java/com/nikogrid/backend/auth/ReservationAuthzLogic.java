package com.nikogrid.backend.auth;

import com.nikogrid.backend.entities.BackendUserDetails;
import com.nikogrid.backend.repositories.ReservationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.stereotype.Component;

@Component("reservationAuthz")
public class ReservationAuthzLogic {

    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationAuthzLogic(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public AuthorizationDecision isReservationOwner(BackendUserDetails user, long reservationId) {
        final var reservation = this.reservationRepository.getReservationById(reservationId);

        return new AuthorizationDecision(
                reservation
                        .map((r) -> r.getUser().getId().equals(user.getUser().getId()))
                        .orElse(false));

    }
}
