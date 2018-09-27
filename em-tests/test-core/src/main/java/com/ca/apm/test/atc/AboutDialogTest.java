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

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.common.BottomBar;
import com.ca.apm.test.atc.common.LeftNavigationPanel;
import com.ca.apm.test.atc.common.ModalDialog;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.Utils;
import com.ca.apm.test.atc.common.ModalDialog.DialogButton;

public class AboutDialogTest extends UITest {

    private final static Logger logger = Logger.getLogger(AboutDialogTest.class);

    @Test
    public void testAboutDialog() throws Exception {
        UI ui = getUI();
        LeftNavigationPanel nav = ui.getLeftNavigationPanel();

        logger.info("should log into APM Server");
        ui.login();

        logger.info("should open about dialog");
        nav.openAboutDialog();
        
        Utils.sleep(250);
        
        ModalDialog dialog = ui.getModalDialog();
        dialog.waitForModalDialogFadeIn();
        Assert.assertTrue(dialog.isModalDialogPresent());

        logger.info("validate dialog content");
        String dialogContent = dialog.getModalDialog().getText();
        Assert.assertTrue(dialogContent.contains("Release:"));
        Assert.assertTrue(!dialogContent.contains("${"), "About dialog does not contain any valid release number");

        logger.info("should close the dialog");
        dialog.clickButton(DialogButton.CLOSE);

        ui.cleanup();
    }

    @Test
    public void testCopyrightString() throws Exception {
        UI ui = getUI();
        LeftNavigationPanel nav = ui.getLeftNavigationPanel();
        BottomBar bottomBar = ui.getBottomBar();

        logger.info("should log into APM Server");
        ui.login();

        logger.info("validate bottom bar's copyright string on dashboard");
        nav.goToDashboardPage();
        Assert.assertTrue(bottomBar.isCopyrightStringPresent());

        logger.info("validate bottom bar's copyright string on map");
        nav.goToMapViewPage();
        Assert.assertTrue(bottomBar.isCopyrightStringPresent());

        logger.info("validate bottom bar's copyright string on settings");
        nav.goToPerspectives();
        Assert.assertTrue(bottomBar.isCopyrightStringPresent());

        ui.cleanup();
    }
}
