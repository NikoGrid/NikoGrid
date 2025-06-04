package com.nikogrid.backend.services;

import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.exceptions.ChargerUnavailable;
import com.nikogrid.backend.exceptions.ReservationConflict;
import com.nikogrid.backend.repositories.ReservationRepository;
import org.postgresql.util.PSQLException;
import org.postgresql.util.ServerErrorMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository reservationRepository;

    @Autowired
    public ReservationServiceImpl(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    @Override
    public Reservation create(Reservation reservation) throws ChargerUnavailable, ReservationConflict {
        if (!reservation.getCharger().isAvailable())
            throw new ChargerUnavailable();

        try {
            return this.reservationRepository.save(reservation);
        } catch (DataIntegrityViolationException exc) {
            if (isReservationOverlapViolation(exc))
                throw new ReservationConflict();

            throw exc;
        }
    }

    private boolean isReservationOverlapViolation(DataIntegrityViolationException e) {
        if (!(e.getRootCause() instanceof PSQLException psqlException))
            return false;

        final ServerErrorMessage serverErrorMessage = psqlException.getServerErrorMessage();

        if (serverErrorMessage == null)
            return false;

        return Objects.equals(serverErrorMessage.getConstraint(), "ec_working_hours_overlap");
    }
}
