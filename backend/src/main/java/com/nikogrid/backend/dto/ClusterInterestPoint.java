package com.nikogrid.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@SchemaProperty(name = "t", schema = @Schema(allowableValues = {ClusterInterestPoint.DISCRIMINATOR}))
public final class ClusterInterestPoint extends InterestPointBaseDTO {
    public static final String DISCRIMINATOR = "C";

    @NotNull
    public final long numPoints;

    public ClusterInterestPoint(float longitude, float latitude, long numPoints) {
        super(longitude, latitude);
        this.numPoints = numPoints;
    }
}
