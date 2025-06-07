package com.nikogrid.backend.controllers;

import app.getxray.xray.junit.customjunitxml.annotations.Requirement;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikogrid.backend.TestcontainersConfiguration;
import com.nikogrid.backend.dto.CreateReservation;
import com.nikogrid.backend.dto.ReservationDTO;
import com.nikogrid.backend.dto.ReservationListDTO;
import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.ChargerRepository;
import com.nikogrid.backend.repositories.LocationRepository;
import com.nikogrid.backend.repositories.ReservationRepository;
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
import org.springframework.test.context.bean.override.convention.TestBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@Import(TestcontainersConfiguration.class)
class ReservationControllerIT {
    @Autowired
    private WebApplicationContext context;

    private MockMvc mvc;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    private User testUser;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @TestBean
    private Clock clock;

    static Clock clock() {
        return Clock.fixed(Instant.parse("2024-01-01T12:00:00.000Z"), ZoneId.of("UTC"));
    }

    @BeforeEach
    void setup() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        this.testUser = new User();
        this.testUser.setEmail("test@test.com");
        this.testUser.setPassword("password");
        this.testUser.setAdmin(false);

        final var otherUser = new User();
        otherUser.setEmail("other@test.com");
        otherUser.setPassword("password");
        otherUser.setAdmin(false);

