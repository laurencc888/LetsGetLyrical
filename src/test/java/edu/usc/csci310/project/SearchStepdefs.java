package edu.usc.csci310.project;

import io.cucumber.java.*;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SearchStepdefs {
    private static final String DB_URL = "jdbc:sqlite:musicWebApp.db";
    private static final String LOGIN_URL = "https://localhost:8080";
    private static WebDriver driver = null;
    private static boolean alreadyRegistered = false;
    private static boolean alreadLoggedIn = false;
    private String currArtist = "";

    private final List<String> selectedSongs = new ArrayList<>();
    private final String[] TSSongs = {"22", "Afterglow", "All Of The Girls You Loved Before", "Angelina", "American Boy", "American Girl", "22 (Taylor's Version)", "All You Had To Do Was Stay", "Am I Ready for Love"};
    private final static String[] TSPopSongs = {"So Long, London", "Fortnight", "All Too Well (10 Minute Version) (Taylor’s Version) [Live Acoustic]", "Lover", "loml", "exile", "cardigan", "All Too Well (10 Minute Version) (Taylor’s Version) [From The Vault]", "The Tortured Poets Department", "But Daddy I Love Him"};
    private final String[] TMPopSongs = {"2 hands", "Revolving door", "Sports car", "you broke me first", "greedy", "It’s ok I’m ok", "Purple lace bra", "stupid", "Dear god", "I know love"};

    private static void registerUser(String username, String password) {
        // registering a user
        driver.findElement(By.linkText("Sign Up")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        driver.findElement(By.ById.id("username")).sendKeys(username);
        driver.findElement(By.ById.id("password")).sendKeys(password);
        driver.findElement(By.ById.id("confirmPassword")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()=\"" + "Submit" + "\"]")).click();

        // going back to login page
        WebDriverWait waitForAlert = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForAlert.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        alreadyRegistered = true;
    }

    public static void login(String user, String pass) throws SQLException {
        driver.get(LOGIN_URL);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));

        registerUser(user, pass);

        // logging in
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("username")));
        driver.findElement(By.ById.id("username")).sendKeys(user);
        driver.findElement(By.ById.id("password")).sendKeys(pass);
        driver.findElement(By.xpath("//button[text()=\"" + "Submit" + "\"]")).click();

        WebDriverWait waitForAlert = new WebDriverWait(driver, Duration.ofSeconds(20));
        waitForAlert.until(ExpectedConditions.alertIsPresent());
        assertEquals("Login successful! Redirecting to search...", driver.switchTo().alert().getText());
        driver.switchTo().alert().accept();

        assertTrue(driver.getPageSource().contains("SEARCH"));
        alreadLoggedIn = true;
    }

    private void doManualSearch(String artist, String song) throws InterruptedException {
        i_enter_in_the_Artist(artist);
        driver.findElement(By.xpath("//button[text()=\"" + "Get Artists" + "\"]")).click();
        i_click_on_artist_results(artist);
        i_select_from_the_selection_menu("Manual");
        input_number_of_songs(1);
        driver.findElement(By.xpath("//button[text()=\"" + "Get Songs" + "\"]")).click();

        // song selection
        WebDriverWait waitList = new WebDriverWait(driver, Duration.ofSeconds(30000));
        waitList.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//li[contains(., '" + song + "')]")));
        driver.findElement(By.xpath("//li[contains(., '" + song + "')]")).click();

        driver.findElement(By.xpath("//button[text()=\"" + "Search" + "\"]")).click();
    }

    @Before
    public void before() {
        driver = SharedWebDriver.getWebDriver();
        driver.manage().window().maximize();
    }

    @After
    public void after() {
        try {
            clean_up();
        }
        catch (Exception e) {
            throw new RuntimeException("Error clearing database", e);
        }
    }

    private void clean_up() throws SQLException {
        System.out.println("debug: deleting tables");
        alreadyRegistered = false;
        alreadLoggedIn = false;

        Connection conn = DriverManager.getConnection(DB_URL);
        String sql = "DELETE FROM songs;";
        try {
            PreparedStatement prepStatement = conn.prepareStatement(sql);
            prepStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "DELETE FROM favorites;";
        try {
            PreparedStatement prepStatement = conn.prepareStatement(sql);
            prepStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }

        sql = "DELETE FROM users;";
        try {
            PreparedStatement prepStatement = conn.prepareStatement(sql);
            prepStatement.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        conn.close();
    }

    @AfterStep
    public void loadPage() throws InterruptedException {
        Thread.sleep(500);
    }

//    @After
//    public void clean_up() throws SQLException {
//    }

    @AfterAll
    public static void after_all() {
        SharedWebDriver.quitWebDriver();
    }

    @Given("I am logged in on the Search page")
    public void i_am_logged_in_on_the_Search_page() throws SQLException {
        if (!alreadLoggedIn)
            login("newuser", "Pass1");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("artistName")));
    }

    @Given("I am on the Search page")
    public void i_am_on_the_Search_page() throws SQLException {
        if (!alreadLoggedIn)
            login("admin", "Pass1");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("artistName")));
    }

    @Given("There is no word cloud on the page")
    public void there_is_no_word_cloud_on_the_page() throws InterruptedException, SQLException {
        assertTrue(driver.findElements(By.className("wordcloud-container")).isEmpty());
    }

    @Given("There's already word cloud with {string}'s {int} most popular songs")
    public void there_already_word_cloud_popular(String artist, int numSongs) throws InterruptedException, SQLException {
        i_enter_in_the_Artist(artist);
        driver.findElement(By.xpath("//button[text()=\"" + "Get Artists" + "\"]")).click();
        i_click_on_artist_results(artist);
        i_select_from_the_selection_menu("Popularity");
        input_number_of_songs(numSongs);
        driver.findElement(By.xpath("//button[text()=\"" + "Search" + "\"]")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
    }

    @Given("{string} by {string} is in my Favorites")
    public void i_add_to_Favorites(String song, String artist) throws InterruptedException, SQLException {
//        i_am_on_the_Search_page();
        doManualSearch(artist, song);

//         word cloud should be instant
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
        driver.findElement(By.xpath("//button[text()=\"" + "Table" + "\"]")).click();

        // clicking on first row frequency to check song list
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]")).click();

        // wait until song details are displayed
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div")));

        // hover over this then press favorites
        WebElement desiredSong = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div/ul/div[1]/li"));

        Actions actions = new Actions(driver);
        // Perform the hover action
        actions.moveToElement(desiredSong).perform();

        driver.findElement(By.xpath("//button[text()=\"" + "♥ Add to Favorites" + "\"]")).click();
        Thread.sleep(2000);
        driver.navigate().refresh();
    }

    @Given("There is already a word cloud with {string}'s {string}")
    public void there_already_word_cloud_song(String artist, String song) throws InterruptedException, SQLException {
        doManualSearch(artist, song);
//        Thread.sleep(15000);
//        driver.findElement(By.xpath("//button[text()=\"" + "Word Cloud" + "\"]")).click();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
//
//        driver.findElement(By.xpath("//button[text()=\"" + "Generate From Favorites" + "\"]")).click();
//        Thread.sleep(2000);
//
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
//        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
//        assertNotNull(driver.findElement(By.className("wordcloud-container")));
    }

    @When("I enter in the Artist {string}")
    public void i_enter_in_the_Artist(String artist) {
        driver.findElement(By.ById.id("artistName")).clear();
        driver.findElement(By.ById.id("artistName")).sendKeys(artist);
        currArtist = artist;
    }

    @When("I click on {string} in Artist Results")
    public void i_click_on_artist_results(String artist) throws InterruptedException {
        Thread.sleep(500);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // this one takes some time to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("popup-container")));
        WebElement artistItem = driver.findElement(By.xpath("//li[contains(., '" + artist + "')]"));
        artistItem.click();
    }

    @When("I leave Artist blank")
    public void i_leave_artist_blank() {}

    @When("I leave # Songs empty")
    public void i_leave_empty_songs() {}

    @When("I don't select songs")
    public void i_dont_select_songs() {}

    @When("I select {string} from the selection menu")
    public void i_select_from_the_selection_menu(String type) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

        WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Sort By') or text()='Manual' or text()='Popularity']")));
        dropdownButton.click();

        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'" + type + "')]")));
        option.click();

        if (type.equals("Manual")) {
            Thread.sleep(5000);
        }
    }

    @When("I input {int} for number of songs")
    public void input_number_of_songs(int number) {
        driver.findElement(By.ById.id("numSongs")).clear();
        driver.findElement(By.ById.id("numSongs")).sendKeys(String.valueOf(number));
    }

    @When("I select {int} songs from the song list")
    public void i_select_songs_from_the_song_list(int number) {
        selectedSongs.clear();

        // song list takes a while to load
        WebDriverWait waitList = new WebDriverWait(driver, Duration.ofSeconds(30000));
        waitList.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div")));

//        String[] songList = new String[number];

//        if (currArtist.equalsIgnoreCase("Taylor Swift"))
//            songList = TSSongs;

        for (int i = 0; i < number; i++) {
            WebElement songItem = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div/ul/li[" + (i + 1) + "]/button"));

            String songName = songItem.getAttribute("aria-label");
            songName = songName.replaceFirst("(?i)^select song ", "").trim();
            selectedSongs.add(songName);

            songItem.click();
        }
        assertEquals(selectedSongs.size(), number);
    }

    @Then("There should be a word cloud with Taylor Swift's {int} songs chosen")
    public void is_a_manual_word_cloud(int number) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
        driver.findElement(By.xpath("//button[text()=\"" + "Table" + "\"]")).click();

        // clicking on first row frequency to check song list
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]")).click();

        // wait until song details are displayed
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div")));
        int numContains = 0;
        for (String selectedSong : selectedSongs) {
            System.out.println("SONG: " + selectedSong);
            if (driver.getPageSource().contains(selectedSong))
                numContains++;
        }
        assertEquals(numContains, number);
    }

    @Then("There should be a word cloud with Taylor Swift's {int} most popular songs")
    public static void is_a_word_cloud(int number) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
        driver.findElement(By.xpath("//button[text()=\"" + "Table" + "\"]")).click();

        // clicking on first row frequency to check song list
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]")).click();

        // wait until song details are displayed
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div")));
        int numContains = 0;
        for (String song : TSPopSongs) {
            System.out.println("SONG: " + song);
            if (driver.getPageSource().contains(song))
                numContains++;
        }
        assertEquals(numContains, number);
    }

    @Then("There should be a list of artists with names related to Taylor")
    public void is_a_list_of_artists_names() {
        assertTrue(driver.getPageSource().contains("Taylor Swift"));
    }

    @Then("There should be picture of each artist next to their name in the list")
    public void there_is_a_pic() {
        WebElement image = driver.findElement(By.cssSelector("img[alt='Taylor Swift']"));
        assertNotNull(image, "There is no image in the list");
    }

    @Then("There should be error message {string}")
    public void there_is_error_message(String message) {
        assertTrue(driver.getPageSource().contains(message), "There is no error message in the list");
    }

    @Then("Taylor Swift and Tate McRae's {int} most popular songs are in word cloud")
    public void taylor_swift_and_tate_mcRae_popular(int number) throws InterruptedException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
