package com.nikogrid.backend.steps;

import com.nikogrid.backend.auth.JwtGenerator;
import com.nikogrid.backend.auth.SecurityConstants;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import org.openqa.selenium.Cookie;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

public class ReservationSteps {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtGenerator jwtGenerator;

    @And("I am authenticated")
    public void iAmAuthenticated() {
        final User user = new User();
        user.setEmail("test@test.com");
        user.setPassword("password");
        user.setAdmin(false);

        userRepository.save(user);

        final var token = this.jwtGenerator.generateToken(user.getEmail());
        DriverInstance.getDriver().manage().addCookie(new Cookie(SecurityConstants.AUTH_COOKIE, token.token()));

        DriverInstance.getDriver().navigate().refresh();
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
