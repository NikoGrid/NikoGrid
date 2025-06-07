package com.nikogrid.backend.services;

import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.exceptions.ChargerUnavailable;
import com.nikogrid.backend.exceptions.ReservationConflict;

import java.util.List;

public interface ReservationService {
    Reservation create(Reservation reservation) throws ReservationConflict, ChargerUnavailable;
    List<Reservation> getUserReservations(User user);
}
