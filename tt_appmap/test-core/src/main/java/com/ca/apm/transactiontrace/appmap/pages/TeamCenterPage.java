package com.ca.apm.transactiontrace.appmap.pages;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.testng.Assert.assertTrue;

/**
 * Team Center landing page after login
 *
 * @author Sundeep (bhusu01)
 */
public class TeamCenterPage {

    private static final Logger LOGGER = LoggerFactory.getLogger(TeamCenterPage.class);
    private final WebDriver driver;
    private final String pageUrl;
    private final WebDriverWait wait;

    @FindBy(id = "menuItemMapId")
    private WebElement mapIcon;

    public TeamCenterPage(WebDriver driver) {
        LOGGER.info(driver.getCurrentUrl());

        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
    }

    public void checkTeamCenterPageContent() {
        wait.until(ExpectedConditions.visibilityOf(mapIcon));
        LOGGER.info("Login successful");
        assertTrue(mapIcon.isDisplayed(), "Map icon field is visible");
    }
}
