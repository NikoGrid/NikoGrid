package com.nikogrid.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SchemaProperty(name = "t", schema = @Schema(allowableValues = {LocationInterestPoint.DISCRIMINATOR}))
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
