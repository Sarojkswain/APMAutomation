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

package com.ca.apm.transactiontrace.appmap.pages.tixchange;

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
 * Checkout page of TixChange
 */
public class CheckoutPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(CheckoutPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String pageUrl;

    @FindBy(xpath = "//*[@data-reactid='.0.2.0.0.0']")
    private WebElement checkoutHeader;
    @FindBy(xpath = "//*[@data-reactid='.0.2.0.0.1.2.1.0']")
    private WebElement creditCardField;
    @FindBy(xpath = "//*[@data-reactid='.0.2.1.1.0.0']")
    private WebElement checkoutButton;

    public CheckoutPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public CheckoutPage typeCreditCardNumber(String creditCardNumber) {
        LOGGER.info("Enter credit card number: " + creditCardNumber);
        creditCardField.sendKeys(creditCardNumber);
        return this;
    }

    public FrontPage clickCheckout() {
        LOGGER.info("Click checkout button");
        checkoutButton.click();
        FrontPage webViewPage = new FrontPage(driver);
        return webViewPage;
    }

    public void checkCheckoutContent() {
        LOGGER.info("Checking Checkout page content");
        wait.until(ExpectedConditions.visibilityOf(creditCardField));

        assertTrue(checkoutHeader.isDisplayed(), "Checkout page header not found");
        assertEquals(checkoutHeader.getText(), "Checkout");
        assertTrue(creditCardField.isDisplayed(), "Credit card field not found");
        assertTrue(checkoutButton.isDisplayed(), "Checkout button not displayed");
        assertTrue(checkoutButton.isEnabled(), "Checkout button not enabled");
    }

    public String getPageUrl() {
        return pageUrl;
    }

}
