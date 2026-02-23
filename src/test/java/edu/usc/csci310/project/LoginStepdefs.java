package edu.usc.csci310.project;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LoginStepdefs {
    private WebDriver driver = null;

    @Before
    public void before() {
        driver = SharedWebDriver.getWebDriver();
    }

    @And("I already registered with username {string} and password {string}")
    public void i_have_already_created_a_user_account_with_the_username_and_the_password(String username, String password) throws InterruptedException {
        //Navigate to the registration page, if not already on the page
        String REGISTRATION_PAGE_URL = "https://localhost:8080/register";
        driver.get(REGISTRATION_PAGE_URL);

        driver.findElement(By.ById.id("username")).sendKeys(username);
        driver.findElement(By.ById.id("password")).sendKeys(password);
        driver.findElement(By.ById.id("confirmPassword")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()=\"Submit\"]")).click();


        //Navigate back to the login page
        String LOGIN_PAGE_URL = "https://localhost:8080";
        driver.get(LOGIN_PAGE_URL);
    }

    @Given("I am on the browser")
    public void iAmOnTheBrowser() {
        //Empty function to simulate the user being on a default browser page.
    }

    @When("I type in {string} in the address bar")
    public void i_type_in_the_address_bar(String url) throws InterruptedException {
        driver.get(url);
    }

    @And("I wait {int} minute")
    public void i_wait_seconds(int time) throws InterruptedException {
        Thread.sleep(time*60*1000);
    }

    @Then("I see the error {string}")
    public void i_see_the_error(String errorMsg) {
        String requiredUsername = driver.findElement(By.ById.id("username")).getAttribute("value");
        String requiredPassword = driver.findElement(By.ById.id("password")).getAttribute("value");
        Wait<WebDriver> waitForError = new WebDriverWait(driver, Duration.ofSeconds(100));
        if (!requiredUsername.isEmpty() && !requiredPassword.isEmpty()) {
            String errorMessage = SharedWebDriver.getErrorMessage(waitForError);
            assertEquals(errorMsg, errorMessage);
        } else {
            String requiredField = waitForError
                    .until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("input:invalid:required")))
                    .getAttribute("validationMessage");
            assertTrue(requiredField.contains("Please fill out this field."));
        }
    }

    @And("I see the error 3 wrong tries in 1 minute. No login for 30 seconds.")
    public void i_see_the_error_3_wrong_tries_in_1minute() {
        driver.findElement(By.ById.id("username")).sendKeys("John Doe");
        driver.findElement(By.ById.id("password")).sendKeys("1Trojan");

        driver.findElement(By.xpath("//button[text()=\"Submit\"]")).click();

        driver.findElement(By.ById.id("username")).clear();
        driver.findElement(By.ById.id("password")).clear();

        driver.findElement(By.ById.id("username")).sendKeys("John Doe");
        driver.findElement(By.ById.id("password")).sendKeys("2Trojan");

        driver.findElement(By.xpath("//button[text()=\"Submit\"]")).click();

        driver.findElement(By.ById.id("username")).clear();
        driver.findElement(By.ById.id("password")).clear();

        driver.findElement(By.ById.id("username")).sendKeys("Jane Doe");
        driver.findElement(By.ById.id("password")).sendKeys("2Trojan");

        driver.findElement(By.xpath("//button[text()=\"Submit\"]")).click();
        Wait<WebDriver> waitForError = new WebDriverWait(driver, Duration.ofSeconds(10));
        String errorMessage = SharedWebDriver.getErrorMessage(waitForError);
    }

    @Then("I should see the title {string}")
    public void iShouldSeeTheTitle(String title) {
        assertTrue(driver.getPageSource().contains(title));
    }

    @Then("I should see {string} on the page")
    public void iShouldSeeOnThePage(String teamNine) {
        assertTrue(driver.getPageSource().contains(teamNine));
    }

    @Then("I should see the {string} tab on the navigation bar")
    public void iShouldSeeTheTabOnTheNavigationBar(String tabName) {
        Wait<WebDriver> waitForTab = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForTab.until(ExpectedConditions.visibilityOfElementLocated(By.linkText(tabName)));
        assertTrue(driver.getPageSource().contains(tabName));
    }
}
