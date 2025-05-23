package com.nikogrid.backend.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.dto.ClusterInterestPoint;
import com.nikogrid.backend.dto.CreateLocation;
import com.nikogrid.backend.dto.InterestPointBaseDTO;
import com.nikogrid.backend.dto.LocationDTO;
import com.nikogrid.backend.dto.LocationInterestPoint;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.repositories.ChargerRepository;
import com.nikogrid.backend.repositories.LocationRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestcontainersConfiguration.class)
class LocationControllerIT {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @AfterEach
    void resetDb() {
        this.chargerRepository.deleteAll();
        this.locationRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void createLocationOk() throws Exception {
        final CreateLocation req = new CreateLocation(
                "Test",
                30.0f,
                20.0f
        );

        final MvcResult res = mvc.perform(post("/api/v1/locations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final LocationDTO body =
                objectMapper.readValue(res.getResponse().getContentAsString(), LocationDTO.class);

        assertThat(body.getName()).isEqualTo(req.name);
        assertThat(body.getLat()).isEqualTo(req.lat);
        assertThat(body.getLon()).isEqualTo(req.lon);

        assertThat(this.locationRepository.findAll())
                .hasSize(1)
                .extracting(Location::getId)
                .containsExactlyInAnyOrder(body.id);
    }

    @Test
    void getNearbyLocationsOk() throws Exception {
        final Location noCluster = createTestLocation("Test3", 15, 15);

        this.locationRepository.saveAll(List.of(
                createTestLocation("Test1", -10, 5),
                createTestLocation("Test2", -11, 5),
                noCluster,
                createTestLocation("Test4", -30, 10)
        ));

        final MvcResult res = mvc.perform(get("/api/v1/locations/nearby")
                        .param("w", "-20")
                        .param("e", "20")
                        .param("s", "-20")
                        .param("n", "20")
                        .param("z", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final List<InterestPointBaseDTO> body = objectMapper.readValue(
                res.getResponse().getContentAsString(),
                new TypeReference<>() {
                }
        );

        assertThat(body)
                .isNotEmpty()
                .hasSize(2)
                .containsExactlyInAnyOrder(
                        new ClusterInterestPoint(-10.5f, 5, 2),
                        new LocationInterestPoint(15, 15, noCluster.getId(), "Test3")
                );
    }

    @Test
    void getClosestAvailableLocationNotFound() throws Exception {
        final Location closestNoChargers = createTestLocation("Test1", 1, 0);
        final Location closestNoAvailable = createTestLocation("Test1", 0, 1);

        this.locationRepository.saveAll(List.of(
                closestNoChargers,
                closestNoAvailable
        ));

        this.chargerRepository.saveAll(List.of(
                createTestCharger(closestNoAvailable, "E1", false)
        ));

        mvc.perform(get("/api/v1/locations/closest")
                        .param("lat", "0")
                        .param("lon", "0"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void getClosestAvailableLocationOk() throws Exception {
        final Location closestNoChargers = createTestLocation("Test1", 1, 0);
        final Location closestNoAvailable = createTestLocation("Test1", 0, 1);
        final Location closestAvailable = createTestLocation("Test1", 2, 3);
        final Location furthestAvailable = createTestLocation("Test1", 5, 5);

        this.locationRepository.saveAll(List.of(
                closestNoChargers,
                closestNoAvailable,
                closestAvailable,
                furthestAvailable
        ));

        this.chargerRepository.saveAll(List.of(
                createTestCharger(closestNoAvailable, "E1", false),
                createTestCharger(closestAvailable, "E2", false),
                createTestCharger(closestAvailable, "E3", true),
                createTestCharger(furthestAvailable, "E4", false)
        ));

        final MvcResult res = mvc.perform(get("/api/v1/locations/closest")
                        .param("lat", "0")
                        .param("lon", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final LocationDTO body =
                objectMapper.readValue(res.getResponse().getContentAsString(), LocationDTO.class);

        assertThat(body.getId()).isEqualTo(closestAvailable.getId());
        assertThat(body.getName()).isEqualTo(closestAvailable.getName());
        assertThat(body.getLat()).isEqualTo(closestAvailable.getLat());
        assertThat(body.getLon()).isEqualTo(closestAvailable.getLon());
    }

    @Test
    void getLocationByIdNotFound() throws Exception {
        mvc.perform(get("/api/v1/locations/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    void getLocationByIdOk() throws Exception {
        final Location loc = createTestLocation("Test", 20, 30);

        this.locationRepository.save(loc);

        final LocationDTO expected = LocationDTO.fromLocation(loc);

        mvc.perform(get("/api/v1/locations/{id}", loc.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    private Location createTestLocation(String name, float lon, float lat) {
        final Location loc = new Location();
        loc.setName(name);
        loc.setLon(lon);
        loc.setLat(lat);
        return loc;
    }

    private Charger createTestCharger(Location loc, String name, boolean available) {
        final Charger c = new Charger();
        c.setLocation(loc);
        c.setName(name);
        c.setAvailable(available);
        return c;
    }
}
