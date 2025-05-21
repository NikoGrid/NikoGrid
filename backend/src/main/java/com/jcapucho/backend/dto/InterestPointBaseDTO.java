package com.jcapucho.backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.jcapucho.backend.entities.InterestPoint;
import com.jcapucho.backend.repositories.projections.LocationListing;
import lombok.EqualsAndHashCode;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "t"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ClusterInterestPoint.class, name = ClusterInterestPoint.DISCRIMINATOR),
        @JsonSubTypes.Type(value = LocationInterestPoint.class, name = LocationInterestPoint.DISCRIMINATOR)
})
@EqualsAndHashCode
public abstract sealed class InterestPointBaseDTO permits ClusterInterestPoint, LocationInterestPoint {
    @JsonProperty("lat")
    public final float latitude;

    @JsonProperty("lon")
    public final float longitude;

    protected InterestPointBaseDTO(float longitude, float latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public static InterestPointBaseDTO fromInterestPoint(InterestPoint ip) {
        final float lon = (float) ip.centroid().getPosition().getLon();
        final float lat = (float) ip.centroid().getPosition().getLat();

        if (ip.isClustered())
            return new ClusterInterestPoint(lon, lat, ip.numPoints());
        else
            return new LocationInterestPoint(lon, lat, ip.id(), ip.name());
    }

    public static InterestPointBaseDTO fromLocationListing(LocationListing loc) {
        return new LocationInterestPoint(loc.getLon(), loc.getLat(), loc.getId(), loc.getName());
    }
}
