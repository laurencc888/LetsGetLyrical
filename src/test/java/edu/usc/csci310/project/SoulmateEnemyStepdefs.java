package edu.usc.csci310.project;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.SQLException;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class SoulmateEnemyStepdefs {
    private WebDriver driver = null;

    @Before
    public void before() {
        driver = SharedWebDriver.getWebDriver();
    }

    @After
    public void clean_up() {
        driver.navigate().refresh();
    }

    @When("I click the soulmate button")
    public void iClickTheSoulmateButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[1]/button[1]")));
        confirmButton.click();
    }

    @And("I should see their favorites list underneath")
    public void iShouldSeeTheirFavoritesListUnderneath() {
        WebElement favoritesList = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[1]/div/div/div[1]"));
        String text = favoritesList.getText();

        assertTrue(favoritesList.isDisplayed());
        assertNotNull(text);
        assertFalse(text.trim().isEmpty());
    }

    @When("I click the enemy button")
    public void iClickTheEnemyButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[1]/button[2]")));
        confirmButton.click();
    }

    @Given("There is a public user registered as {string} and {string}")
    public void thereIsAPublicUserRegisteredAsAnd(String username, String password) throws SQLException {
        FavoritesStepdefs.register_login(username, password);
    }

    @And("They have 1 favorite song from {string}")
    public void theyHaveFavoriteSongFrom(String artistName) {
        FavoritesStepdefs.add_fav_songs_from_search(1, artistName);

        // navigating to favorites
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Favorites")));
        driver.findElement(By.linkText("Favorites")).click();

        // make public
        WebElement publicRadio = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[1]/input")));
        publicRadio.click();
    }

    @And("I should see the username {string} as my lyrical enemy")
    public void iShouldSeeTheUsernameAsMyLyricalEnemy(String username) {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(driver.getPageSource().contains(username));
    }

    @And("I should see the username {string} as my lyrical soulmate")
    public void iShouldSeeTheUsernameAsMyLyricalSoulmate(String username) {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(driver.getPageSource().contains(username));
    }

    @Given("There are 3 users registered as {string}, {string}, and {string}")
    public void thereAreUsersRegisteredAsAnd(String user1, String user2, String user3) throws SQLException {
        // user 1
        FavoritesStepdefs.register_login(user1, "Pw123");
        FavoritesStepdefs.add_fav_songs_from_search(2, "Lorde");

        // navigating to favorites
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Favorites")));
        driver.findElement(By.linkText("Favorites")).click();

        // make public
        WebElement publicRadio = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[1]/input")));
        publicRadio.click();
        
        // user 2
        FavoritesStepdefs.register_login(user2, "Pw123");
        FavoritesStepdefs.add_fav_songs_from_search(2, "Taylor Swift");

        // navigating to favorites
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Favorites")));
        driver.findElement(By.linkText("Favorites")).click();

        // make public
        publicRadio = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[1]/input")));
        publicRadio.click();
    }

    @And("{string} and {string} are both public")
    public void andAreBothPublic(String user1, String user2) {
        // do nothing
    }

    @And("{string} and {string} have the same songs")
    public void andHaveTheSameSongs(String user1, String user2) {
        // do nothing
    }

    @And("{string} and {string} have no similar songs")
    public void andHaveNoSimilarSongs(String user1, String user2) {
        // do nothing
    }

    @Then("I should see a thumbs up emoji pop up for a second")
    public void iShouldSeeAThumbsUpEmojiPopUpForASecond() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement image = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/img"));
        String alt = image.getAttribute("alt");

        assertNotNull(image, "There is no thumbs up");
        assertEquals("Thumbs Up Sign", alt);
    }

    @Then("I should see a thumbs down emoji pop up for a second")
    public void iShouldSeeAThumbsDownEmojiPopUpForASecond() {
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        WebElement image = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/img"));
        String alt = image.getAttribute("alt");

        assertNotNull(image, "There is no thumbs down");
        assertEquals("Thumbs Down Sign", alt);
    }
}