        this.userRepository.saveAll(List.of(this.testUser, otherUser));
    }

    @AfterEach
    void resetDb() {
        this.chargerRepository.deleteAll();
        this.locationRepository.deleteAll();
        this.userRepository.deleteAll();
    }

    @Test
    @Requirement("NIK-12")
    void createReservationNoAuthentication() throws Exception {
        final CreateReservation req =
                new CreateReservation(
                        1L,
                        Instant.parse("2024-01-01T22:00:00.000+00:00"),
                        Instant.parse("2024-01-01T23:00:00.000+00:00"));

        mvc.perform(
                        post("/api/v1/reservations/")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    void createReservationChargerNotFound() throws Exception {
        final CreateReservation req =
                new CreateReservation(
                        1L,
                        Instant.parse("2024-01-01T22:00:00.000+00:00"),
                        Instant.parse("2024-01-01T23:00:00.000+00:00"));

        mvc.perform(
                        post("/api/v1/reservations/")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn();
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    void createReservationChargerNotAvailable() throws Exception {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.locationRepository.save(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(false);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.chargerRepository.save(charger);

        final CreateReservation req =
                new CreateReservation(
                        charger.getId(),
                        Instant.parse("2024-01-01T22:00:00.000+00:00"),
                        Instant.parse("2024-01-01T23:00:00.000+00:00"));

        mvc.perform(
                        post("/api/v1/reservations/")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn();
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    void createReservationOverlap() throws Exception {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.locationRepository.save(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.chargerRepository.save(charger);

        final Reservation reservation = new Reservation();
        reservation.setUser(this.testUser);
        reservation.setCharger(charger);
        reservation.setStartsAt(Instant.parse("2024-01-01T22:22:20.000+00:00"));
        reservation.setEndsAt(Instant.parse("2024-01-01T22:22:40.000+00:00"));

        this.reservationRepository.save(reservation);

        final CreateReservation req =
                new CreateReservation(
                        charger.getId(),
                        Instant.parse("2024-01-01T22:00:00.000+00:00"),
                        Instant.parse("2024-01-01T23:00:00.000+00:00"));

        mvc.perform(
                        post("/api/v1/reservations/")
                                .contentType(MediaType.APPLICATION_JSON_VALUE)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE))
                .andReturn();
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    void createReservationOk() throws Exception {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.locationRepository.save(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.chargerRepository.save(charger);

        final CreateReservation req =
                new CreateReservation(
                        charger.getId(),
                        Instant.parse("2024-01-01T22:00:00.000+00:00"),
                        Instant.parse("2024-01-01T23:00:00.000+00:00"));

        final MvcResult res =
                mvc.perform(
                                post("/api/v1/reservations/")
                                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                                        .content(objectMapper.writeValueAsString(req)))
                        .andExpect(status().isCreated())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andReturn();

        final ReservationDTO body =
                objectMapper.readValue(
                        res.getResponse().getContentAsString(), ReservationDTO.class);

        assertThat(body.getChargerId()).isEqualTo(req.chargedId);
        assertThat(body.getStart()).isEqualTo(req.start);
        assertThat(body.getEnd()).isEqualTo(req.end);

        assertThat(this.reservationRepository.findAll())
                .hasSize(1)
                .extracting(Reservation::getId, r -> r.getUser().getEmail())
                .containsExactlyInAnyOrder(tuple(body.id, "test@test.com"));
    }

    @Test
    @Requirement("NIK-12")
    void getReservationNoAuth() throws Exception {
        mvc.perform(get("/api/v1/reservations/"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE));
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-12")
    void getReservationOk() throws Exception {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.locationRepository.save(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.chargerRepository.save(charger);

        final Reservation reservation1 = new Reservation();
        reservation1.setUser(this.testUser);
        reservation1.setStartsAt(Instant.now().plus(2, ChronoUnit.HOURS));
        reservation1.setEndsAt(Instant.now().plus(3, ChronoUnit.HOURS));
        reservation1.setCharger(charger);

        final Reservation reservation2 = new Reservation();
        reservation2.setUser(this.testUser);
        reservation2.setStartsAt(Instant.now().plus(3, ChronoUnit.HOURS));
        reservation2.setEndsAt(Instant.now().plus(4, ChronoUnit.HOURS));
        reservation2.setCharger(charger);

        final Reservation reservation3 = new Reservation();
        reservation3.setUser(this.testUser);
        reservation3.setStartsAt(Instant.now().minus(1, ChronoUnit.DAYS));
        reservation3.setEndsAt(Instant.now().minus(4, ChronoUnit.HOURS));
        reservation3.setCharger(charger);

        final Reservation reservation4 = new Reservation();
        reservation4.setUser(this.testUser);
        reservation4.setStartsAt(Instant.now().minus(3, ChronoUnit.DAYS));
        reservation4.setEndsAt(Instant.now().minus(2, ChronoUnit.DAYS));
        reservation4.setCharger(charger);

        this.reservationRepository.save(reservation1);
        this.reservationRepository.save(reservation2);
        this.reservationRepository.save(reservation3);
        this.reservationRepository.save(reservation4);

        final MvcResult res =
                mvc.perform(get("/api/v1/reservations/"))
                        .andExpect(status().isOk())
                        .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                        .andReturn();

        final List<ReservationListDTO> body =
                objectMapper.readValue(
                        res.getResponse().getContentAsString(),
                        new TypeReference<ArrayList<ReservationListDTO>>() {
                        });

        assertThat(body).hasSize(4);
        assertThat(body.get(0).charger).isEqualTo(charger.getName());
        assertThat(body.get(0).maxPower).isEqualTo(charger.getMaxPower());
        assertThat(body.get(0).location).isEqualTo(location.getName());
        assertThat(body.get(0).start)
                .isCloseTo(reservation1.getStartsAt(), within(1, ChronoUnit.SECONDS));
        assertThat(body.get(0).end)
                .isCloseTo(reservation1.getEndsAt(), within(1, ChronoUnit.SECONDS));
        assertThat(body.get(1).start)
                .isCloseTo(reservation2.getStartsAt(), within(1, ChronoUnit.SECONDS));
        assertThat(body.get(1).end)
                .isCloseTo(reservation2.getEndsAt(), within(1, ChronoUnit.SECONDS));
        assertThat(body.get(2).start)
                .isCloseTo(reservation3.getStartsAt(), within(1, ChronoUnit.SECONDS));
        assertThat(body.get(2).end)
                .isCloseTo(reservation3.getEndsAt(), within(1, ChronoUnit.SECONDS));
        assertThat(body.get(3).start)
                .isCloseTo(reservation4.getStartsAt(), within(1, ChronoUnit.SECONDS));
        assertThat(body.get(3).end)
                .isCloseTo(reservation4.getEndsAt(), within(1, ChronoUnit.SECONDS));
    }

    @Test
    @Requirement("NIK-25")
    void cancelReservationNoAuthentication() throws Exception {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.locationRepository.save(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.chargerRepository.save(charger);

        Reservation reservation = new Reservation();
        reservation.setUser(this.testUser);
        reservation.setCharger(charger);
        reservation.setStartsAt(Instant.parse("2024-01-01T22:22:20.000+00:00"));
        reservation.setEndsAt(Instant.parse("2024-01-01T22:22:40.000+00:00"));

        reservation = this.reservationRepository.save(reservation);

        mvc.perform(delete("/api/v1/reservations/{id}", reservation.getId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "other@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-25")
    void cancelReservationNotOwner() throws Exception {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.locationRepository.save(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.chargerRepository.save(charger);

        Reservation reservation = new Reservation();
        reservation.setUser(this.testUser);
        reservation.setCharger(charger);
        reservation.setStartsAt(Instant.parse("2024-01-01T22:22:20.000+00:00"));
        reservation.setEndsAt(Instant.parse("2024-01-01T22:22:40.000+00:00"));

        reservation = this.reservationRepository.save(reservation);

        mvc.perform(delete("/api/v1/reservations/{id}", reservation.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    @Requirement("NIK-25")
    void cancelReservationOk() throws Exception {
        final Location location = new Location();
        location.setName("Test location");
        location.setLon(20.0f);
        location.setLat(30.0f);

        this.locationRepository.save(location);

        final Charger charger = new Charger();
        charger.setName("AAA1");
        charger.setAvailable(true);
        charger.setMaxPower(22.2F);
        charger.setLocation(location);

        this.chargerRepository.save(charger);

        Reservation reservation = new Reservation();
        reservation.setUser(this.testUser);
        reservation.setCharger(charger);
        reservation.setStartsAt(Instant.parse("2024-01-01T22:22:20.000+00:00"));
        reservation.setEndsAt(Instant.parse("2024-01-01T22:22:40.000+00:00"));

        reservation = this.reservationRepository.save(reservation);

        mvc.perform(delete("/api/v1/reservations/{id}", reservation.getId()))
                .andExpect(status().isNoContent());
    }
}
