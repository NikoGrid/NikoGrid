package com.nikogrid.backend.steps;

import com.nikogrid.backend.entities.Charger;
import com.nikogrid.backend.entities.Location;
import com.nikogrid.backend.repositories.ChargerRepository;
import com.nikogrid.backend.repositories.LocationRepository;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.DataTableType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


public class StationDiscoverySteps {

    private WebDriver driver;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ChargerRepository chargerRepository;

    @Value("${frontend.base-url}")
    private String baseUrl;


    @Value("${selenium.docker:false}")
    private boolean useDocker;
    private WebElement waitFindByTestId(String testId, int duration) {
        var selector = By.cssSelector(String.format("[data-test-id='%s']", testId));
        var wait = new WebDriverWait(driver, Duration.ofSeconds(duration));
        wait.until(d -> d.findElement(selector).isDisplayed());
        return driver.findElement(selector);
    }

    private WebElement waitFindByTestId(String testId) {
        return waitFindByTestId(testId, 5);
    }

    private List<WebElement> waitFindByTestGroup(String testId, int duration) {
        var selector = By.cssSelector(String.format("[data-test-group='%s']", testId));
        var wait = new WebDriverWait(driver, Duration.ofSeconds(duration));
        wait.until(d -> d.findElement(selector).isDisplayed());
        return driver.findElements(selector);
    }

    private List<WebElement> waitFindByTestGroup(String testId) {
        return waitFindByTestGroup(testId, 5);
    }

    @Before
    public void setUpWebDriver() {
        if (this.driver == null) {
            var opts = new FirefoxOptions();
            opts.addPreference("geo.enabled", false);
            WebDriverManager wdm = WebDriverManager.firefoxdriver();

            if (this.useDocker)
                wdm = wdm.browserInDocker();

            this.driver = wdm.capabilities(opts).timeout(120).create();
        }
    }

    @After
    public void tearDownWebDriver() {
        if (this.driver != null) {
            this.driver.quit();
            this.driver = null;
        }
        chargerRepository.deleteAll();
        locationRepository.deleteAll();
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
        driver.get(baseUrl);
    }

    @And("I browse for stations at {float}, {float}")
    public void iBrowseForStationsAt(float lat, float lon) {
        var addrInput = waitFindByTestId("address-input");
        var coordsButton = waitFindByTestId("coords-button");

        addrInput.sendKeys(String.format("%f, %f", lat, lon));
        coordsButton.click();

        var wait = new WebDriverWait(driver, Duration.ofMillis(500));
        var pane = driver.findElement(By.className("leaflet-map-pane"));
        wait.until(d -> !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-pan-anim"));

        var selector = By.cssSelector("[data-test-group='location']");

        var zoomOut = driver.findElement(By.className("leaflet-control-zoom-out"));
        for (var i = 0; i < 8; i++) {
            if (driver.findElements(selector).size() > 1) break;
            zoomOut.click();
            wait.until(d -> !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-zoom-anim"));
        }
    }

    @When("I browse for the closest station to {string}")
    public void iBrowseForStationClosest(String addr) {
        var addrInput = waitFindByTestId("address-input");
        var coordsButton = waitFindByTestId("find-closest");

        addrInput.sendKeys(addr);
        coordsButton.click();
        var wait = new WebDriverWait(driver, Duration.ofHours(2));
        var pane = driver.findElement(By.className("leaflet-map-pane"));
        wait.until(d -> !d.findElement(By.cssSelector("[data-test-id='location-loading']")).isDisplayed()
            && !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-zoom-anim")
            && !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-pan-anim")
        );
    }

    @Then("I get the stations:")
    public void iGetTheStations(List<Location> locations) {
        var items = waitFindByTestGroup("location");
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
        var wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        wait.until(d -> d.findElement(selector).isDisplayed());
        var item = driver.findElement(selector);
        assertThat(item).isNotNull();
    }

    @And("navigate to the register page")
    public void navigateToTheRegisterPage() {
        final var registerLink = waitFindByTestId("register-link");
        registerLink.click();
    }

    @Then("I see a register form")
    public void iSeeARegisterForm() {
        waitFindByTestId("register-form");
    }

    @And("when I input the email {string} and password {string}")
    public void whenIInputTheEmailAndPassword(String email, String password) {
        final var emailInput = waitFindByTestId("register-email");
        final var passwordInput = waitFindByTestId("register-password");
        final var confirmPasswordInput = waitFindByTestId("register-confirm-password");

        final var submitButton = waitFindByTestId("register-submit-button");

        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        confirmPasswordInput.sendKeys(password);

        submitButton.click();
    }

    @Then("I get redirected to the login page")
    public void iGetRedirectedToTheLoginPage() {
        waitFindByTestId("login-page");
    }
}
