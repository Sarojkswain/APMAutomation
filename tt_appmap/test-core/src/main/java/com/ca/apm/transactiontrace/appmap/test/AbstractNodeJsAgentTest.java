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

package com.ca.apm.transactiontrace.appmap.test;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.transactiontrace.appmap.pages.tixchange.CheckoutPage;
import com.ca.apm.transactiontrace.appmap.pages.tixchange.ConcertsPage;
import com.ca.apm.transactiontrace.appmap.pages.tixchange.FrontPage;
import com.ca.apm.transactiontrace.appmap.pages.tixchange.JohnEltonPage;
import com.ca.apm.transactiontrace.appmap.pages.tixchange.ShoppingCartPage;
import com.ca.apm.transactiontrace.appmap.testbed.NodeJsAgentStandAloneTestbed;
import com.ca.tas.role.TixChangeRole;

/**
 * Abstract superclass for tests that start a TT session during which they execute some actions
 * on TixChange and then log into the Team Center and request the graph object using private API.
 *
 * We check whether there are any edges in the resulting graph.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public abstract class AbstractNodeJsAgentTest extends AbstractAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractNodeJsAgentTest.class);

    protected AbstractNodeJsAgentTest(DesiredCapabilities dc) {
        super(dc);
    }

    protected void verifyWithNodeJsAgent() {
        startTixChangeNodeApp();

        try {
            initiateTTSession(NodeJsAgentStandAloneTestbed.INITIATE_TT_SESSION_ROLE_ID);

            executeTixChangeActions();

            LOGGER.info("Waiting 60s for the TT processing...");
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for the TT processing", e);
            }

            checkAtcGraph(NodeJsAgentStandAloneTestbed.EM_ROLE_ID);
        } finally {
            stopTixChangeNodeApp();
        }
    }

    private void executeTixChangeActions() {
        LOGGER.info("Accessing TixChange at " + getTixChangeUrl());
        driver.get(getTixChangeUrl());

        FrontPage frontPage = new FrontPage(driver);
        frontPage.checkFrontPageContent();
        frontPage.typeUserId("user1@users.com");
        frontPage = frontPage.submitLogin();

        ConcertsPage concertsPage = frontPage.clickConcertsLink();
        concertsPage.checkConcertsPageContent();

        JohnEltonPage johnEltonPage = concertsPage.clickJohnEltonLink();
        johnEltonPage.checkJohnEltonPageContent();
        johnEltonPage.typeSection111Quantity("2");
        johnEltonPage.clickSection111AddToCartButton();

        ShoppingCartPage shoppingCartPage = johnEltonPage.clickViewCartButton();
        shoppingCartPage.checkShoppingCartPageContent();

        CheckoutPage checkoutPage = shoppingCartPage.clickCheckoutButton();
        checkoutPage.checkCheckoutContent();
        checkoutPage.typeCreditCardNumber("9999 9999 9999 9999");
        frontPage = checkoutPage.clickCheckout();
    }

    private void startTixChangeNodeApp() {
        runSerializedCommandFlowFromRole(NodeJsAgentStandAloneTestbed.TIXCHANGE_ROLE,
            TixChangeRole.ENV_TIXCHANGE_START);
    }

    private void stopTixChangeNodeApp() {
        runSerializedCommandFlowFromRole(NodeJsAgentStandAloneTestbed.TIXCHANGE_ROLE,
            TixChangeRole.ENV_TIXCHANGE_STOP);
    }

    private String getTixChangeUrl() {
        return envProperties.getRolePropertiesById(NodeJsAgentStandAloneTestbed.TOMCAT_ROLE_ID)
            .getProperty(NodeJsAgentStandAloneTestbed.TIXCHANGE_NODE_CONTEXT + "_url");
    }

}
