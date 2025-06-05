package com.nikogrid.backend.dto;

import com.nikogrid.backend.entities.Charger;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChargerDTO {
    @NotNull
    public final long id;

    @NotNull
    public final String name;

    @NotNull
    public final boolean isAvailable;

    @NotNull
    public final float maxPower;

    public static ChargerDTO fromCharger(Charger charger) {
        return new ChargerDTO(charger.getId(), charger.getName(), charger.isAvailable(), charger.getMaxPower());
    }
}
