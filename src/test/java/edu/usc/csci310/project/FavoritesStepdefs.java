package edu.usc.csci310.project;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.*;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class FavoritesStepdefs {
    private static final String LOGIN_PAGE_URL = "https://localhost:8080";
    private static final String REGISTRATION_PAGE_URL = "https://localhost:8080/register";
    private static WebDriver driver = null;
    private String songName = null;
    private static final String DATABASE_URL = "jdbc:sqlite:musicWebApp.db";
    private static String current_username = null;

    @Before
    public void before() {
        driver = SharedWebDriver.getWebDriver();
    }

    public void set_up_favorites(int numSongs) {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (numSongs != 0) {
            add_fav_songs_from_search(numSongs, "Taylor Swift");
        }

        // navigating to favorites
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Favorites")));
        driver.findElement(By.linkText("Favorites")).click();
    }

    // overloading function to set up fav with a different artist (default is T swizzle)
    public void set_up_favorites(int numSongs, String artistName) {
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (numSongs != 0) {
            add_fav_songs_from_search(numSongs, artistName);
        }

        // navigating to favorites
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Favorites")));
        driver.findElement(By.linkText("Favorites")).click();
    }


    public static void add_fav_songs_from_search(int numSongs, String artistName) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // adding songs through search
        driver.findElement(By.ById.id("artistName")).sendKeys(artistName);
        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()=\"Get Artists\"]")));
        driver.findElement(By.xpath("//button[text()=\"Get Artists\"]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("popup-container")));
        WebElement artistItem = driver.findElement(By.xpath("//li[contains(., '" + artistName + "')]"));
        artistItem.click();

        WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Sort By') or text()='Manual' or text()='Popularity']")));
        dropdownButton.click();

        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'Popularity')]")));
        option.click();

        if (numSongs == 1) {
            driver.findElement(By.ById.id("numSongs")).sendKeys(String.valueOf(1));
        }
        else {
            driver.findElement(By.ById.id("numSongs")).sendKeys(String.valueOf(numSongs + 4));
        }


        wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[text()=\"Search\"]")));
        driver.findElement(By.xpath("//button[text()=\"Search\"]")).click();

        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // click table
        WebElement tableButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[2]/button[1]")));
        tableButton.click();

        // click first table row
        WebElement lyricButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]")));
        lyricButton.click();

        for (int i = 1; i <= numSongs; ++i) {
            // hover over song number i
            WebElement song = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div/ul/div[" + i + "]"));
            Actions actions = new Actions(driver);
            actions.moveToElement(song).perform();

            // click add to favorites
            WebElement favButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div/ul/div[" + i + "]/button")));
            favButton.click();
        }

        // exit out of pop up
        WebElement closeButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div/div/button")));
        closeButton.click();
    }

    @Given("I am registered as {string} and {string} and on the Login page")
    public static void iAmRegisteredAsAndAndOnTheLoginPage(String username, String password) throws SQLException {
        register_login(username, password);
    }

    // if a user is logged in, log out
    public static void register_login(String username, String password) throws SQLException {
        if (current_username != null && current_username.equals(username)) {}
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }



        if (driver.getPageSource().contains("Log Out")) {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Log Out")));
            driver.findElement(By.linkText("Log Out")).click();
        }

        SearchStepdefs.login(username, password);
    }

    @Given("I am on the Favorites page and there are {int} favorite songs")
    public void iAmOnTheFavoritesPageAndThereIsFavoriteSong(int numSongs) {
        set_up_favorites(numSongs);
    }

    @When("I hover over favorite song number {int}")
    public void iHoverOverFavoriteSongNumber(int number) {
        WebElement song = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[" + number + "]"));
        Actions actions = new Actions(driver);
        actions.moveToElement(song).perform();
        songName = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[" + number + "]/span")).getText();
    }

    @Then("I should see the up arrow button")
    public void iShouldSeeTheUpArrowButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement upArrowButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[1]/div/button[1]")));
        assertTrue(upArrowButton.isDisplayed());
    }

    @And("I should see the down arrow button")
    public void iShouldSeeTheDownArrowButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement downArrowButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[1]/div/button[2]")));
        assertTrue(downArrowButton.isDisplayed());
    }

    @And("I click the delete button for favorite song number {int}")
    public void iClickTheDeleteButtonForFavoriteSongNumber(int number) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement deleteButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[" + number + "]/div/button[3]")));
        deleteButton.click();
    }

    @And("I click the up arrow for favorite song number {int}")
    public void iClickTheUpArrowForFavoriteSongNumber(int number) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement upArrowButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[" + number + "]/div/button[1]")));
        upArrowButton.click();
    }

    @And("I click the down arrow for favorite song number {int}")
    public void iClickTheDownArrowForFavoriteSongNumber(int number) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement downArrowButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[" + number + "]/div/button[2]")));
        downArrowButton.click();
    }

    @Then("I should see the song move to position {int}")
    public void iShouldSeeTheSongMoveToPosition(int pos) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement song = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[" + pos + "]/span")));
        assertEquals(songName, song.getText());
    }

    @And("I click yes in the delete confirmation message")
    public void iClickYesInTheDeleteConfirmationMessage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div[2]/div/div/button[1]")));
        confirmButton.click();
    }

    @Then("The song should be deleted and not shown anymore")
    public void theSongShouldBeDeletedAndNotShownAnymore() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(driver.getPageSource().contains(songName));
    }

    @And("I click cancel in the delete confirmation message")
    public void iClickCancelInTheDeleteConfirmationMessage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div[2]/div/div/button[2]")));
        confirmButton.click();
    }

    @And("The song is not deleted and still shown")
    public void theSongIsNotDeletedAndStillShown() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(driver.getPageSource().contains(songName));
    }

    @When("I click the delete all button")
    public void iClickTheDeleteAllButton() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/button")));
        confirmButton.click();
    }

    @And("I click yes in the delete all confirmation message")
    public void iClickYesInTheDeleteAllConfirmationMessage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div[2]/div/div/button[1]")));
        confirmButton.click();
    }

    @Then("I see the message {string}")
    public void iSeeTheMessage(String message) {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(driver.getPageSource().contains(message));
    }

    @And("I click cancel in the delete all confirmation message")
    public void iClickCancelInTheDeleteAllConfirmationMessage() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div[2]/div/div/button[2]")));
        confirmButton.click();
    }

    @And("No songs are delete and all are still shown")
    public void noSongsAreDeleteAndAllAreStillShown() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(driver.getPageSource().contains("No Favorite Songs"));
    }

    @And("I click the song title for favorite song number {int}")
    public void iClickTheSongTitleForFavoriteSongNumber(int number) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement confirmButton = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[2]/div[" + number + "]/span")));
        confirmButton.click();
    }

    @Then("I see a pop up with the song title, artist, and year of recording")
    public void iShouldSeeAPopUpMessageWithTheSongTitleArtistAndYearOfRecording() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(driver.getPageSource().contains("Artist:"));
        assertTrue(driver.getPageSource().contains("Year of Recording:"));
    }

    @Then("I should see the delete confirmation message")
    public void iShouldSeeTheDeleteConfirmationMessage() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(driver.getPageSource().contains("Are you sure you want to delete your favorite song?"));
    }

    @Then("I should see the delete all confirmation message")
    public void iShouldSeeTheDeleteAllConfirmationMessage() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertTrue(driver.getPageSource().contains("Are you sure you want to delete all your favorite songs?"));
    }

    @When("I move the second song up")
    public void iMoveTheSecondSongUp() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        iHoverOverFavoriteSongNumber(2);
        iClickTheUpArrowForFavoriteSongNumber(2);
    }

    @And("I refresh the page")
    public void iRefreshThePage() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.navigate().refresh();
    }

    @And("I am logged in on Search page")
    public void iAmLoggedInOnSearchPage() {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        add_fav_songs_from_search(1, "Taylor Swift");
    }

    @Then("I should be on the Favorites page")
    public void iShouldBeOnTheFavoritesPage() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String currentUrl = driver.getCurrentUrl();
        assertEquals("https://localhost:8080/favorites", currentUrl);
    }

    @And("I should see a list of favorite songs")
    public void iShouldSeeAListOfFavoriteSongs() {
        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertFalse(driver.getPageSource().contains("No Favorite Songs"));
    }

    @And("I should see that the private selection is toggled")
    public void iShouldSeeThatThePrivateSelectionIsToggled() {
        WebElement privateRadio = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[2]/input"));
        assertTrue(privateRadio.isSelected());
    }

    @When("I toggle the public selection")
    public void iToggleThePublicSelection() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement publicRadio = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[1]/input")));
        publicRadio.click();
    }

    @When("I toggle the private selection")
    public void iToggleThePrivateSelection() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        WebElement privateRadio = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[2]/input")));
        privateRadio.click();
    }


    @Then("I should see that the user is public")
    public void iShouldSeeThatTheUserIsPublic() {
        WebElement publicRadio = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[1]/input"));
        assertTrue(publicRadio.isSelected());
    }

    @Given("I am on the Favorites page")
    public void iAmOnTheFavoritesPage() {
        set_up_favorites(0);
    }

    @Then("I should see that the private button is not toggled")
    public void iShouldSeeThatThePrivateButtonIsNotToggled() {
        WebElement privateRadio = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[2]/input"));
        assertFalse(privateRadio.isSelected());
    }

    @Then("I should see that the public button is not toggled")
    public void iShouldSeeThatThePublicButtonIsNotToggled() {
        WebElement publicRadio = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div[3]/div/div/label[1]/input"));
        assertFalse(publicRadio.isSelected());
    }
}