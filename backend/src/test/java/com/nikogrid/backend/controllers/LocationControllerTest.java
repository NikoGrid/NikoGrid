package com.nikogrid.backend.controllers;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.TestSecurityBeans;
import com.nikogrid.backend.auth.SecurityConfig;
import com.nikogrid.backend.dto.ClusterInterestPoint;
import com.nikogrid.backend.dto.CreateLocation;
import com.nikogrid.backend.dto.InterestPointBaseDTO;
import com.nikogrid.backend.dto.LocationDTO;
import com.nikogrid.backend.dto.LocationInterestPoint;
import com.nikogrid.backend.entities.BackendUserDetails;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LocationController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, TestSecurityBeans.class})
class LocationControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @MockitoBean
    private LocationService locationService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        final User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setAdmin(true);

        Mockito.when(userDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenReturn(new BackendUserDetails(user));
    }

    @Test
    @Requirement("NIK-20")
    void createLocationNoAuth() throws Exception {
        final CreateLocation req = new CreateLocation(
                "Test",
                30F,
                20F
        );

        mvc.perform(post("/api/v1/locations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    @Requirement("NIK-20")
    void createLocationBadRequest() throws Exception {
        final CreateLocation req = new CreateLocation(
                "",
                180F,
                360F
        );

        mvc.perform(post("/api/v1/locations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(locationService, Mockito.never()).createLocation(Mockito.any());
    }

    @Test
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createLocationOk() throws Exception {
        Mockito.when(locationService.createLocation(Mockito.any())).thenAnswer(i -> {
            final Location l = i.getArgument(0);
            l.setId(1L);
            return l;
        });

        final CreateLocation req = new CreateLocation(
                "Good",
                20F,
                30F
        );

        final LocationDTO expected = new LocationDTO(
                1L,
                "Good",
                20,
                30
        );

        mvc.perform(post("/api/v1/locations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

        Mockito.verify(locationService, Mockito.times(1)).createLocation(Mockito.any());
    }

    @ParameterizedTest
    @CsvSource(textBlock = """
            # West, East, South, North, Zoom
            0,1,0,-91,0
            0,1,0,91,0
            0,1,-91,1,0
            0,1,91,1,0
            0,-181,0,1,0
            0,181,0,1,0
            -181,1,0,1,0
            181,1,0,1,0
            0,1,0,1,-1
            0,1,0,1,19
            1,0,0,1,0
            0,1,1,0,0
            """)
    @Requirement("NIK-37")
    void getNearbyLocationsBadData(float west, float east, float south, float north, int zoom) throws Exception {
        mvc.perform(get("/api/v1/locations/nearby")
                        .param("w", Float.toString(west))
                        .param("e", Float.toString(east))
                        .param("s", Float.toString(south))
                        .param("n", Float.toString(north))
                        .param("z", Integer.toString(zoom))
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(locationService, Mockito.never()).getNearbyLocations(
                Mockito.anyFloat(),
                Mockito.anyFloat(),
                Mockito.anyFloat(),
                Mockito.anyFloat(),
                Mockito.anyInt()
        );
    }

    @Test
    @Requirement("NIK-37")
    void getNearbyLocationsOk() throws Exception {
        final List<InterestPointBaseDTO> res = List.of(
                new ClusterInterestPoint(21.5f, 30.0f, 2),
                new LocationInterestPoint(21.5f, 30.0f, 2, "Test")
        );

        Mockito.when(locationService.getNearbyLocations(
                Mockito.anyFloat(),
                Mockito.anyFloat(),
                Mockito.anyFloat(),
                Mockito.anyFloat(),
                Mockito.anyInt()
        )).thenReturn(res);

        mvc.perform(get("/api/v1/locations/nearby")
                        .param("w", "-180")
                        .param("e", "180")
                        .param("s", "-90")
                        .param("n", "90")
                        .param("z", "0")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(res)));

        Mockito.verify(locationService, Mockito.times(1)).getNearbyLocations(
                -180,
                -90,
                180,
                90,
                0
        );
    }

    @Test
    @Requirement("NIK-24")
    void getClosestAvailableLocationBadData() throws Exception {
        mvc.perform(get("/api/v1/locations/closest")
                        .param("lat", "-190")
                        .param("lon", "100")
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(locationService, Mockito.never()).getClosestAvailable(
                Mockito.anyFloat(),
                Mockito.anyFloat()
        );
    }

    @Test
    @Requirement("NIK-24")
    void getClosestAvailableLocationNotFound() throws Exception {
        Mockito.when(locationService.getClosestAvailable(
                Mockito.anyFloat(),
                Mockito.anyFloat()
        )).thenThrow(new ResourceNotFound());

        mvc.perform(get("/api/v1/locations/closest")
                        .param("lat", "30")
                        .param("lon", "20")
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(locationService, Mockito.times(1)).getClosestAvailable(
                20,
                30
        );
    }

    @Test
    @Requirement("NIK-24")
    void getClosestAvailableLocationOk() throws Exception {
        final Location location = new Location();
        location.setId(1L);
        location.setName("Test");
        location.setLat(35.0f);
        location.setLon(25.0f);

        final LocationDTO res = LocationDTO.fromLocation(location);

        Mockito.when(locationService.getClosestAvailable(
                Mockito.anyFloat(),
                Mockito.anyFloat()
        )).thenReturn(location);

        mvc.perform(get("/api/v1/locations/closest")
                        .param("lat", "30")
                        .param("lon", "20")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(res)));

        Mockito.verify(locationService, Mockito.times(1)).getClosestAvailable(
                20,
                30
        );
    }

    @Test
    @Requirement("NIK-37")
    void getLocationByIdNotFound() throws Exception {
        Mockito.when(locationService.getLocationById(1L)).thenThrow(new ResourceNotFound());

        mvc.perform(get("/api/v1/locations/1"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(locationService, Mockito.times(1)).getLocationById(1L);
    }

    @Test
    @Requirement("NIK-37")
    void getLocationByIdOk() throws Exception {
        final Location location = new Location();
        location.setId(1L);
        location.setName("Test");
        location.setLat(35.0f);
        location.setLon(25.0f);

        final LocationDTO res = LocationDTO.fromLocation(location);

        Mockito.when(locationService.getLocationById(1L)).thenReturn(location);

        mvc.perform(get("/api/v1/locations/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(res)));

        Mockito.verify(locationService, Mockito.times(1)).getLocationById(1L);
    }
}