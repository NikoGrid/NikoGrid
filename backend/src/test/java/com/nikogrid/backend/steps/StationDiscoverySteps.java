package com.nikogrid.backend.steps;

import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.entities.User;
import com.nikogrid.backend.repositories.ChargerRepository;
import com.nikogrid.backend.repositories.LocationRepository;
import com.nikogrid.backend.repositories.UserRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


public class StationDiscoverySteps {
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${frontend.base-url}")
    private String baseUrl;

    @After
    @Before
    public void cleanDatabase() {
        chargerRepository.deleteAll();
        locationRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DataTableType
    public Location locationEntry(Map<String, String> entry) {
        final var location = new Location();
        location.setName(entry.get("name"));
        final var lat = entry.get("latitude");
        final var lon = entry.get("longitude");
        if (lat == null && lon == null) return location;

        assert lat != null;
        assert lon != null;

        location.setLat(Float.parseFloat(lat));
        location.setLon(Float.parseFloat(lon));

        return location;
    }

    private Charger createChargerOnLocation(Location l) {
        var charger = new Charger();
        charger.setName(l.getName());
        charger.setLocation(l);
        charger.setMaxPower(250);
        return charger;
    }

    @Given("the following charging stations exist:")
    public void theFollowingChargingStationsExist(List<Location> locations) {
        locationRepository.saveAll(locations);
        chargerRepository.saveAll(locations.stream().map(this::createChargerOnLocation).toList());
    }

    @When("I open the application")
    public void iOpenTheApplication() {
        DriverInstance.getDriver().get(baseUrl);
    }

    @And("I browse for stations at {float}, {float}")
    public void iBrowseForStationsAt(float lat, float lon) {
        var addrInput = DriverInstance.waitFindByTestId("address-input");
        var coordsButton = DriverInstance.waitFindByTestId("coords-button");

        addrInput.sendKeys(String.format("%f, %f", lat, lon));
        coordsButton.click();

        var wait = new WebDriverWait(DriverInstance.getDriver(), Duration.ofMillis(500));
        var pane = DriverInstance.getDriver().findElement(By.className("leaflet-map-pane"));
        wait.until(d -> !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-pan-anim"));

        var selector = By.cssSelector("[data-test-group='location']");

        var zoomOut = DriverInstance.getDriver().findElement(By.className("leaflet-control-zoom-out"));
        for (var i = 0; i < 8; i++) {
            if (DriverInstance.getDriver().findElements(selector).size() > 1) break;
            zoomOut.click();
            wait.until(d -> !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-zoom-anim"));
        }
    }

    @When("I browse for the closest station to {string}")
    public void iBrowseForStationClosest(String addr) {
        var addrInput = DriverInstance.waitFindByTestId("address-input");
        var coordsButton = DriverInstance.waitFindByTestId("find-closest");

        addrInput.sendKeys(addr);
        coordsButton.click();
        var wait = new WebDriverWait(DriverInstance.getDriver(), Duration.ofHours(2));
        var pane = DriverInstance.getDriver().findElement(By.className("leaflet-map-pane"));
        wait.until(d -> !d.findElement(By.cssSelector("[data-test-id='location-loading']")).isDisplayed()
                && !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-zoom-anim")
                && !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-pan-anim")
        );
    }

    @Then("I get the stations:")
    public void iGetTheStations(List<Location> locations) {
        var items = DriverInstance.waitFindByTestGroup("location");
        var ids = locations
                .stream()
                .map(Location::getName)
                .toArray(String[]::new);
        assertThat(items).hasSize(locations.size());
        assertThat(items)
                .map(i -> Objects.requireNonNull(i.getAttribute("data-test-name")))
                .containsExactlyInAnyOrder(ids);
    }

    @Then("I get the {string}")
    public void iGetTheStation(String location) {
        var selector = By.cssSelector("[data-test-highlighted=true]");
        var wait = new WebDriverWait(DriverInstance.getDriver(), Duration.ofSeconds(5));
        wait.until(d -> d.findElement(selector).isDisplayed());
        var item = DriverInstance.getDriver().findElement(selector);
        assertThat(item).isNotNull();
    }

    @And("navigate to the register page")
    public void navigateToTheRegisterPage() {
        final var registerLink = DriverInstance.waitFindByTestId("register-link");
        registerLink.click();
    }

    @Then("I see a register form")
    public void iSeeARegisterForm() {
        DriverInstance.waitFindByTestId("register-form");
    }

    @And("when I input the email {string} and password {string}")
    public void whenIInputTheEmailAndPassword(String email, String password) {
        final var emailInput = DriverInstance.waitFindByTestId("register-email");
        final var passwordInput = DriverInstance.waitFindByTestId("register-password");
        final var confirmPasswordInput = DriverInstance.waitFindByTestId("register-confirm-password");

        final var submitButton = DriverInstance.waitFindByTestId("register-submit-button");

        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        confirmPasswordInput.sendKeys(password);

        submitButton.click();
    }

    @Then("I get redirected to the login page")
    public void iGetRedirectedToTheLoginPage() {
        DriverInstance.waitFindByTestId("login-page");
    }

    @Given("I have the account with email {string} and password {string}")
    public void iHaveTheAccountWithEmailAndPassword(String email, String password) {
        final var user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
    }

    @And("navigate to the login page")
    public void navigateToTheLoginPage() {
        final var loginLink = DriverInstance.waitFindByTestId("login-link");
        loginLink.click();
    }

    @Then("I see a login form")
    public void iSeeALoginForm() {
        DriverInstance.waitFindByTestId("login-form");
    }

    @And("when I login with the email {string} and password {string}")
    public void whenILoginWithTheEmailAndPassword(String email, String password) {
        final var emailInput = DriverInstance.waitFindByTestId("login-email");
        final var passwordInput = DriverInstance.waitFindByTestId("login-password");
        final var submitButton = DriverInstance.waitFindByTestId("login-submit-button");

        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        submitButton.click();
    }

    @Then("I get redirected to the home page")
    public void iGetRedirectedToTheHomePage() {
        DriverInstance.waitFindByTestId("home-page");
    }

    @And("I select a station")
    public void iSelectAStation() {
        var selector = By.cssSelector("[data-test-highlighted=true]");
        var wait = new WebDriverWait(DriverInstance.getDriver(), Duration.ofSeconds(5));
        wait.until(d -> d.findElement(selector).isDisplayed());
        var item = DriverInstance.getDriver().findElement(selector);

        item.click();
    }
}
