package com.jcapucho.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class LocationInterestPoint extends InterestPointBaseDTO {
    public static final String DISCRIMINATOR = "L";

    public final long id;
    @JsonProperty("n")
    public final String name;

    public LocationInterestPoint(float longitude, float latitude, long id, String name) {
        super(longitude, latitude);
        this.id = id;
        this.name = name;
    }
}
