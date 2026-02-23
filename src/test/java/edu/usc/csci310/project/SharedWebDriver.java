package edu.usc.csci310.project;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Wait;

public class SharedWebDriver {
    private static WebDriver driver = null;
    public static WebDriver getWebDriver() {
        if (driver == null) {
            ChromeOptions options = new ChromeOptions();
            options.setAcceptInsecureCerts(true);
            options.addArguments("--ignore-certificate-errors");
            options.addArguments("--allow-insecure-localhost");
            options.addArguments("--remote-allow-origins=*");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            driver = new ChromeDriver(options);
        }
        return driver;
    }

    public static void quitWebDriver() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    public static String getErrorMessage(Wait<WebDriver> waitForError){
        return waitForError.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div/div/div/div[1]/div/span"))).getText();
    }
//    private static final WebDriver driver = new ChromeDriver();
//
//    public static WebDriver getWebDriver() {
//        return driver;
//    }
//
//    public static void quitWebDriver() {
//        driver.quit();
//    }
//
//    public static String getErrorMessage(Wait<WebDriver> waitForError) {
//        return waitForError.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("/html/body/div/div/div/div[1]/div/span"))).getText();
//    }
}
