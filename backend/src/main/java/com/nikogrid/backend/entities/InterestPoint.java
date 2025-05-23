package com.nikogrid.backend.entities;

import org.geolatte.geom.G2D;
import org.geolatte.geom.Point;

public record InterestPoint(long numPoints, Point<G2D> centroid, Long id, String name) {
    public boolean isClustered() {
        return this.numPoints > 1;
    }
}
