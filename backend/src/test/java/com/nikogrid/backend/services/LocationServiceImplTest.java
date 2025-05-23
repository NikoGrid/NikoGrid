package com.nikogrid.backend.services;

import com.nikogrid.backend.dto.ClusterInterestPoint;
import com.nikogrid.backend.dto.InterestPointBaseDTO;
import com.nikogrid.backend.dto.LocationInterestPoint;
import com.nikogrid.backend.entities.InterestPoint;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.repositories.LocationRepository;
import com.nikogrid.backend.repositories.projections.LocationListing;
import org.geolatte.geom.G2D;
import org.geolatte.geom.Geometries;
import org.geolatte.geom.crs.CoordinateReferenceSystems;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class LocationServiceImplTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private LocationServiceImpl locationService;

    @Test
    void createLocationPersists() {
        final Location location = new Location();
        location.setName("Test");
        location.setLat(20.0f);
        location.setLon(30.0f);

        this.locationService.createLocation(location);

        // Verify that the service persisted the menu to the repo
        Mockito.verify(this.locationRepository, Mockito.times(1)).save(Mockito.any());
    }

    @Test
    void getLocationByIdReturnsFound() throws ResourceNotFound {
        final Location location = new Location();
        location.setName("Test");
        location.setLat(20.0f);
        location.setLon(30.0f);

        Mockito.when(this.locationRepository.findById(1L)).thenReturn(Optional.of(location));

        final Location result = this.locationService.getLocationById(1L);

        assertThat(result).isNotNull();
    }

    @Test
    void getLocationByIdThrowsNotFound() {
        Mockito.when(this.locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.locationService.getLocationById(1L))
                .isInstanceOf(ResourceNotFound.class);
    }

    @Test
    void getClosestAvailableReturnsFound() throws ResourceNotFound {
        final Location location = new Location();
        location.setName("Test");
        location.setLat(20.0f);
        location.setLon(30.0f);

        Mockito.when(this.locationRepository.findClosestAvailable(Mockito.anyFloat(), Mockito.anyFloat()))
                .thenReturn(Optional.of(location));

        final Location result = this.locationService.getClosestAvailable(20.0f, 30.0f);

        assertThat(result).isNotNull();
    }

    @Test
    void getClosestAvailableThrowsNotFound() {
        Mockito.when(this.locationRepository.findClosestAvailable(Mockito.anyFloat(), Mockito.anyFloat()))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> this.locationService.getClosestAvailable(20.0f, 30.0f))
                .isInstanceOf(ResourceNotFound.class);
    }

    @Test
    void getNearbyLocationsClusterZoom() {
        final InterestPoint ip1 = new InterestPoint(
                2,
                Geometries.mkPoint(new G2D(21.5f, 30.0f), CoordinateReferenceSystems.WGS84),
                null,
                null
        );

        final InterestPoint ip2 = new InterestPoint(
                0,
                Geometries.mkPoint(new G2D(22.5f, 30.0f), CoordinateReferenceSystems.WGS84),
                2L,
                "Test"
        );

        final AtomicBoolean wasClosed = new AtomicBoolean(false);

        Stream<InterestPoint> stream = Stream.of(ip1, ip2);
        stream = stream.onClose(() -> wasClosed.set(true));

        Mockito.when(this.locationRepository.findInterestPoints(Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat()))
                .thenReturn(stream);

        final Collection<InterestPointBaseDTO> result =
                this.locationService.getNearbyLocations(-180, -90, 180, 90, 17);

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new ClusterInterestPoint(21.5f, 30.0f, 2),
                        new LocationInterestPoint(22.5f, 30.0f, 2L, "Test")
                );

        assertThat(wasClosed).isTrue();
    }


    @Test
    void getNearbyLocationsNoClusterZoom() {
        final LocationListing l1 = createLocationListing(1L, "Test1", 21.5f, 30.0f);
        final LocationListing l2 = createLocationListing(2L, "Test2", 22.5f, 30.0f);

        final AtomicBoolean wasClosed = new AtomicBoolean(false);

        Stream<LocationListing> stream = Stream.of(l1, l2);
        stream = stream.onClose(() -> wasClosed.set(true));

        Mockito.when(this.locationRepository.getLocationsInEnvelope(Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat(), Mockito.anyFloat()))
                .thenReturn(stream);

        final Collection<InterestPointBaseDTO> result =
                this.locationService.getNearbyLocations(-180, -90, 180, 90, 18);

        assertThat(result)
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new LocationInterestPoint(21.5f, 30.0f, 1L, "Test1"),
                        new LocationInterestPoint(22.5f, 30.0f, 2L, "Test2")
                );

        assertThat(wasClosed).isTrue();
    }

    private LocationListing createLocationListing(long id, String name, float lon, float lat) {
        return new LocationListing() {
            @Override
            public long getId() {
                return id;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public float getLat() {
                return lat;
            }

            @Override
            public float getLon() {
                return lon;
            }
        };
    }
}