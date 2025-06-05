package com.nikogrid.backend.steps;

import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

public class ReservationSteps {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @And("I am authenticated")
    public void iAmAuthenticated() {
        final User user = new User();
        user.setEmail("test@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setAdmin(false);

        userRepository.save(user);

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
}
