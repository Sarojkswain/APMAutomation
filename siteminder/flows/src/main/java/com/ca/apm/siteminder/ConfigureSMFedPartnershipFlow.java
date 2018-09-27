/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.siteminder;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.siteminder.pages.AdminUILoginPage;
import com.ca.apm.siteminder.pages.EntityConfigPage;
import com.ca.apm.siteminder.pages.EntityCreationWizard;
import com.ca.apm.siteminder.pages.PartnershipConfigPage;
import com.ca.apm.siteminder.pages.SMHomePage;

/**
 * @author Sundeep (bhusu01)
 */
@Flow
public class ConfigureSMFedPartnershipFlow extends FlowBase {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(ConfigureSMFedPartnershipFlow.class);

    @FlowContext
    ConfigureSMFedPartnershipFlowContext flowContext;

    WebDriver driver;

    @Override
    public void run() throws Exception {
        driver = new FirefoxDriver();
        driver.manage().window().setSize(new Dimension(1280, 1024));
        driver.get(flowContext.getAdminUIURL());

        // Log in to siteminder
        AdminUILoginPage loginPage = new AdminUILoginPage(driver);
        loginPage.overrideCertificateError(driver);
        loginPage.checkLoginPageContent();
        loginPage.typeUserName(flowContext.getAdminUser());
        loginPage.typePassword(flowContext.getAdminPassword());
        SMHomePage smHomePage = loginPage.clickLoginButton(driver);
        smHomePage.waitForLoginComplete();
        LOGGER.info("Logged in to siteminder");

        // Create local IdP Entity
        EntityConfigPage entityConfig = smHomePage.goToEntityConfig(driver);
        EntityCreationWizard creationWizard = entityConfig.clickOnCreateEntity();
        String idPBaseURL = "http://" + flowContext.getSMHost();
        if (!idPBaseURL.endsWith(".ca.com")) {
            idPBaseURL = idPBaseURL + ".ca.com";
        }
        entityConfig =
            creationWizard.createEntity(flowContext.getIdPEntityID(), flowContext.getIdpEntityName(), idPBaseURL, "principalName", "groups");

        entityConfig.verifyCreatedIdP(flowContext.getIdpEntityID());

        // Create metadata entity
        if (flowContext.isImportWvSP()) {
            entityConfig.importMetadata(flowContext.getWVMetadataPath(), flowContext.getWebViewSPID(), "sp_public_key");
        }
        if (flowContext.isImportWsSP()) {
            entityConfig.importMetadata(flowContext.getWsMetadataPath(), flowContext.getWebstartSPID(), "sp_public_key");
        }
        if (flowContext.isImportEmSP()) {
            entityConfig.importMetadata(flowContext.getEmMetadataPath(), flowContext.getEmSPID(), "sp_public_key");
        }

        // Configure partnership
        if (flowContext.isImportWvSP()) {
            PartnershipConfigPage partnershipConfigPage = new PartnershipConfigPage(driver);
            partnershipConfigPage.createIdPtoSPPartnership("WV_Partnership", flowContext.getWebViewSPID(), "admin", "admin", "admin",
                "http://" + flowContext.getSMHost() + "/affwebservices/redirectjsp/redirect.jsp");
            partnershipConfigPage.activatePartnership("WV_Partnership");
        }

        if (flowContext.isImportWsSP()) {
            PartnershipConfigPage partnershipConfigPage = new PartnershipConfigPage(driver);
            partnershipConfigPage.createIdPtoSPPartnership("WS_Partnership", flowContext.getWebstartSPID(), "admin", "admin", "admin",
                "http://" + flowContext.getSMHost() + "/affwebservices/redirectjsp/redirect.jsp");
            partnershipConfigPage.activatePartnership("WS_Partnership");
        }

        if (flowContext.isImportEmSP()) {
            PartnershipConfigPage partnershipConfigPage = new PartnershipConfigPage(driver);
            partnershipConfigPage.createIdPtoSPPartnership("EM_Partnership", flowContext.getEmSPID(), "admin", "admin", "admin",
                "http://" + flowContext.getSMHost() + "/affwebservices/redirectjsp/redirect.jsp");
            partnershipConfigPage.activatePartnership("EM_Partnership");
        }

        LOGGER.info("Closing Selenium driver");

        driver.close();
    }
}
