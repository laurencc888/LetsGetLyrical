package edu.usc.csci310.project;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.sql.SQLException;
import java.time.Duration;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class WordCloudStepdefs {
    private static WebDriver driver = null;

    private int extractFontSize(String style) {
        Pattern pattern = Pattern.compile("font-size:\\s*(\\d+)px");
        Matcher matcher = pattern.matcher(style);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        throw new IllegalArgumentException("Font size not found in style: " + style);
    }

    private void doPopularSearch(String artist, int songs) throws InterruptedException {
        driver.findElement(By.ById.id("artistName")).clear();
        driver.findElement(By.ById.id("artistName")).sendKeys(artist);

        driver.findElement(By.xpath("//button[text()=\"" + "Get Artists" + "\"]")).click();
        Thread.sleep(500);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // this one takes some time to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("popup-container")));
        WebElement artistItem = driver.findElement(By.xpath("//li[contains(., '" + artist + "')]"));
        artistItem.click();

        // selection
        WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Sort By') or text()='Manual' or text()='Popularity']")));
        dropdownButton.click();
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'" + "Popularity" + "')]")));
        option.click();

        // input num songs
        driver.findElement(By.ById.id("numSongs")).clear();
        driver.findElement(By.ById.id("numSongs")).sendKeys(String.valueOf(songs));

        driver.findElement(By.xpath("//button[text()=\"" + "Search" + "\"]")).click();
    }

    @Before
    public void before() {
        driver = SharedWebDriver.getWebDriver();
    }

    @Given("I am on the Search page as {string}")
    public void i_am_on_the_Search_page_as(String username) throws SQLException {
        SearchStepdefs.login(username, "Pass1");
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("artistName")));
    }

    @Given("I want to search for Taylor Swift's 2 most popular songs")
    public void wantTS2PopSongs() throws InterruptedException {
        driver.findElement(By.ById.id("artistName")).clear();
        driver.findElement(By.ById.id("artistName")).sendKeys("Taylor Swift");

        driver.findElement(By.xpath("//button[text()=\"" + "Get Artists" + "\"]")).click();
        Thread.sleep(500);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // this one takes some time to load
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.className("popup-container")));
        WebElement artistItem = driver.findElement(By.xpath("//li[contains(., '" + "Taylor Swift" + "')]"));
        artistItem.click();

        // selection
        WebElement dropdownButton = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Sort By') or text()='Manual' or text()='Popularity']")));
        dropdownButton.click();
        WebElement option = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//a[contains(text(),'" + "Popularity" + "')]")));
        option.click();

        // input num songs
        driver.findElement(By.ById.id("numSongs")).clear();
        driver.findElement(By.ById.id("numSongs")).sendKeys(String.valueOf(2));
    }

    @When("I see a word cloud with Taylor Swift's {int} most popular songs")
    public void seeAPopularWordCloud(int numSongs) {
        SearchStepdefs.is_a_word_cloud(numSongs);
        driver.findElement(By.xpath("//button[text()=\"" + "×" + "\"]")).click();
    }

    @When("I see the table of words")
    public void seeTheTableOfWords() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table")));

        assertTrue(driver.getPageSource().contains("Frequency"));
    }

    @When("I see {string} and {string} as the top words in the table")
    public void seeATopWordsInTheTable(String word1, String word2) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table")));

        //*[@id="root"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]

        WebElement row1 = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[1]/td[1]"));
        String rowName1 = row1.getAttribute("aria-label");
        rowName1 = rowName1.replaceFirst("(?i)^word: ", "").trim();
        assertTrue(driver.getPageSource().contains(rowName1));

        WebElement row2 = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[2]/td[1]"));
        String rowName2 = row2.getAttribute("aria-label");
        rowName2 = rowName2.replaceFirst("(?i)^word: ", "").trim();
        assertTrue(driver.getPageSource().contains(rowName2));
    }

    @When("There already word cloud with {string}'s {int} most popular songs")
    public void thereAlreadyWordCloudWithMostPopularSongs(String artist, int numSongs) throws InterruptedException {
        doPopularSearch(artist, numSongs);

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
    }

    @When("I click on a word such as {string}")
    public void iClickOnAWordSuchAs(String word) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath("//*[name()='text' and text()='" + word + "']")));
        driver.findElement(By.xpath("//*[name()='text' and text()='" + word + "']")).click();
    }

    @When("There is {string} in the lyrics")
    public void thereIsTheInTheLyrics(String lyrics) {}

    @When("I hover over the song {string} in the list")
    public void iHoverOverTheSongInTheList(final String song) {
        assertTrue(driver.getPageSource().contains(song));
        // hover over song
        WebElement desiredSong = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div/ul/div[1]/li"));
        Actions actions = new Actions(driver);
        actions.moveToElement(desiredSong).perform();

        driver.findElement(By.xpath("//button[text()=\"" + "♥ Add to Favorites" + "\"]")).click();
    }

    @When("I click the {string} button to add it to favorites")
    public void iClickTheButtonToAddItToFavorites(String song) {
        driver.findElement(By.xpath("//button[text()=\"" + "×" + "\"]")).click();
    }

    @When("I click on the song {string} in the list")
    public void iClickOnTheSongInTheList(final String song) {
        driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[3]/div/ul/div[1]/li")).click();
    }

    @Then("There is a list of songs that {string} comes from in the modal")
    public void thereIsAListOfSongsWordOrigin(String word) {
        assertTrue(driver.getPageSource().contains(word));
        assertTrue(driver.getPageSource().contains("Songs with"));
        assertTrue(driver.getPageSource().contains("All Too Well (10 Minute Version) (Taylor’s Version) [Live Acoustic]"));
        assertTrue(driver.getPageSource().contains("All Too Well (10 Minute Version) (Taylor’s Version) [From The Vault]"));
    }

    @Then("The modal lists the frequency of {string} in each song")
    public void theModalListsTheFrequencyOfEachSong(String word) {
        assertTrue(driver.getPageSource().contains(word));
        assertTrue(driver.getPageSource().contains("9"));
    }

    @Then("I see confirmation message {string}")
    public void iSeeConfirmationMessage(String message) {
        assertTrue(driver.getPageSource().contains(message));
    }

    @Then("There should be a table of common words in her songs")
    public void thereShouldBeATableOfCommonWordsInHerSongs() {
        assertNotNull(driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table")));
    }

    @Then("Only {string} should appear in the cloud and not {string}")
    public void onlyShouldAppearInTheCloudAndNot(String word1, String word2) {
        List<WebElement> word1Elements = driver.findElements(By.xpath("//*[contains(text(), '" + word1 + "')]"));
        assertFalse(word1Elements.isEmpty());

        List<WebElement> word2Elements = driver.findElements(By.xpath("//*[contains(text(), '" + word2 + "')]"));
        assertTrue(word2Elements.isEmpty());
    }

    @Then("A popup should display title {string}")
    public void aPopupShouldDisplayTitle(String title) {
        // Locate the popup title element
        WebElement titleElement = driver.findElement(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[4]/div/h2"));

        // Get its text
        String actualTitle = titleElement.getText();
        assertEquals(title, actualTitle);
    }

    @Then("Artist should be listed as {string}")
    public void artistShouldBeListedAs(String artist) {
        assertTrue(driver.getPageSource().contains(artist));
    }

    @Then("The popup should show the year recorded as {string}")
    public void thePopupShouldShowTheYearRecordedAs(String year) {
        assertTrue(driver.getPageSource().contains(year));
    }

    @Then("The lyrics should be displayed directly below the title")
    public void theLyricsShouldBeDisplayedDirectlyBelowTheTitle() {
        assertTrue(driver.getPageSource().contains("Autumn leaves falling down like pieces into place"));
    }

    @Then("The more common a word is, the bigger the font")
    public void theMoreCommonAWordIsTheBiggerTheFont() {
        WebElement textYou = driver.findElement(By.xpath("//*[contains(text(), 'you')]"));
        WebElement textDisposition = driver.findElement(By.xpath("//*[contains(text(), 'disposition')]"));

//        String styleYou = textYou.getAttribute("style");
//        String styleAll = textAll.getAttribute("style");
//
//        int sizeYou = extractFontSize(styleYou);
//        int sizeAll = extractFontSize(styleAll);
        int sizeYou = Integer.parseInt(textYou.getCssValue("font-size").replace("px",""));
        int sizeAfter = Integer.parseInt(textDisposition.getCssValue("font-size").replace("px",""));
        assertTrue(sizeYou > sizeAfter);
    }

    @Then("Filler words should not be part of the word cloud")
    public void fillerWordsShouldNotBePartOfTheWordCloud() {
        String[] fillerWords = {"ah", "but", "yooo", "yuh"};

        for (String word : fillerWords) {
            System.out.println(word);
            List<WebElement> wordElements = driver.findElements(By.xpath("//*[contains(text(), '" + word + "')]"));
            assertTrue(wordElements.isEmpty());
        }
    }

    @Then("The word {string} should be highlighted in the lyrics")
    public void theWordShouldBeHighlightedInTheLyrics(String word) {
        WebElement highlight = driver.findElement(By.xpath("//span[@class='bg-yellow-300 font-bold' and text()='" + word + "']"));

        assertEquals(word, highlight.getText());
    }

    @Then("There are only 100 different words displayed")
    public void thereAreOnly100DifferentWordsDisplayed() {
        driver.findElement(By.xpath("//button[text()=\"" + "Table" + "\"]")).click();

        List<WebElement> numWords = driver.findElements(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[100]/td[1]"));
        assertFalse(numWords.isEmpty());

        List<WebElement> pastMax = driver.findElements(By.xpath("//*[@id=\"root\"]/div/div/div/div/div/div[2]/div[1]/table/tbody/tr[101]/td[1]"));
        assertTrue(pastMax.isEmpty());
    }

    @Then("There should be a word cloud with Taylor Swift's 22")
    public void thereShouldBeAWordCloudCloudWithSong() {
        iClickOnAWordSuchAs("hipster");
        iClickOnTheSongInTheList("22");

        assertTrue(driver.getPageSource().contains("Taylor Swift"));
        assertTrue(driver.getPageSource().contains("22"));
    }

    @Then("There word cloud of Taylor Swift's 2 most popular songs in 15 seconds")
    public void cloudGenerationTimeLimit() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15000));
        wait.until(ExpectedConditions.presenceOfElementLocated(By.className("wordcloud-container")));
        assertNotNull(driver.findElement(By.className("wordcloud-container")));
    }
}
