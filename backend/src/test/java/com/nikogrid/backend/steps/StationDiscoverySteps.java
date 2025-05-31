package com.nikogrid.backend.steps;

import com.nikogrid.backend.entities.Location;
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
import org.openqa.selenium.firefox.FirefoxDriver;
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

    private static WebDriver driver;

    private final LocationRepository locationRepository;

    @Value("${frontend.base-url}")
    private String baseUrl;

    @Autowired
    public StationDiscoverySteps(LocationRepository locationRepository) {
        this.locationRepository = locationRepository;
    }

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
        if (driver == null) {
            WebDriverManager.firefoxdriver().setup();
            var opts = new FirefoxOptions();
            opts.addPreference("geo.enabled", false);
            driver = new FirefoxDriver(opts);
        }
    }

    @After
    public void tearDownWebDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
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

    @Given("the following charging stations exist:")
    public void theFollowingChargingStationsExist(List<Location> locations) {
        locationRepository.saveAll(locations);
    }

    @When("I open the application")
    public void iOpenTheApplication() {
        driver.get(baseUrl);
    }

    @And("I browse for stations at {float}, {float}")
    public void iBrowseForStationsAt(float lat, float lon) {
        var latInput = waitFindByTestId("lat-input");
        var lonInput = waitFindByTestId("lon-input");
        var coordsButton = waitFindByTestId("coords-button");

        latInput.sendKeys(Float.toString(lat));
        lonInput.sendKeys(Float.toString(lon));
        coordsButton.click();

        var wait = new WebDriverWait(driver, Duration.ofMillis(500));
        var pane = driver.findElement(By.className("leaflet-map-pane"));
        wait.until(d -> !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-pan-anim"));

        var selector = By.cssSelector("[data-test-group='location']");

        var zoomOut = driver.findElement(By.className("leaflet-control-zoom-out"));
        for (var i = 0; i < 8; i++) {
            if (driver.findElements(selector).size() != 1) break;
            zoomOut.click();
            wait.until(d -> !Objects.requireNonNull(pane.getAttribute("class")).contains("leaflet-zoom-anim"));
        }
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
}
