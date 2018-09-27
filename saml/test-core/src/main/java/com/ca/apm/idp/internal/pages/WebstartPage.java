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

import static org.testng.Assert.assertTrue;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebstartPage {
    private static final Logger log = LoggerFactory.getLogger(WebstartPage.class);

    private WebDriver driver;

    @FindBy(id = "manualLogin")
    private WebElement workstationLink;

    public WebstartPage(WebDriver driver) {
        super();
        this.driver = driver;
        PageFactory.initElements(driver, this);
    }


    public void startWorkstation() {
        log.info("Click workstation link");
        workstationLink.click();

    }

    public LoginPage webstartClickNotLogedIn() {
        log.info("Click workstation link (not logged in)");
        workstationLink.click();
        return new LoginPage(driver);
    }

    public void checkContent() {
        log.info("Validate workstation link");
        assertTrue(workstationLink.isDisplayed(), "Workstation link not found");
    }

}
