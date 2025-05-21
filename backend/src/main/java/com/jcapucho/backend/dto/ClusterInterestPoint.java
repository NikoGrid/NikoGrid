package com.jcapucho.backend.dto;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public final class ClusterInterestPoint extends InterestPointBaseDTO {
    public static final String DISCRIMINATOR = "C";

    public final long numPoints;

    public ClusterInterestPoint(float longitude, float latitude, long numPoints) {
        super(longitude, latitude);
        this.numPoints = numPoints;
    }
}
