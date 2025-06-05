package com.nikogrid.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class CreateReservation {
    @NotNull
    public final Long chargedId;

    @NotNull
    public final Instant start;

    @NotNull
    public final Instant end;
}
