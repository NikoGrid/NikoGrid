package com.nikogrid.backend.steps;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;

import java.time.Duration;
import java.util.List;

public class DriverInstance {
    private static WebDriver driver;

    @Value("${selenium.docker:false}")
    private boolean useDocker;

    @Before
    public void setUpWebDriver() {
        if (driver == null) {
            var opts = new FirefoxOptions();
            opts.addPreference("geo.enabled", false);
            WebDriverManager wdm = WebDriverManager.firefoxdriver();

            if (this.useDocker)
                wdm = wdm.browserInDocker();

            driver = wdm.capabilities(opts).timeout(120).create();
        }
    }

    @After
    public void tearDownWebDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public static synchronized WebDriver getDriver() {
        return driver;
    }

    public static WebElement waitFindByTestId(String testId, int duration) {
        var selector = By.cssSelector(String.format("[data-test-id='%s']", testId));
        var wait = new WebDriverWait(DriverInstance.getDriver(), Duration.ofSeconds(duration));
        wait.until(d -> d.findElement(selector).isDisplayed());
        return DriverInstance.getDriver().findElement(selector);
    }

    public static WebElement waitFindByTestId(String testId) {
        return waitFindByTestId(testId, 5);
    }

    public static List<WebElement> waitFindByTestGroup(String testId, int duration) {
        var selector = By.cssSelector(String.format("[data-test-group='%s']", testId));
        var wait = new WebDriverWait(DriverInstance.getDriver(), Duration.ofSeconds(duration));
        wait.until(d -> d.findElement(selector).isDisplayed());
        return DriverInstance.getDriver().findElements(selector);
    }

    public static List<WebElement> waitFindByTestGroup(String testId) {
        return waitFindByTestGroup(testId, 5);
    }

}
