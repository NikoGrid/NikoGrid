package com.nikogrid.backend.dto;

import com.nikogrid.backend.entities.Reservation;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class ReservationDTO {
    @NotNull
    public final long id;

    @NotNull
    public final long chargerId;

    @NotNull
    public final Instant start;

    @NotNull
    public final Instant end;

    public static ReservationDTO fromReservation(Reservation reservation) {
        return new ReservationDTO(
                reservation.getId(),
                reservation.getCharger().getId(),
                reservation.getStartsAt(),
                reservation.getEndsAt()
        );
    }
}
