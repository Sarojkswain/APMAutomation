/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.pages.magento;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Women New Arrivals page of Magento
 */
public class WomenNewArrivalsPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(WomenNewArrivalsPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String pageUrl;

    @FindBy(xpath = "//h1")
    private WebElement newArrivalsHeader;
    @FindBy(css = "a[class='product-image'][title='Tori Tank']")
    private WebElement toriTankImageLink;

    public WomenNewArrivalsPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public ToriTankPage clickToriTankLink() {
        LOGGER.info("Click Tori Tank");
        toriTankImageLink.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // noop
        }
        ToriTankPage webViewPage = new ToriTankPage(driver);
        return webViewPage;
    }

    public void checkWomenNewArrivalsPageContent() {
        LOGGER.info("Checking Women New Arrivals page content");
        wait.until(ExpectedConditions.visibilityOf(toriTankImageLink));

        assertTrue(newArrivalsHeader.isDisplayed(), "New Arrivals header not found");
        assertEquals(newArrivalsHeader.getText().toUpperCase(), "NEW ARRIVALS");
        assertTrue(toriTankImageLink.isDisplayed(), "Tori Tank link not found");
    }

    public String getPageUrl() {
        return pageUrl;
    }

}
