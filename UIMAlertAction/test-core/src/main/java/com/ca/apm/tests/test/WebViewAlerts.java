package com.ca.apm.tests.test;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ca.apm.tests.testbed.WindowsStandaloneTestbed;
import com.ca.tas.role.EmRole;

public class WebViewAlerts extends WebViewLoginLogout {

    static final Logger LOGGER = LoggerFactory.getLogger(WebViewLoginLogout.class);
    WebElement we = null;

    public void harvestWait(int seconds) {
        try {
            LOGGER.info("Harvesting crops for " + String.valueOf(seconds) + " seconds");
            Thread.sleep(seconds * 1000);
            LOGGER.info("Crops harvested.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void clickonInvestigator() throws InterruptedException {

        /*
         * we = waitExplicitPresenceOfElementByXPath(WEBVIEW_INVESTIGATOR);
         * we.click();
         * LOGGER.info("Clicked on Investigator Tab");
         */
        String hostName =
            envProperties.getMachineHostnameByRoleId(WindowsStandaloneTestbed.EM_ROLE_ID);
        String InvestigatorURL =
            "http://" + hostName + ":" + webviewPort + "/#investigator;smm=false;tr=0";
        fd.navigate().to(InvestigatorURL);
        waitExplicitPresenceOfElement(WEBVIEW_INVESTIGATOR_SUPERDOMAIN);
    }

    public void clickonSuperDomain() {

        LOGGER.info(fd.getCurrentUrl());
        we = waitExplicitPresenceOfElement(WEBVIEW_INVESTIGATOR_SUPERDOMAIN);
        doubleClick(we);
        LOGGER.info("Clicked on SuperDomain Node");
    }

    public void clickonAgentNode() {
        initializeEMandAgents();
        we = waitExplicitPresenceOfElement(WEBVIEW_INVESTIGATOR_AGENTNODE);
        // we.click();
        doubleClick(we);
        LOGGER.info("Clicked on AgentNode");
    }

    public void clickonAgentNameNode() {
        initializeEMandAgents();
        we = waitExplicitPresenceOfElement(WEBVIEW_AGENTNODE_AGENTNAME);
        we.click();
        LOGGER.info("Clicked on Agent Name Node");
    }

    public void clickonAgentDomainNameNode() {
        initializeEMandAgents();
        we = waitExplicitPresenceOfElement(WEBVIEW_AGENTNAME_AGENTDOMAIN);
        we.click();
        LOGGER.info("Clicked on Agent Domain Name Node");
    }

    public void clickonAgentGCHeapNode() {
        initializeEMandAgents();
        we = waitExplicitPresenceOfElement(WEBVIEW_AGENTDOMAIN_GCHEAPNODE);
        we.click();
        LOGGER.info("Clicked on Agent GC Heap Node");
    }

    public void clickonBytesInUse_GCHeapNode() {
        initializeEMandAgents();

        we = waitExplicitPresenceOfElementByXPath(WEBVIEW_AGENTDOMAIN_GCHEAPNODE_BIU);
        if (we.isDisplayed()) {
            we.click();
        } else {
            LOGGER.info("Not able to click on GC Heap Bytes In use");
        }
    }


    public void clickonAlert() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT);
        we.click();
    }

