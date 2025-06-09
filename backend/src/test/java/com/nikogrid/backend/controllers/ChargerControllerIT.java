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
class ChargerControllerIT {
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
        assertThat(body.isAvailable).isEqualTo(req.isAvailable());

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

}
