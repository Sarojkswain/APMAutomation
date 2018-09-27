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

package com.ca.apm.idp.internal.pages;

import static org.testng.Assert.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CemPage {
    private static final Logger log = LoggerFactory.getLogger(CemPage.class);

    private WebDriverWait wait;


    @FindBy(className = "ca-user-name")
    private WebElement userLink;
    
    @FindBy(className="ca-logout-link")
    private WebElement logoutLink;

    public CemPage(WebDriver driver) {
        super();
        PageFactory.initElements(driver, this);
        wait = new WebDriverWait(driver, 30);
    }


    public void checkContent() {
        log.info("Validate user logged in");
        assertTrue(userLink.isDisplayed(), "User link not found");
        String text = userLink.getText();
        assertEquals(text, "admin", "User is not admin");        
    }

    public CemPage waitToLoad() {
        wait.until(ExpectedConditions.visibilityOf(logoutLink));
        return this;
    }

}