    public void defineAlert() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_NAME);
        we.clear();
        we.sendKeys("BytesInUseAlertForUIM");
        we = waitExplicitPresenceOfElement(NEWTTACTION_NAME_MGMT);
        we.clear();
        we.sendKeys("Default");
        we.sendKeys(Keys.ENTER);
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_OK);
        we.click();
        harvestWait(10);

    }

    public void clickonCreatedAlert() {

        we = waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_ALERTS);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_ALERTS_SELECT);
        we.click();

    }

    public void reverseClickonCreatedAlert() {
        we = waitExplicitPresenceOfElement(MANAGEMENT_ALERTS);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();

    }

    public void alertActivate() {

        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_ACTIVE);
        we.click();
        LOGGER.info("Clicked on 'ACTIVE' checkbox");
    }

    public void actionSelectandChooseAction() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ACTION_SELECTUIM);
        we.click();
        we = waitExplicitPresenceOfElement(WEBVIEW_ACTION_CHOOSE);
        we.click();

    }

    public void addActionDanger() {
        we = waitExplicitPresenceOfElement(WEBVIEW_DANGER_THRESHOLD);
        we.clear();
        we.sendKeys("40");
        we = waitExplicitPresenceOfElement(WEBVIEW_ACTION_ADD_DANGER);
        JavascriptExecutor je = (JavascriptExecutor) fd;
        je.executeScript("arguments[0].scrollIntoView(true);", we);
        LOGGER.info("Add Button Label in Danger Threshold:" + we.getText());
        boolean flag = we.isDisplayed();
        if (flag) {
            we.click();
        } else {

            LOGGER.info("Element is not visible ..." + we);
        }
        actionSelectandChooseAction();

    }

    public void addActionCaution() {
        we = waitExplicitPresenceOfElement(WEBVIEW_CAUTION_THRESHOLD);
        we.clear();
        we.sendKeys("10");
        we = waitExplicitPresenceOfElement(WEBVIEW_ACTION_ADD_CAUTION);
        JavascriptExecutor je = (JavascriptExecutor) fd;
        je.executeScript("arguments[0].scrollIntoView(true);", we);
        LOGGER.info("Add Button Label in Caution Threshold" + we.getText());
        we.click();
        actionSelectandChooseAction();
    }

    public void alertCombinationAll() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_COMBINATION);
        we.clear();
        we.sendKeys("all");
        we.sendKeys(Keys.ENTER);

    }

    public void alertNotifyIndividualMetric() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_NOTIFYINDIVIDUAL);
        we.click();
    }

    public void alertTriggerSelection() {
        we = waitExplicitPresenceOfElement(WBVIEW_ALERT_TRIGGERNOTIFY);
        we.clear();
        we.sendKeys("Each Period While Problem Exists");
        we.sendKeys(Keys.ENTER);

    }

    public void alertResolutionSelection() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_RESOLUTION);
        we.clear();
        // we.sendKeys("1 minute");
        we.sendKeys("30 seconds");
        we.sendKeys(Keys.ENTER);

    }

    public void alertApply() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_APPLY);
        we.click();
    }

    public void alertRevert() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_REVERT);
        we.click();
    }

    public void alertDelete() {
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_DELETE);
        we.click();
        we = waitExplicitPresenceOfElement(WEBVIEW_ALERT_DELETE_YES);
        we.click();
        harvestWait(50);
    }

    public void createEntireAlert() throws InterruptedException {
        clickonInvestigator();
        clickonSuperDomain();
        clickonAgentNode();
        clickonAgentNameNode();
        clickonAgentDomainNameNode();
        clickonAgentGCHeapNode();
        clickonBytesInUse_GCHeapNode();
        rightClick();
        clickonAlert();
        defineAlert();
        reverseClickonCreatedAlert();
        clickonCreatedAlert();

    }

    public void clickonMangementModule() {
        boolean isManagement = fd.findElementByXPath(WEBVIEW_MANAGEMENTMODULE).isDisplayed();
        if (isManagement) {
            we = waitExplicitPresenceOfElementByXPath(WEBVIEW_MANAGEMENTMODULE);
            we.click();
        }

        LOGGER.info("Clicked on Management Module Tab");
    }

    public void createUIMAction() {
        LOGGER.info("Creating UIM Action");
        // clickonMangementModule();
        we = waitExplicitPresenceOfElement(MANAGEMENTMODULE_ELEMENT);
        we.click();
        we = waitExplicitPresenceOfElement(ELEMENT_NEWACTION);
        mouseHover(we);
        we = waitExplicitPresenceOfElement(ELEMENT_NEWUIMACTION);
        LOGGER.info("*********Action name******" + we.getText());
        we.click();
        we = waitExplicitPresenceOfElement(NEWTTACTION_NAME);
        we.clear();
        we.sendKeys("UIMAction");
        we = waitExplicitPresenceOfElement(NEWTTACTION_NAME_MGMT);
        we.clear();
        we.sendKeys("Default");
        we.sendKeys(Keys.ENTER);
        we = waitExplicitPresenceOfElement(NEWTTACTION_OK);
        we.click();
        LOGGER.info("Waiting for the UIM Action");
        harvestWait(50);
        LOGGER.info("Created the UIM Action");
    }

    public void movetoAction() {
        LOGGER.info("Moved to UIM Action Created");

        try {
            we = waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
            we.click();
            we = waitExplicitPresenceOfElementByXPath(MANAGEMENT_MODULES1);
            we.click();
            we = waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
            we.click();
            we = waitExplicitPresenceOfElement(MANAGEMENT_ACTION);
            doubleClick(we);
            we = waitExplicitPresenceOfElement(MANAGEMENT_ACTION_SELECT);
            we.click();
        } catch (NoSuchElementException e) {

            e.printStackTrace();
        }
    }

    public boolean isAllFieldsPresent() {

        boolean allFieldsPresent = false;

        WebElement inpFld1 = waitExplicitPresenceOfElementByXPath(label1);
        // WebElement inpFld2 = waitExplicitPresenceOfElementByXPath(chkUIMSecured);
        WebElement inpFld3 = waitExplicitPresenceOfElementByXPath(label2);
        WebElement inpFld4 = waitExplicitPresenceOfElementByXPath(label3);
        WebElement inpFld5 = waitExplicitPresenceOfElementByXPath(label4);
        WebElement inpFld6 = waitExplicitPresenceOfElementByXPath(label5);
        WebElement inpFld7 = waitExplicitPresenceOfElementByXPath(label6);
        WebElement inpFld8 = waitExplicitPresenceOfElementByXPath(label7);
        WebElement inpFld9 = waitExplicitPresenceOfElementByXPath(label8);
        WebElement inpFld10 = waitExplicitPresenceOfElementByXPath(label9);
        // WebElement inpFld11 = waitExplicitPresenceOfElementByXPath(chkWVSecured);
        // WebElement inpFld12 = waitExplicitPresenceOfElementByXPath(label10);

        if (inpFld1.isDisplayed() && inpFld3.isDisplayed() && inpFld4.isDisplayed()
            && inpFld5.isDisplayed() && inpFld6.isDisplayed() && inpFld7.isDisplayed()
            && inpFld7.isDisplayed() && inpFld8.isDisplayed() && inpFld9.isDisplayed()
            && inpFld10.isDisplayed()) {
            allFieldsPresent = true;
            return allFieldsPresent;

        } else {

            return allFieldsPresent;
        }
    }

    public void generateValidationMessage(String identifier1, String Identifier2)

    {

        we = waitExplicitPresenceOfElementByXPath(identifier1);
        we.clear();
        WebElement we2 = waitExplicitPresenceOfElementByXPath(Identifier2);
        pressTab(we2);

    }

    public void reverseMoveToAction() {
        we = waitExplicitPresenceOfElement(MANAGEMENT_ACTION);
        doubleClick(we);
        we = waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();
    }

    public void actionActive() {
        we = waitExplicitPresenceOfElement(NEWACTION_ACTIVE);
        we.click();
    }

    public void actionDelete() {
        we = waitExplicitPresenceOfElement(NEWUIMACTION_DELETE);
        we.click();
        we = waitExplicitPresenceOfElement(NEWACTION_DELETE_YES);
        we.click();
        try {
            harvestWait(50);
        } finally {
            LOGGER.info("Action Deleted ...");
        }
    }

    public void createEntireUIMAction() {
        LOGGER.info("Create the Entire UIM Action along with all Input fields");

        try {
            // Create UIM Action...
            initBrowser();
            loginToTeamCenter();
            moveToWebView();
            clickonMangementModule();
            createUIMAction();

            // Below Output will let us know which page it is in
            LOGGER.info(fd.getCurrentUrl());

            actionActive();
            WebElement inpFld1 = waitExplicitPresenceOfElementByXPath(label1);
            inpFld1.clear();
            inpFld1.sendKeys(UIMHost);

            WebElement inpFld3 = waitExplicitPresenceOfElementByXPath(label2);
            inpFld3.clear();
            inpFld3.sendKeys(UIMPort);

            WebElement inpFld4 = waitExplicitPresenceOfElementByXPath(label3);
            inpFld4.clear();
            inpFld4.sendKeys(UIMUserId);

            WebElement inpFld5 = waitExplicitPresenceOfElementByXPath(label4);
            inpFld5.clear();
            inpFld5.sendKeys(UIMPassword);

            WebElement inpFld6 = waitExplicitPresenceOfElementByXPath(label5);
            inpFld6.clear();
            inpFld6.sendKeys(UIMDomain);

            WebElement inpFld7 = waitExplicitPresenceOfElementByXPath(label6);
            inpFld7.clear();
            inpFld7.sendKeys(UIMHub);

            WebElement inpFld8 = waitExplicitPresenceOfElementByXPath(label7);
            inpFld8.clear();
            inpFld8.sendKeys(UIMRobot);

            WebElement inpFld9 = waitExplicitPresenceOfElementByXPath(label8);
            inpFld9.clear();
            inpFld9.sendKeys(UIM_MM);

            WebElement inpFld10 = waitExplicitPresenceOfElementByXPath(label9);
            inpFld10.clear();
            String emhost =
                envProperties.getMachineHostnameByRoleId(WindowsStandaloneTestbed.EM_ROLE_ID);
            inpFld10.sendKeys(emhost);

            WebElement inpFld12 = waitExplicitPresenceOfElementByXPath(label10);
            inpFld12.clear();
            inpFld12.sendKeys(String.valueOf(webviewPort));
            we = waitExplicitPresenceOfElementByXPath(BTNAPPLY);

            if (we.isEnabled()) {
                we.click();
                LOGGER.info("clicked on APPLY button in UIM Alert Action window");
            } else {

                LOGGER.info("APPLY Button in UIM ALERT Action is not enabled...");
            }
        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void moveToMetricGroupExpression() {
        // logoutandLoginFromWebView();
        // clickonMangementModule();
        we = waitExplicitPresenceOfElement(MANAGEMENT_SUPERDOMAIN);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_MODULES);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_DEFAULT);
        we.click();
        we = waitExplicitPresenceOfElement(MANAGEMENT_METRICGROUPING);
        we.click();
        we = waitExplicitPresenceOfElement(MANGEMENT_REGISTERMETRICGROUP);
        we.click();
    }


    public void verifyTraceForHelloWorld() {
        we = waitExplicitPresenceOfElement(REGISTERPATIENT_TRACE);
        we.click();
        we = waitExplicitPresenceOfElement(REGISTERPATIENT_TRACE);
    }


    public void startEmandWebView() {
        initializeEMandAgents();
        runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_START_WEBVIEW);
    }

    public void stopEmandWebView() {
        initializeEMandAgents();
        stopEM(emRoleId);
        File source = new File("C:\\windows\\System32\\taskkill.exe");
        File destination = new File("C:\\automation\\deployed\\em");
        try {
            FileUtils.copyFileToDirectory(source, destination);

        } catch (IOException e) {
            e.printStackTrace();
        }
        runSerializedCommandFlowFromRole(emRoleId, EmRole.ENV_STOP_WEBVIEW);

        File traces = new File("C:\\automation\\deployed\\em\\traces");
        File data = new File("C:\\automation\\deployed\\em\\data");
        try {
            FileUtils.deleteDirectory(traces);
            FileUtils.deleteDirectory(data);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}
