package com.ca.apm.tests.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.commons.tests.BaseAgentTest;

public class WebViewUIMTest extends WebViewAlerts {
    static final Logger LOGGER = LoggerFactory.getLogger(WebViewUIMTest.class);

    @BeforeTest
    public void runAll() {
        startEmandWebView();
        AgentOperations ao = new AgentOperations();
        ao.startAllAgents();
        LOGGER.info(" Starting EM, WebView, Agents...");
    }

    @Test(priority = 0,groups = {"BAT"})
    public void verifyDropDownOptions_TestCaseID_455803() {
        LOGGER.info(" ====> TestCaseID_455803 <====");
        try {
            initBrowser();
            loginToTeamCenter();
            moveToWebView();
            clickonMangementModule();
            we = waitExplicitPresenceOfElement(MANAGEMENTMODULE_ELEMENT);
            we.click();
            we = waitExplicitPresenceOfElement(ELEMENT_NEWACTION);
            mouseHover(we);
            we = waitExplicitPresenceOfElement(ELEMENT_NEWUIMACTION);
            String actualText = we.getText();
            String expectedText = "New UIM Alert Action";
            Assert.assertEquals(actualText, expectedText);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("TESTCASE_455803 :: FAILED");
        } finally {
            closeBrowser();
        }
    }

    @Test(priority = 1,groups = {"BAT"})
    public void verifyUIoptionsofUIMAlertAction_TestCaseID_455813()

    {
        LOGGER.info(" ====> TestCaseID_455813 <====");

        try {

            initBrowser();
            loginToTeamCenter();
            moveToWebView();
            clickonMangementModule();
            createUIMAction();
            reverseMoveToAction();
            movetoAction();
            isAllFieldsPresent();
            Assert.assertTrue(isAllFieldsPresent());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("TESTCASE_455813 :: FAILED");
        } finally {
            actionDelete();
            closeBrowser();
        }
    }

    @Test(priority = 2,groups = {"BAT"})
    public void createNewUIMAlertAction_TestCaseID_455812() {
        LOGGER.info(" ====> TestCaseID_455812 <====");
        try {
            initBrowser();
            loginToTeamCenter();
            moveToWebView();
            clickonMangementModule();
            createUIMAction();
            reverseMoveToAction();
            movetoAction();
            we = waitExplicitPresenceOfElement(NEWTTACTION_POSTCREATION_NAME);
            if (we.getAttribute("value").equals("UIMAction")) {
                LOGGER.info("createNewUIMAlertAction_TestCaseID_455812 is PASS");

            } else {
                LOGGER.info("createNewUIMAlertAction_TestCaseID_455812 is Failed");
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("TESTCASE_455812 :: FAILED");
        }

        finally {
            actionDelete();
            closeBrowser();
        }

    }

    @Test(priority = 3,groups = {"BAT"})
    public void validationMessagesForUIMAlertAction_TestCaseID_455815() {
        LOGGER.info(" ====> TestCaseID_455815 <====");
        try {
            initBrowser();
            loginToTeamCenter();
            moveToWebView();
            clickonMangementModule();
            createUIMAction();
            reverseMoveToAction();
            movetoAction();
            generateValidationMessage(label1, label2);
            boolean imgpresence1 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg1).isDisplayed();
            LOGGER.info("isHostIp Validation working :" + imgpresence1);

            generateValidationMessage(label2, label3);
            boolean imgpresence2 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg2).isDisplayed();
            LOGGER.info("isUIMRestWebServicePort Validation working :" + imgpresence2);

            generateValidationMessage(label3, label4);
            boolean imgpresence3 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg3).isDisplayed();
            LOGGER.info("isUIMUserId Validation working :" + imgpresence3);

            generateValidationMessage(label4, label5);
            boolean imgpresence4 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg4).isDisplayed();
            LOGGER.info("isUIMPassword Validation working :" + imgpresence4);

            generateValidationMessage(label5, label6);
            boolean imgpresence5 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg5).isDisplayed();
            LOGGER.info("isUIMDomain Validation working :" + imgpresence5);

            generateValidationMessage(label6, label7);
            boolean imgpresence6 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg6).isDisplayed();
            LOGGER.info("isUIMHub Validation working :" + imgpresence6);

            generateValidationMessage(label7, label8);
            boolean imgpresence7 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg7).isDisplayed();
            LOGGER.info("isUIMRobot Validation working :" + imgpresence7);

            generateValidationMessage(label8, label9);
            boolean imgpresence8 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg8).isDisplayed();
            LOGGER.info("isCAAPMWVHOST Validation working :" + imgpresence8);

            generateValidationMessage(label9, label10);
            boolean imgpresence9 =
                waitExplicitPresenceOfElementByXPath(imgvalidationmsg9).isDisplayed();
            LOGGER.info("isAPMWVPort Validation working :" + imgpresence9);

            if (imgpresence1 && imgpresence2 && imgpresence3 && imgpresence2 && imgpresence4
                && imgpresence5 && imgpresence6 && imgpresence7 && imgpresence8 && imgpresence9) {

                LOGGER.info("All Validation Messages are Generated Successfully");
                boolean VALIDATIONSUCCESS = true;
                Assert.assertTrue(VALIDATIONSUCCESS);
            } else {

                LOGGER.info("validationMessagesForUIMAlertAction_TestCaseID_455815 FAILED ");
                boolean VALIDATIONSUCCESS = false;
                Assert.assertTrue(VALIDATIONSUCCESS);
            }

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("TESTCASE_455815 :: FAILED");
        } finally {
            actionDelete();
            closeBrowser();
        }
    }


    @Test(priority = 4,groups = {"BAT"})
    public void triggerUIMAlertActionusingHTTPSettings_455814()
    
    {
        LOGGER.info(" ====> TestCaseID_455814 <====");
        AgentOperations ao = new AgentOperations();
        try {
            ao.startAllAgents();
            createEntireUIMAction();
            createEntireAlert();
            alertCombinationAll();
            alertTriggerSelection();
            alertActivate();
            addActionCaution();
            addActionDanger();
            alertApply();
            LOGGER.info("Alert Applied and waiting for 15 seconds to generate the log message");
            harvestWait(15);
            BaseAgentTest bat = new BaseAgentTest();
            bat.checkLogForMsg(envProperties, emMachineId, emlogFile, "HTTP Code : 204");

        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail("TESTCASE_455814 :: FAILED");
        } finally {
            actionDelete();
            closeBrowser();
            ao.stopAllAgents();
            stopEmandWebView();

        }
    }

}