//        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        Thread.sleep(15000);
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
        driver.findElement(By.xpath("//button[text()=\"" + "Table" + "\"]")).click();

        // clicking on first row frequency to check song list
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]")).click();

        // wait until song details are displayed
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div")));
        int numContains = 0;
        for (String song : TSPopSongs) {
            System.out.println("SONG: " + song);
            if (driver.getPageSource().contains(song))
                numContains++;
        }
        assertEquals(numContains, number);
        numContains = 0;
        for (String song : TMPopSongs) {
            System.out.println("SONG: " + song);
            if (driver.getPageSource().contains(song))
                numContains++;
        }
        assertEquals(numContains, number);
    }

    @Then("There should be a word cloud with {string} and {string} lyrics")
    public void there_is_a_word_cloud_with_combo(String first, String second) throws InterruptedException {
//        driver.findElement(By.xpath("//button[text()=\"" + "Word Cloud" + "\"]")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
        driver.findElement(By.xpath("//button[text()=\"" + "Table" + "\"]")).click();

        // clicking on first row frequency to check song list
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]")).click();

        // wait until song details are displayed
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div")));

        assertTrue(driver.getPageSource().contains(first));
        assertTrue(driver.getPageSource().contains(second));
    }

    @Then("Word cloud with {string} {string} and {string} lyrics")
    public void word_cloud_with_three_combo(String first, String second, String third) throws InterruptedException {
//        driver.findElement(By.xpath("//button[text()=\"" + "Word Cloud" + "\"]")).click();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
        driver.findElement(By.xpath("//button[text()=\"" + "Table" + "\"]")).click();

        // clicking on first row frequency to check song list
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]")).click();

        // wait until song details are displayed
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div")));

        assertTrue(driver.getPageSource().contains(first));
        assertTrue(driver.getPageSource().contains(second));
    }
}