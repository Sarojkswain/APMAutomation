/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.pages.tixchange;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertEquals;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * John Elton page of TixChange
 */
public class JohnEltonPage {
    private static final Logger LOGGER = LoggerFactory.getLogger(JohnEltonPage.class);

    private final WebDriver driver;
    private final WebDriverWait wait;
    private final String pageUrl;
    
    @FindBy(xpath = "//*[@data-reactid='.0.2.0.0.0.1.0']")
    private WebElement johnEltonHeader;
    @FindBy(xpath = "//*[@data-reactid='.0.2.0.2.3.$K5.4.0']")
    private WebElement section111QuantityField;
    @FindBy(xpath = "//*[@data-reactid='.0.2.0.2.3.$K5.5']")
    private WebElement section111AddToCardButton;
    @FindBy(xpath = "//*[@data-reactid='.0.0.0.2.0.0']")
    private WebElement viewCartButton;

    public JohnEltonPage(WebDriver driver) {
        this.driver = driver;
        wait = new WebDriverWait(driver, 60);
        PageFactory.initElements(driver, this);
        pageUrl = driver.getCurrentUrl();
        LOGGER.info(pageUrl);
    }

    public JohnEltonPage typeSection111Quantity(String quantity) {
        LOGGER.info("Enter Section 111's quantity: " + quantity);
        section111QuantityField.clear();
        section111QuantityField.sendKeys(quantity);
        return this;
    }
    
    public JohnEltonPage clickSection111AddToCartButton() {
        LOGGER.info("Click Section 111's Add to cart");
        section111AddToCardButton.click();
        wait.until(ExpectedConditions.visibilityOf(viewCartButton));
        return this;
    }
    
    public ShoppingCartPage clickViewCartButton() {
        LOGGER.info("Click View cart");
        viewCartButton.click();
        ShoppingCartPage webViewPage = new ShoppingCartPage(driver);
        return webViewPage;
    }
    
    public void checkJohnEltonPageContent() {
        LOGGER.info("Checking John Elton page content");
        wait.until(ExpectedConditions.visibilityOf(section111QuantityField));

        assertTrue(johnEltonHeader.isDisplayed(), "John Elton page header not found");
        assertEquals(johnEltonHeader.getText(), "John Elton");
        assertTrue(section111QuantityField.isDisplayed(), "Section 111's Quantity field not found");
        assertTrue(section111AddToCardButton.isDisplayed(), "Section 111's Add to cart button not displayed");
        assertTrue(section111AddToCardButton.isEnabled(), "Section 111's Add to cart button not enabled");
    }

    public String getPageUrl() {
        return pageUrl;
    }

}
