package com.nikogrid.backend.steps;

import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.Reservation;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.ChargerRepository;
import com.nikogrid.backend.repositories.LocationRepository;
import com.nikogrid.backend.repositories.ReservationRepository;
import com.nikogrid.backend.repositories.UserRepository;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ReservationSteps {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Value("${frontend.base-url}")
    private String baseUrl;

    private User user;

    @DataTableType
    public Reservation dateEntry(Map<String, String> entry) {
        final var reservation = new Reservation();
        reservation.setStartsAt(Instant.now().plus(Long.parseLong(entry.get("startsAt")), ChronoUnit.HOURS));
        reservation.setEndsAt(Instant.now().plus(Long.parseLong(entry.get("endsAt")), ChronoUnit.HOURS));

        return reservation;
    }

    @And("I am authenticated")
    public void iAmAuthenticated() {
        this.user = new User();
        this.user.setEmail("test@test.com");
        this.user.setPassword(passwordEncoder.encode("password"));
        this.user.setAdmin(false);

        this.userRepository.save(user);

        final var loginLink = DriverInstance.waitFindByTestId("login-link");
        loginLink.click();

        final var emailInput = DriverInstance.waitFindByTestId("login-email");
        final var passwordInput = DriverInstance.waitFindByTestId("login-password");
        final var submitButton = DriverInstance.waitFindByTestId("login-submit-button");

        emailInput.sendKeys(user.getEmail());
        passwordInput.sendKeys("password");
        submitButton.click();
    }

    @And("I select a charger to book")
    public void iSelectAChargerToBook() {
        var bookButton = DriverInstance.waitFindByTestId("book-charger-btn");
        bookButton.click();
    }

    @Then("A reservation dialog should appear")
    public void aReservationDialogShouldAppear() {
        DriverInstance.waitFindByTestId("reservation-dialog");
    }

    @And("I fill the reservation time for tomorrow at noon for {int} hour")
    public void iFillTheReservationTimeForTomorrowAtNoonForHour(int hours) {
        final var startInput = DriverInstance.waitFindByTestId("book-start");
        final var endInput = DriverInstance.waitFindByTestId("book-end");

        final var submitButton = DriverInstance.waitFindByTestId("book-submit");

        final LocalDateTime start = LocalDateTime.now()
                .plusDays(1)
                .toLocalDate().atTime(12, 0);

        final LocalDateTime end = start.plusHours(hours);

        startInput.sendKeys(start.toString());
        endInput.sendKeys(end.toString());


        submitButton.click();
    }

    @Then("I should get confirmation that the reservation was created")
    public void iShouldGetConfirmationThatTheReservationWasCreated() {
        DriverInstance.waitFindByTestId("reservation-success");
    }

    @Given("the following reservations are booked:")
    public void createReservations(List<Reservation> reservations) {
        final var location = new Location();
        location.setName("AAA1");
        location.setLat(0);
        location.setLon(0);
        this.locationRepository.save(location);
        final var charger = new Charger();
        charger.setName("AAA1");
        charger.setMaxPower(250);
        charger.setLocation(location);
        this.chargerRepository.save(charger);
        for (Reservation res : reservations) {
            res.setUser(this.user);
            res.setCharger(charger);
            this.reservationRepository.save(res);
        }
    }

    @When("I go to my profile")
    public void iGoToMyProfile() {
        DriverInstance.getDriver().get(baseUrl + "/profile");
    }

    @Then("I should see {int} reservations")
    public void countReservations(int numReservations) {
        final var cards = DriverInstance.waitFindByTestGroup("reservation-card");
        assert (cards.size() == numReservations);
    }

    @Then("reservation {int} starts at {int}")
    public void confirmStartInstant(int cardIdx, int instantOffset) {
        final var cards = DriverInstance.waitFindByTestGroup("reservation-card");
        final var card = cards.get(cardIdx - 1);
        var selector = By.cssSelector("[data-test-id='reservation-start-instant']");
        final var instant = LocalDateTime.parse(card.findElement(selector).getText(), DateTimeFormatter.ofPattern("dd/MM/yyyy, HH:mm"));
        assertThat(instant).isCloseTo(LocalDateTime.now().plus(instantOffset, ChronoUnit.HOURS), within(1, ChronoUnit.HOURS));

    }
}
