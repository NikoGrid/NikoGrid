package com.nikogrid.backend.controllers;


import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.nikogrid.backend.TestSecurityBeans;
import com.nikogrid.backend.auth.SecurityConfig;
import com.nikogrid.backend.dto.ChargerDTO;
import com.nikogrid.backend.dto.CreateCharger;
import com.nikogrid.backend.dto.CreateLocation;
import com.nikogrid.backend.entities.BackendUserDetails;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.services.ChargerService;
import com.nikogrid.backend.services.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@WebMvcTest(ChargerController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, TestSecurityBeans.class})
public class ChargerControllerTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @MockitoBean
    private LocationService locationService;

    @MockitoBean
    private ChargerService chargerService;

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
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createChargerNoLocation() throws Exception {
        final CreateCharger req = new CreateCharger();
        req.setAvailable(true);
        req.name = "AAA1";
        req.maxPower = 250;

        Mockito.when(locationService.getLocationById(Mockito.anyLong())).thenThrow(ResourceNotFound.class);

        mvc.perform(post("/api/v1/locations/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(chargerService, Mockito.times(0)).createCharger(Mockito.any());
    }

    @Test
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-20")
    void createChargerOk() throws Exception {
        Mockito.when(chargerService.createCharger(Mockito.any())).thenAnswer(i -> {
            final Charger l = i.getArgument(0);
            l.setId(1L);
            return l;
        });

        Mockito.when(locationService.getLocationById(Mockito.anyLong())).thenAnswer(i -> {
            final Location l = new Location();
            l.setId(1L);
            l.setName("AAA1");
            l.setLat(0);
            l.setLon(0);
            return l;
        });

        final CreateCharger req = new CreateCharger();
        req.setAvailable(true);
        req.name = "AAA1";
        req.maxPower = 250;

        final ChargerDTO expected = new ChargerDTO(
                1L,
                "AAA1",
                true,
                250
        );

        mvc.perform(post("/api/v1/locations/1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

        Mockito.verify(chargerService, Mockito.times(1)).createCharger(Mockito.any());
    }
}
