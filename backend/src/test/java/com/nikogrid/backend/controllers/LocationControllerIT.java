package com.nikogrid.backend.controllers;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.dto.ChargerDTO;
import com.nikogrid.backend.dto.ClusterInterestPoint;
import com.nikogrid.backend.dto.CreateCharger;
import com.nikogrid.backend.dto.CreateLocation;
import com.nikogrid.backend.dto.InterestPointBaseDTO;
import com.nikogrid.backend.dto.LocationDTO;
import com.nikogrid.backend.dto.LocationInterestPoint;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.ChargerRepository;
import com.nikogrid.backend.repositories.LocationRepository;
import com.nikogrid.backend.repositories.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
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
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        final User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setPassword("password");
        admin.setAdmin(true);

        this.userRepository.save(admin);

        final User user = new User();
        user.setEmail("user@test.com");
        user.setPassword("password");
        user.setAdmin(false);

        this.userRepository.save(user);
    }

    @AfterEach
    void resetDb() {
        this.chargerRepository.deleteAll();
        this.locationRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createLocationOk() throws Exception {
        final CreateLocation req = new CreateLocation("Test", 30.0f, 20.0f);

        final MvcResult res = mvc.perform(post("/api/v1/locations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final LocationDTO body = objectMapper.readValue(res.getResponse().getContentAsString(), LocationDTO.class);

        assertThat(body.getName()).isEqualTo(req.name);
        assertThat(body.getLat()).isEqualTo(req.lat);
        assertThat(body.getLon()).isEqualTo(req.lon);

        assertThat(this.locationRepository.findAll()).hasSize(1).extracting(Location::getId).containsExactlyInAnyOrder(body.id);
    }

    @Test
    @WithUserDetails(value = "user@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createLocationNoAuthz() throws Exception {
        final CreateLocation req = new CreateLocation("Test", 30.0f, 20.0f);

        mvc.perform(post("/api/v1/locations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        assertThat(this.locationRepository.findAll()).hasSize(0);
    }

    @Test
    @Requirement("NIK-37")
    void getNearbyLocationsOk() throws Exception {
        final Location noCluster = createTestLocation("Test3", 15, 15);

        this.locationRepository.saveAll(List.of(createTestLocation("Test1", -10, 5), createTestLocation("Test2", -11, 5), noCluster, createTestLocation("Test4", -30, 10)));

        final MvcResult res = mvc.perform(get("/api/v1/locations/nearby").param("w", "-20").param("e", "20").param("s", "-20").param("n", "20").param("z", "0")).andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE)).andReturn();

        final List<InterestPointBaseDTO> body = objectMapper.readValue(res.getResponse().getContentAsString(), new TypeReference<>() {
        });

        assertThat(body).isNotEmpty().hasSize(2).containsExactlyInAnyOrder(new ClusterInterestPoint(-10.5f, 5, 2), new LocationInterestPoint(15, 15, noCluster.getId(), "Test3"));
    }

    @Test
    @Requirement("NIK-24")
    void getClosestAvailableLocationNotFound() throws Exception {
        final Location closestNoChargers = createTestLocation("Test1", 1, 0);
        final Location closestNoAvailable = createTestLocation("Test1", 0, 1);

        this.locationRepository.saveAll(List.of(closestNoChargers, closestNoAvailable));

        this.chargerRepository.saveAll(List.of(createTestCharger(closestNoAvailable, "E1", false)));

        mvc.perform(get("/api/v1/locations/closest").param("lat", "0").param("lon", "0")).andExpect(status().isNotFound()).andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @Requirement("NIK-24")
    void getClosestAvailableLocationOk() throws Exception {
        final Location closestNoChargers = createTestLocation("Test1", 1, 0);
        final Location closestNoAvailable = createTestLocation("Test1", 0, 1);
        final Location closestAvailable = createTestLocation("Test1", 2, 3);
        final Location furthestAvailable = createTestLocation("Test1", 5, 5);

        this.locationRepository.saveAll(List.of(closestNoChargers, closestNoAvailable, closestAvailable, furthestAvailable));

        this.chargerRepository.saveAll(List.of(createTestCharger(closestNoAvailable, "E1", false), createTestCharger(closestAvailable, "E2", false), createTestCharger(closestAvailable, "E3", true), createTestCharger(furthestAvailable, "E4", false)));

        final MvcResult res = mvc.perform(get("/api/v1/locations/closest")
                    .param("lat", "0")
                    .param("lon", "0"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final LocationDTO body = objectMapper.readValue(res.getResponse().getContentAsString(), LocationDTO.class);

        assertThat(body.getId()).isEqualTo(closestAvailable.getId());
        assertThat(body.getName()).isEqualTo(closestAvailable.getName());
        assertThat(body.getLat()).isEqualTo(closestAvailable.getLat());
        assertThat(body.getLon()).isEqualTo(closestAvailable.getLon());
    }

    @Test
    @Requirement("NIK-37")
    void getLocationByIdNotFound() throws Exception {
        mvc.perform(get("/api/v1/locations/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @Requirement("NIK-37")
    void getLocationByIdOk() throws Exception {
        final Location loc = createTestLocation("Test", 20, 30);

        this.locationRepository.save(loc);

        final LocationDTO expected = LocationDTO.fromLocation(loc);

        mvc.perform(get("/api/v1/locations/{id}", loc.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createChargerOk() throws Exception {
        final var loc = this.locationRepository.save(createTestLocation("Test", 20, 30));


        final CreateCharger req = new CreateCharger();
        req.maxPower = 250;
        req.name = "AAA1";

        final var res = mvc.perform(post("/api/v1/locations/{id}", loc.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andReturn();

        final ChargerDTO body = objectMapper.readValue(res.getResponse().getContentAsString(), ChargerDTO.class);
        assertThat(body.name).isEqualTo(req.name);
        assertThat(body.maxPower).isEqualTo(req.maxPower);
        assertThat(body.isAvailable).isEqualTo(req.available);

        assertThat(this.chargerRepository.findAll())
                .hasSize(1)
                .extracting(Charger::getId)
                .containsExactlyInAnyOrder(body.id);

    }

    @Test
    @WithUserDetails(value = "admin@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createChargerNoLocation() throws Exception {
        final CreateCharger req = new CreateCharger();
        req.maxPower = 250;
        req.name = "AAA1";

        mvc.perform(post("/api/v1/locations/10")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @WithUserDetails(value = "user@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createChargerNoAuthz() throws Exception {
        final var loc = this.locationRepository.save(createTestLocation("Test", 20, 30));


        final CreateCharger req = new CreateCharger();
        req.maxPower = 250;
        req.name = "AAA1";

        mvc.perform(post("/api/v1/locations/{id}", loc.getId())
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
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
