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
 * Tori Tank page of Magento
 */
public class ToriTankPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(ToriTankPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String pageUrl;

    @FindBy(css = "span[class = 'h1']")
    private WebElement toriTankHeader;
    @FindBy(css = "a[id='swatch26']")
    private WebElement colorIndigoSelector;
    @FindBy(css = "a[id='swatch79']")
    private WebElement sizeMSelector;
    @FindBy(css = "div[class*='add-to-cart'] > button[class*='btn-cart']")
    private WebElement addToCartButton;
    

    public ToriTankPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public ToriTankPage clickColorIndigoSelector() {
        LOGGER.info("Click Color Indigo selector");
        colorIndigoSelector.click();
        return this;
    }
    
    public ToriTankPage clickSizeMSelector() {
        LOGGER.info("Click Size M selector");
        sizeMSelector.click();
        return this;        
    }
    
    public ShoppingCartPage clickAddToCartButton() {
        LOGGER.info("Click Add To Cart");
        addToCartButton.click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // noop
        }
        ShoppingCartPage webViewPage = new ShoppingCartPage(driver);
        return webViewPage;
    }

    public void checkToriTankPageContent() {
        LOGGER.info("Checking Tori Tank page content");
        wait.until(ExpectedConditions.visibilityOf(addToCartButton));

        assertTrue(toriTankHeader.isDisplayed(), "Tori Tank header not found");
        assertEquals(toriTankHeader.getText().toUpperCase(), "TORI TANK");
        assertTrue(colorIndigoSelector.isDisplayed(), "Color Indigo selector not displayed");
        assertTrue(sizeMSelector.isDisplayed(), "Size M selector not displayed");
        assertTrue(addToCartButton.isDisplayed(), "Add To Cart button not displayed");
        assertTrue(addToCartButton.isEnabled(), "Add To Cart button not enabled");
    }

    public String getPageUrl() {
        return pageUrl;
    }

}
