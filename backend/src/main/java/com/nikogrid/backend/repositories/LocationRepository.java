package com.nikogrid.backend.repositories;

import com.nikogrid.backend.entities.InterestPoint;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.repositories.projections.LocationListing;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface LocationRepository extends CrudRepository<Location, Long> {
    @NativeQuery(value = """
            SELECT
                COUNT(cluster_loc.cluster_id) AS numPoints,
                ST_Centroid(ST_Collect(geom)) AS centroid,
                CASE COUNT(id) WHEN 1 THEN MIN(id) END AS id,
                CASE COUNT(id) WHEN 1 THEN MIN(name) END AS name
            FROM (
                SELECT
                    id,
                    name,
                    geom,
                    ST_ClusterDBSCAN(geom, eps := ?5, minpoints := 2) OVER () AS cluster_id
                FROM locations l
                WHERE
                    geom && ST_MakeEnvelope(?1, ?2, ?3, ?4, 4326)
                    AND (
                        NOT ?6
                        OR EXISTS (
                            SELECT 1
                            FROM chargers c
                            WHERE c.location_id = l.id AND c.available
                        )
                    )
            ) AS cluster_loc
            GROUP BY COALESCE(cluster_loc.cluster_id, cluster_loc.id);
            """)
    Stream<InterestPoint> findInterestPoints(float minLongitude, float minLatitude, float maxLongitude, float maxLatitude, float clusterRadius, boolean onlyActive);

    @NativeQuery(value = """
            SELECT id, name, lat, lon
            FROM locations l
            WHERE geom && ST_MakeEnvelope(?1, ?2, ?3, ?4, 4326)
            AND (
                NOT ?5
                OR EXISTS (
                    SELECT 1
                    FROM chargers c
                    WHERE c.location_id = l.id AND c.available
                )
            )
            ;
            """)
    Stream<LocationListing> getLocationsInEnvelope(float minLongitude, float minLatitude, float maxLongitude, float maxLatitude, boolean onlyActive);

    @NativeQuery(value = """
            SELECT *
            FROM locations l
            WHERE EXISTS (SELECT 1 FROM chargers c WHERE l.id = c.location_id AND c.available)
            ORDER BY l.geom <-> ST_SetSRID(ST_MAKEPOINT(?1, ?2), 4326)
            LIMIT 1;
            """)
    Optional<Location> findClosestAvailable(float longitude, float latitude);
}
