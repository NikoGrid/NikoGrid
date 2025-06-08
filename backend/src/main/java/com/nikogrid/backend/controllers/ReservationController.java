package com.nikogrid.backend.controllers;

import com.nikogrid.backend.dto.CreateReservation;
import com.nikogrid.backend.dto.ReservationDTO;
import com.nikogrid.backend.dto.ReservationListDTO;
import com.nikogrid.backend.entities.BackendUserDetails;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.exceptions.ChargerUnavailable;
import com.nikogrid.backend.exceptions.ReservationConflict;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.services.ChargerService;
import com.nikogrid.backend.services.ReservationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.stream.Stream;

@RestController
@RequestMapping(value = "/api/v1/reservations", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
public class ReservationController {
    private final ReservationService reservationService;
    private final ChargerService chargerService;

    private final Clock clock;

    @Autowired
    public ReservationController(
            ReservationService reservationService, ChargerService chargerService, Clock clock) {
        this.reservationService = reservationService;
        this.chargerService = chargerService;
        this.clock = clock;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationDTO createLocation(
            @Valid @RequestBody CreateReservation req,
            @AuthenticationPrincipal BackendUserDetails userDetails)
            throws ResourceNotFound, ChargerUnavailable, ReservationConflict {
        if (req.start.isBefore(Instant.now(clock)))
            throw new ConstraintViolationException(
                    "Reservation must start in the future", Set.of());

        if (!req.end.isAfter(req.start))
            throw new ConstraintViolationException("Reservation end must be after start", Set.of());

        final Charger charger = this.chargerService.findChargerById(req.chargedId);

        final Reservation reservation = new Reservation();
        reservation.setUser(userDetails.getUser());
        reservation.setCharger(charger);
        reservation.setStartsAt(req.start);
        reservation.setEndsAt(req.end);

        return ReservationDTO.fromReservation(this.reservationService.create(reservation));
    }

    @GetMapping("/")
    @Operation(
            responses = {
                @ApiResponse(
                        responseCode = "200",
                        content = {
                            @Content(
                                    mediaType = "application/json",
                                    array =
                                            @ArraySchema(
                                                    schema =
                                                            @Schema(
                                                                    implementation =
                                                                            ReservationListDTO
                                                                                    .class)))
                        })
            })
    public Stream<ReservationListDTO> getUserReservations(
            @AuthenticationPrincipal BackendUserDetails userDetails) {
        return this.reservationService.getUserReservations(userDetails.getUser()).stream()
                .map(ReservationListDTO::fromReservation);
    }
}
