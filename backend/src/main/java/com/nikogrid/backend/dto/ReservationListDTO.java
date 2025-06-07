package com.nikogrid.backend.dto;

import com.nikogrid.backend.entities.Reservation;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class ReservationListDTO {
    @NotNull
    public final long id;

    @NotNull
    public final long chargerId;

    @NotNull
    public final Instant start;

    @NotNull
    public final Instant end;

    @NotNull
    public String charger;

    @NotNull
    public String location;

    @NotNull
    public float maxPower;



    public static ReservationListDTO fromReservation(Reservation reservation) {
        return new ReservationListDTO(
                reservation.getId(),
                reservation.getCharger().getId(),
                reservation.getStartsAt(),
                reservation.getEndsAt(),
                reservation.getCharger().getName(),
                reservation.getCharger().getLocation().getName(),
                reservation.getCharger().getMaxPower()
        );
    }
}
