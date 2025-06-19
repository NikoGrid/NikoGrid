package com.nikogrid.backend.steps;

import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.UserRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

public class StationSteps {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Given("I have the admin account")
    public void iHaveTheAdminAccount() {
        final var user = new User();
        user.setEmail("admin@test.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setAdmin(true);

        userRepository.save(user);
    }

    @When("I am authenticated as admin")
    public void iAmAdmin() {
        final var user = this.userRepository.findByEmail("admin@test.com").orElseThrow();
        final var loginLink = DriverInstance.waitFindByTestId("login-link");
        loginLink.click();
        final var emailInput = DriverInstance.waitFindByTestId("login-email");
        final var passwordInput = DriverInstance.waitFindByTestId("login-password");
        final var submitButton = DriverInstance.waitFindByTestId("login-submit-button");

        emailInput.sendKeys(user.getEmail());
        passwordInput.sendKeys("password");
        submitButton.click();
    }

    @When("I click the button to create a new location")
    public void iClickTheButtonToCreateANewLocation() {
        DriverInstance.waitFindByTestId("create-location-button").click();
    }

    @Then("I see a location creation form")
    public void iSeeALocationCreationForm() {
        final var form = DriverInstance.waitFindByTestId("create-location-form");
        assertThat(form).isNotNull();
    }

    @Then("when I input the name {string}, the latitude {int} and the longitude {int}")
    public void iInputTheNameLatitudeLocation(String name, int latitude, int longitude) {
        DriverInstance.waitFindByTestId("location-name-input").sendKeys(name);
        DriverInstance.waitFindByTestId("location-latitude-input").sendKeys(Integer.toString(latitude));
        DriverInstance.waitFindByTestId("location-longitude-input").sendKeys(Integer.toString(longitude));
    }

    @Then("when I submit the form to create a location")
    public void submitLocationForm() {
        DriverInstance.waitFindByTestId("create-location-submit").click();
    }

    @When("I click the button to create a new station")
    public void createStationButton() {
        DriverInstance.waitFindByTestId("create-charger-button").click();
    }
    @Then("I see a station creation form")
    public void getStationCreationForm(){
        final var form = DriverInstance.waitFindByTestId("create-charger-form");
        assertThat(form).isNotNull();
    }
    @Then("when I input the name {string} and the power {int}")
    public void fillStationCreationForm(String name, Integer power){
        DriverInstance.waitFindByTestId("charger-name-input").sendKeys(name);
        DriverInstance.waitFindByTestId("charger-power-input").sendKeys(Integer.toString(power));

    }

    @Then("when I submit the form to create a station")
    public void submitStationForm() {
        DriverInstance.waitFindByTestId("create-charger-submit").click();
    }

    @Then("I get confirmation that the station was created")
    public void getStationCreationConfirmation() {
        DriverInstance.waitFindByTestId("charger-creation-success");
    }

}
