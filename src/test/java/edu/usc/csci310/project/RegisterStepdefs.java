package edu.usc.csci310.project;

import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
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

import java.sql.*;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RegisterStepdefs {

    private static final String REGISTRATION_PAGE_URL = "https://localhost:8080/register";
    private static final String LOGIN_PAGE_URL = "https://localhost:8080";
    private WebDriver driver = null;
    private static final String XPATH_ERROR_MESSAGE = "/html/body/div/div/div/div[1]/div/span";
    private static final String CSS_REQUIRED_FIELD = "input:invalid:required";
    private static final String VALIDATION_MESSAGE = "validationMessage";
    private static final String REQUIRED_FIELD_TEXT = "Please fill out this field.";


    @Before
    public void before() throws SQLException {
        driver = SharedWebDriver.getWebDriver();
    }

    @Given("I am on the Sign Up page")
    public void i_am_on_the_register_page() {
        driver.get(REGISTRATION_PAGE_URL);
    }

    @Given("I am on the Login page")
    public void i_am_on_the_login_page() {
        driver.get(LOGIN_PAGE_URL);
    }

    @When("I click the {string} tab on the navigation bar")
    public void i_click_the_tab_on_the_navigation_bar(String tabName) {
        Wait<WebDriver> waitForTab = new WebDriverWait(driver, Duration.ofSeconds(15));
        waitForTab.until(ExpectedConditions.elementToBeClickable(By.linkText(tabName)));
        driver.findElement(By.linkText(tabName)).click();
    }

    @When("I enter in the username {string}")
    public void i_enter_in_the_username(String username) {
        Wait<WebDriver> waitForTab = new WebDriverWait(driver, Duration.ofSeconds(15));
        waitForTab.until(ExpectedConditions.elementToBeClickable(By.ById.id("username")));
        driver.findElement(By.ById.id("username")).sendKeys(username);
    }

    @And("I enter in the password {string}")
    public void i_enter_in_the_password(String password) {
//        driver.findElement(By.ById.id("password")).clear();
        driver.findElement(By.ById.id("password")).sendKeys(password);
    }

    @And("I click the {string} button")
    public void i_click_the_button(String buttonName) throws InterruptedException {
        Wait<WebDriver> waitForButton = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForButton.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()=\"" + buttonName + "\"]")));
        driver.findElement(By.xpath("//button[text()=\"" + buttonName + "\"]")).click();

        if (buttonName.equals("Get Artists")) {
            Thread.sleep(1000);
        }
        else if (buttonName.equals("Get Songs")) {
            Thread.sleep(15000);
        }
    }

    @And("I click the {string} button on the pop up")
    public void i_click_the_button_pop_up(String buttonName) throws InterruptedException {
        WebDriverWait waitForAlert = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForAlert.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().dismiss();
//        Thread.sleep(1000);
    }

    @And("I click the {string} button again")
    public void i_click_the_button_again(String buttonName) {
//        Wait<WebDriver> waitForButton = new WebDriverWait(driver, Duration.ofSeconds(20));
//        waitForButton.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()=\"" + buttonName + "\"]")));
        driver.findElement(By.xpath("//button[text()=\"" + buttonName + "\"]")).click();
    }

    @And("I enter in the password {string} to confirm the password")
    public void i_enter_in_the_password_again_to_confirm_the_password(String password) {
        driver.findElement(By.ById.id("confirmPassword")).sendKeys(password);
    }

    @And("there is already another user with the username {string}")
    public void there_is_already_another_user_with_the_username(String username) {
        driver.findElement(By.ById.id("username")).sendKeys(username);
        driver.findElement(By.ById.id("password")).sendKeys("2trojanN");
        driver.findElement(By.ById.id("confirmPassword")).sendKeys("2trojanN");
        driver.findElement(By.xpath("//button[text()=\"Submit\"]")).click();

        WebDriverWait waitForAlert = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForAlert.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        //Navigate to the registration page, if not already on the page
        String REGISTRATION_PAGE_URL = "http://localhost:8080/register";
        driver.get(REGISTRATION_PAGE_URL);
    }

    @Then("see error {string}")
    public void i_should_see_the_error_message(String errorMsg) {
        String requiredUsername = driver.findElement(By.ById.id("username")).getAttribute("value");
        String requiredPassword = driver.findElement(By.ById.id("password")).getAttribute("value");
        String confirmRequiredPassword = driver.findElement(By.ById.id("confirmPassword")).getAttribute("value");
        Wait<WebDriver> waitForError = new WebDriverWait(driver, Duration.ofSeconds(100));
        if (requiredUsername.isEmpty() || requiredPassword.isEmpty() || confirmRequiredPassword.isEmpty()) {
            String requiredFieldText = Optional.ofNullable(
                    waitForError.until(ExpectedConditions
                                    .visibilityOfElementLocated(By.cssSelector(CSS_REQUIRED_FIELD)))
                            .getAttribute(VALIDATION_MESSAGE)
            ).orElse("");

            assertTrue(requiredFieldText.contains(REQUIRED_FIELD_TEXT));
            return;
        }
        String errorMessage = waitForError
                .until(ExpectedConditions.visibilityOfElementLocated(By.xpath(XPATH_ERROR_MESSAGE)))
                .getText();

        assertEquals(errorMsg, errorMessage);
    }

    @Then("I see message {string}")
    public void i_should_see_the_confirmation_message(String confirmationMsg) {
        WebDriverWait waitForAlert = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForAlert.until(ExpectedConditions.alertIsPresent());
        assertEquals(confirmationMsg, driver.switchTo().alert().getText());
        driver.switchTo().alert().accept();
    }

    @Then("I should be on the {string} page")
    public void iShouldBeOnThePage(String pageTitle) {
        System.out.println(pageTitle);
        assertTrue(driver.getPageSource().contains(pageTitle));
    }

    @Then("I still see the username {string}")
    public void iStillSeeTheUsername(String arg0) throws InterruptedException {
        Thread.sleep(10000);
        WebDriverWait waitForError = new WebDriverWait(driver, Duration.ofSeconds(5000));
        String usernameName = waitForError.until(ExpectedConditions.visibilityOfElementLocated(By.id("username"))).getAttribute("value");
        System.out.println("usernameName: " + usernameName);
        assertEquals(arg0, usernameName);
    }

    @And("I still see the password {string}")
    public void iStillSeeThePassword(String arg0) throws InterruptedException {
//        Thread.sleep(10000);
        WebDriverWait waitForError = new WebDriverWait(driver, Duration.ofSeconds(5000));
        String usernameName = waitForError.until(ExpectedConditions.visibilityOfElementLocated(By.id("password"))).getAttribute("value");
        System.out.println("usernameName: " + usernameName);
        assertEquals(arg0, usernameName);
    }

    @And("I still see the confirm password {string}")
    public void iStillSeeTheConfirmPassword(String arg0) throws InterruptedException {
//        Thread.sleep(10000);
        WebDriverWait waitForError = new WebDriverWait(driver, Duration.ofSeconds(5000));
        String usernameName = waitForError.until(ExpectedConditions.visibilityOfElementLocated(By.id("confirmPassword"))).getAttribute("value");
        System.out.println("usernameName: " + usernameName);
        assertEquals(arg0, usernameName);
    }

    @When("I click the Ok button on the pop up")
    public void iClickTheOkButtonOnThePopUp() {
        WebDriverWait waitForAlert = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForAlert.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();
    }

//    @After
//    public void after() throws SQLException {
//        Connection connection = DriverManager.getConnection("jdbc:sqlite:musicWebApp.db");
//        try (Statement stmt = connection.createStatement()) {
//            String deleteTableSQL = "DELETE FROM users";
//            stmt.executeUpdate(deleteTableSQL);
//            deleteTableSQL = "DELETE FROM favorites";
//            stmt.executeUpdate(deleteTableSQL);
//            deleteTableSQL = "DELETE FROM songs";
//            stmt.executeUpdate(deleteTableSQL);
//        } catch (SQLException e) {
//            throw new RuntimeException("Error deleting database", e);
//        }
//        connection.close();
//    }

    @AfterAll
    public static void after_all() {
        SharedWebDriver.quitWebDriver();
    }
}