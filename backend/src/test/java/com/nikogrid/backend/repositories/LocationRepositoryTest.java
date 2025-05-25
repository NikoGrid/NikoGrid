package com.nikogrid.backend.repositories;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.InterestPoint;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.repositories.projections.LocationListing;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class LocationRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LocationRepository locationRepository;

    @Test
    @Requirement("NIK-20")
    void locationSaveWorks() {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        final Location newLocation = locationRepository.save(location);
        assertThat(newLocation.getId()).isNotNull();
        assertThat(newLocation.getGeom().getPosition().getLon()).isEqualTo(20.0);
        assertThat(newLocation.getGeom().getPosition().getLat()).isEqualTo(30.0);
    }

    @Test
    @Requirement("NIK-37")
    void findInterestPointsReturnsClustered() {
        final float[][] points = {
                // Cluster 1
                {21.0f, 30.0f},
                {22.0f, 30.0f},
                // Cluster 2
                {24.0f, 30.0f},
                {25.0f, 30.0f},
                // No cluster 1
                {28.0f, 30.0f},
                // No cluster 2
                {28.0f, 33.0f},
                // Outside envelope
                {15.0f, 30.0f},
                {35.0f, 30.0f},
                {25.0f, 20.0f},
                {25.0f, 40.0f}
        };
        for (int i = 0; i < points.length; i++) {
            final Location location = new Location();
            location.setName(Integer.toString(i));
            location.setLon(points[i][0]);
            location.setLat(points[i][1]);
            entityManager.persistAndFlush(location);
        }

        final Stream<InterestPoint> interestPoints = locationRepository.findInterestPoints(20, 25, 30, 35, 1);
        assertThat(interestPoints)
                .isNotEmpty()
                .hasSize(4)
                .extracting(InterestPoint::numPoints, InterestPoint::isClustered, InterestPoint::centroid, InterestPoint::name)
                .containsExactlyInAnyOrder(
                        tuple(2L, true, Geometries.mkPoint(new G2D(21.5f, 30.0f), CoordinateReferenceSystems.WGS84), null),
                        tuple(2L, true, Geometries.mkPoint(new G2D(24.5f, 30.0f), CoordinateReferenceSystems.WGS84), null),
                        tuple(0L, false, Geometries.mkPoint(new G2D(28.0f, 30.0f), CoordinateReferenceSystems.WGS84), "4"),
                        tuple(0L, false, Geometries.mkPoint(new G2D(28.0f, 33.0f), CoordinateReferenceSystems.WGS84), "5")
                );
    }

    @Test
    @Requirement("NIK-37")
    void getLocationsInEnvelopeReturnsInside() {
        final float[][] points = {
                // Inside
                {2.0f, 2.5f},
                {-2.0f, -2.5f},
                {0.0f, 0.5f},
                // Outside
                {4.0f, 0.0f},
                {-4.0f, 0.0f},
                {0.0f, 4.0f},
                {0.0f, -4.0f},
        };
        for (int i = 0; i < points.length; i++) {
            final Location location = new Location();
            location.setName(Integer.toString(i));
            location.setLon(points[i][0]);
            location.setLat(points[i][1]);
            entityManager.persistAndFlush(location);
        }

        final Stream<LocationListing> interestPoints = locationRepository.getLocationsInEnvelope(-3, -3, 3, 3);
        assertThat(interestPoints)
                .isNotEmpty()
                .hasSize(3)
                .extracting(LocationListing::getName, LocationListing::getLon, LocationListing::getLat)
                .containsExactlyInAnyOrder(
                        tuple("0", 2.0f, 2.5f),
                        tuple("1", -2.0f, -2.5f),
                        tuple("2", 0.0f, 0.5f)
                );
    }

    @Test
    @Requirement("NIK-24")
    void findClosestAvailableReturnsAvailable() {
        final Location location1 = new Location();
        location1.setName("1");
        location1.setLon(2);
        location1.setLat(1);
        entityManager.persistAndFlush(location1);

        final Location location2 = new Location();
        location2.setName("2");
        location2.setLon(-2);
        location2.setLat(2);
        entityManager.persistAndFlush(location2);

        final Location location3 = new Location();
        location3.setName("3");
        location3.setLon(2.5f);
        location3.setLat(2.7f);
        entityManager.persistAndFlush(location3);

        final Charger charger1 = new Charger();
        charger1.setName("E1");
        charger1.setLocation(location1);
        charger1.setAvailable(false);
        charger1.setMaxPower(220);
        entityManager.persistAndFlush(charger1);

        final Charger charger2 = new Charger();
        charger2.setName("E2");
        charger2.setLocation(location2);
        charger2.setAvailable(false);
        charger2.setMaxPower(220);
        entityManager.persistAndFlush(charger2);

        final Charger charger3 = new Charger();
        charger3.setName("E3");
        charger3.setLocation(location2);
        charger3.setAvailable(true);
        charger3.setMaxPower(220);
        entityManager.persistAndFlush(charger3);

        final Charger charger4 = new Charger();
        charger4.setName("E4");
        charger4.setLocation(location3);
        charger4.setAvailable(true);
        charger4.setMaxPower(220);
        entityManager.persistAndFlush(charger4);

        final Optional<Location> res = locationRepository.findClosestAvailable(0, 0);
        assertThat(res)
                .isNotEmpty()
                .get()
                .isEqualTo(location2);
    }


    @Test
    @Requirement("NIK-24")
    void findClosestAvailableReturnsEmpty() {
        final Location location1 = new Location();
        location1.setName("1");
        location1.setLon(2);
        location1.setLat(1);
        entityManager.persistAndFlush(location1);

        final Location location2 = new Location();
        location2.setName("2");
        location2.setLon(-2);
        location2.setLat(2);
        entityManager.persistAndFlush(location2);

        final Charger charger1 = new Charger();
        charger1.setName("E1");
        charger1.setLocation(location1);
        charger1.setAvailable(false);
        charger1.setMaxPower(220);
        entityManager.persistAndFlush(charger1);

        final Charger charger2 = new Charger();
        charger2.setName("E2");
        charger2.setLocation(location2);
        charger2.setAvailable(false);
        charger2.setMaxPower(220);
        entityManager.persistAndFlush(charger2);

        final Charger charger3 = new Charger();
        charger3.setName("E3");
        charger3.setLocation(location2);
        charger3.setAvailable(false);
        charger3.setMaxPower(220);
        entityManager.persistAndFlush(charger3);

        final Optional<Location> res = locationRepository.findClosestAvailable(0, 0);
        assertThat(res).isEmpty();
    }
}
