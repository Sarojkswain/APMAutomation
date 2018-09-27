package com.ca.apm.saas.test;

import java.io.PrintWriter;

import org.apache.commons.lang3.RandomStringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.saas.pagefactory.OnBoardingPage;
import com.ca.apm.saas.pagefactory.RegistrationPage;
import com.ca.apm.saas.testbed.SaasUITestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * @author sinab10, kurma05
 *
 */
public class NewRegistrationTest extends SaaSBaseTest {

    public final static String DEV_INFO_FILE_NAME = "registrationCredentials.txt";
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    /**
     * Registers for a new SAAS instance
     */
    @Tas(testBeds = @TestBed(name = SaasUITestbed.class, executeOn = SaasUITestbed.MACHINE_ID), size = SizeType.MEDIUM, owner = "sinab10")
    @Test(enabled = true)
    public void newRegistration() {

        PrintWriter writer = null;
        RemoteWebDriver driver = ui.getDriver();

        try {
            // Create a new registration page object
            RegistrationPage regPage = new RegistrationPage(driver);
            regPage.fetchPage();

            String emailAddress = RandomStringUtils.randomAlphabetic(6) + "@automation.com";
            password = "interOP@123";

            // Pass all the values (arguments) needed to create a new SaaS instance to the
            // RegistrationPage object
            regPage.fillFormValues("SaaS", "Automation", emailAddress, password);

            // Submit the filled-in form
            regPage.submitForm();            
            regPage.newInstanceURL = loginURL;

            // Write down the credentials and new instance's URL in a file.
            writer = new PrintWriter(DEV_INFO_FILE_NAME, "UTF-8");
            writer.println(emailAddress);
            writer.println(password);
         
            OnBoardingPage onBoardingPg = new OnBoardingPage(driver);
            Assert.assertTrue(onBoardingPg.isElementPresent(driver, onBoardingPg.getWebElementApmBtn()),
                "Onboarding page didn't load properly");
            onBoardingPg.clickApmButton();

            // Check for and close the popup, if present
            regPage.closePopUp();
            
            Assert.assertTrue(onBoardingPg.isElementPresent(driver, driver.findElement(By.id("settings-agents-link"))),
                "Instance home page doesn't load correctly");
            Thread.sleep(5000);
            logger.info("New SAAS instance was created. Email '{}', Password '{}'.", emailAddress, password);
            
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("Error occurred while test execution: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}