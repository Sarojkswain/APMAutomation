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

import com.ca.apm.transactiontrace.appmap.pages.magento.CheckoutPage;
import com.ca.apm.transactiontrace.appmap.pages.magento.FrontPage;
import com.ca.apm.transactiontrace.appmap.pages.magento.ShoppingCartPage;
import com.ca.apm.transactiontrace.appmap.pages.magento.ToriTankPage;
import com.ca.apm.transactiontrace.appmap.pages.magento.WomenNewArrivalsPage;
import com.ca.apm.transactiontrace.appmap.role.ApacheRole;
import com.ca.apm.transactiontrace.appmap.role.MagentoRole;
import com.ca.apm.transactiontrace.appmap.testbed.PhpAgentStandAloneTestbed;

/**
 * Abstract superclass for tests that start a TT session during which they execute some actions
 * on Magento and then log into the Team Center and request the graph object using private API.
 *
 * We check whether there are any edges in the resulting graph.
 *
 * @author Jan Zak (zakja01@ca.com)
 */
public abstract class AbstractPhpAgentTest extends AbstractAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPhpAgentTest.class);

    protected AbstractPhpAgentTest(DesiredCapabilities dc) {
        super(dc);
    }

    protected void verifyWithPhpAgent() {
        startApache();

        try {
            initiateTTSession(PhpAgentStandAloneTestbed.INITIATE_TT_SESSION_ROLE_ID);

            executeMagentoActions();

            LOGGER.info("Waiting 60s for the TT processing...");
            try {
                Thread.sleep(60 * 1000L);
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted while waiting for the TT processing", e);
            }

            checkAtcGraph(PhpAgentStandAloneTestbed.EM_ROLE_ID);
        } finally {
            stopApache();
        }
    }

    private void executeMagentoActions() {
        LOGGER.info("Accessing Magento at " + getMagentoUrl());
        driver.get(getMagentoUrl());

        FrontPage frontPage = new FrontPage(driver);
        frontPage.checkFrontPageContent();

        WomenNewArrivalsPage womenNewArrivalsPage = frontPage.clickWomenNewArrivalsLink();
        womenNewArrivalsPage.checkWomenNewArrivalsPageContent();

        ToriTankPage toriTankPage = womenNewArrivalsPage.clickToriTankLink();
        toriTankPage.checkToriTankPageContent();
        toriTankPage.clickColorIndigoSelector();
        toriTankPage.clickSizeMSelector();

        ShoppingCartPage shoppingCartPage = toriTankPage.clickAddToCartButton();
        shoppingCartPage.checkShoppingCartPageContent();

        CheckoutPage checkoutPage = shoppingCartPage.clickProceedToCheckoutButton();
        checkoutPage.checkCheckoutPageContent();
    }

    private void startApache() {
        runSerializedCommandFlowFromRole(PhpAgentStandAloneTestbed.APACHE_ROLE_ID,
            ApacheRole.APACHE_START);
    }

    private void stopApache() {
        runSerializedCommandFlowFromRole(PhpAgentStandAloneTestbed.APACHE_ROLE_ID,
            ApacheRole.APACHE_STOP);
    }

    private String getMagentoUrl() {
        return envProperties.getRolePropertiesById(PhpAgentStandAloneTestbed.MAGENTO_ROLE_ID)
            .getProperty(MagentoRole.ENV_MAGENTO_URL);
    }

}
