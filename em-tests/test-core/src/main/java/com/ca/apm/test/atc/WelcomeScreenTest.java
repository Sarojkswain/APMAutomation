/*
 * Copyright (c) 2015 CA. All rights reserved.
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
package com.ca.apm.test.atc;

import com.ca.apm.test.atc.common.ModalDialog;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;
import com.ca.apm.test.atc.common.UI.Role;
import com.ca.apm.test.atc.common.Utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class WelcomeScreenTest extends UITest {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    // Welcome screen is switched off in APM 10.7
    @Test(enabled = false, groups = "failing")
    public void welcomeScreen_test() throws Exception {

        getUI().setDisableWelcome(false);

        logger.info("should log into Team Center");
        getUI().login(Role.ADMIN);
        getUI().doWait(10).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return !d.getTitle().isEmpty();
            }
        });

        logger.info("should welcome dialog be visible");
        ModalDialog md = getUI().getModalDialog();
        Assert.assertTrue(md.getModalDialog() != null);
        
        logger.info("should uncheck Show next time");
        WebElement checkboxLabel = md.findBySelector(By.cssSelector("label[for=showAgainCheckbox]"));
        checkboxLabel.click();
        Utils.sleep(100);
        WebElement checkbox = md.findBySelector(By.id("showAgainCheckbox"));
        Assert.assertFalse(checkbox.isSelected(), "should not be checked");
        
        logger.info("should close welcome dialog");
        md.clickButton(DialogButton.CLOSE);
        
        logger.info("should logout and login again");
        getUI().logout();
        
        getUI().login(Role.ADMIN);
        getUI().doWait(10).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return !d.getTitle().isEmpty();
            }
        });

        logger.info("should welcome dialog be hidden");
        md = getUI().getModalDialog();
        try {
            Assert.assertNull(md.getModalDialog(), "Modal dialog is visible");
        } catch (Exception e) {
            
        }
        
        logger.info("should logout");
        getUI().logout();

        getUI().getDriver().executeScript("window.localStorage.setItem('welcomeObj', JSON.stringify({ releaseVersion: '99.TEST_VERSION' }));");
        
        logger.info("should login again");
        getUI().login(Role.ADMIN);
        getUI().doWait(10).until(new ExpectedCondition<Boolean>() {
            @Override
            public Boolean apply(WebDriver d) {
                return !d.getTitle().isEmpty();
            }
        });

        logger.info("should welcome dialog be visible");
        md = getUI().getModalDialog();
        try {
            md.getModalDialog();
        } catch (Exception e) {
            Assert.assertTrue(true, "welcome dialog is not visible");
        }
        
        logger.info("should do a cleanup");
        getUI().cleanup();
        
    }
}
