package com.nikogrid.backend.controllers;


import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.TestSecurityBeans;
import com.nikogrid.backend.auth.SecurityConfig;
import com.nikogrid.backend.dto.CreateReservation;
import com.nikogrid.backend.dto.ReservationDTO;
import com.nikogrid.backend.entities.BackendUserDetails;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.exceptions.ResourceNotFound;
import com.nikogrid.backend.services.ChargerService;
import com.nikogrid.backend.services.ReservationService;
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
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReservationController.class)
@ActiveProfiles("test")
@Import({SecurityConfig.class, TestSecurityBeans.class})
class ReservationControllerTest {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @MockitoBean
    private ChargerService chargerService;

    @MockitoBean
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestBean
    private Clock clock;

    @MockitoBean
    private UserDetailsService userDetailsService;

    static Clock clock() {
        return Clock.fixed(
                Instant.parse("2024-01-01T12:00:00.000Z"),
                ZoneId.of("UTC")
        );
    }

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        final User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setAdmin(false);

        Mockito.when(userDetailsService.loadUserByUsername(Mockito.anyString()))
                .thenReturn(new BackendUserDetails(user));
    }

    @Test
    @Requirement("NIK-12")
    void createReservationNoAuth() throws Exception {
        final CreateReservation req = new CreateReservation(
                1L,
                OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant(),
                OffsetDateTime.of(2024, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC).toInstant()
        );

        mvc.perform(post("/api/v1/reservations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    @CsvSource(textBlock = """
            # Start, End
            # End before start
            2024-01-01T23:00:00+00:00,2024-01-01T22:00:00+00:00
            # Start before now
            2023-12-31T22:00:00+00:00,2024-01-01T22:00:00+00:00
            """)
    void createReservationBadRequest(String start, String end) throws Exception {
        final CreateReservation req = new CreateReservation(
                1L,
                Instant.parse(start),
                Instant.parse(end)
        );

        mvc.perform(post("/api/v1/reservations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(reservationService, Mockito.never()).create(Mockito.any());
    }

    @Test
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    void createReservationNoCharger() throws Exception {
        final CreateReservation req = new CreateReservation(
                1L,
                Instant.parse("2024-01-01T22:00:00.000+00:00"),
                Instant.parse("2024-01-01T23:00:00.000+00:00")
        );

        Mockito.when(chargerService.findChargerById(Mockito.anyLong())).thenThrow(new ResourceNotFound());

        mvc.perform(post("/api/v1/reservations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));

        Mockito.verify(reservationService, Mockito.never()).create(Mockito.any());
    }

    @Test
    @WithUserDetails(setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    void createReservationOk() throws Exception {
        final Location location = new Location();
        location.setId(1L);
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        final Charger charger = new Charger();
        charger.setId(1L);
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        Mockito.when(chargerService.findChargerById(Mockito.anyLong())).thenReturn(charger);
        Mockito.when(reservationService.create(Mockito.any())).thenAnswer(i -> {
            final Reservation r = i.getArgument(0);
            r.setId(1L);
            return r;
        });

        final CreateReservation req = new CreateReservation(
                1L,
                Instant.parse("2024-01-01T22:00:00.000+00:00"),
                Instant.parse("2024-01-01T23:00:00.000+00:00")
        );

        final ReservationDTO expected = new ReservationDTO(
                1L,
                1L,
                req.start,
                req.end
        );

        mvc.perform(post("/api/v1/reservations/")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(objectMapper.writeValueAsString(expected)));

        Mockito.verify(reservationService, Mockito.times(1)).create(Mockito.any());
    }
}
