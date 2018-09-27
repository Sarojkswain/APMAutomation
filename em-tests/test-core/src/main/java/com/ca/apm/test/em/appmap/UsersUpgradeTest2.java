/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.test.em.appmap;

import java.util.List;

import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UniverseSettings;
import com.ca.apm.test.atc.common.UI.Permission;
import com.ca.apm.test.atc.common.element.PageElement;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.em.appmap.SimpleEmTestBed_10_X;
import com.ca.tas.test.em.appmap.SimpleEmUpgradeTestBed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * @author Martin Surab (surma04)
 *
 */
public class UsersUpgradeTest2 extends UITest {

    private static final String ADMIN = "admin";
    private static final String U1 = "universe1";
    private static final String U2 = "universe2";
    private static final String U3 = "universe3";
    private static final String U_NO_USERS = "universeNoUsers";

    @Test(groups = {"smoke"})
    @Tas(testBeds = @TestBed(name = SimpleEmUpgradeTestBed.class, executeOn = "standalone"), owner = "surma04", size = SizeType.MEDIUM)
    public void validateUsersExistAfterUpgrade() throws Exception {
        UI ui = getUI();
        ui.login();

        ui.getLeftNavigationPanel().goToUniverses();
        UniverseSettings uSett = ui.getUniverseSettings();

        uSett.openUsersDialog(U1);

        List<PageElement> dialogUserRows = uSett.getDialogUserRows();
        Assert.assertTrue(dialogUserRows.size() == 3);
        for (PageElement userRow : dialogUserRows) {
            String user = userRow.findElement(By.tagName("span")).getText();
            String access = uSett.getUserAccessToggle(userRow).getText();
            if (user.equalsIgnoreCase(ADMIN)) {
                logger.info("Admin found - checking it has manage access.");
                Assert.assertEquals(access, Permission.manage.toString());
                // restrict the admin access for the restart test
                logger.info("Changing admin access to read.");
                uSett.getUserAccessToggle(userRow).click();
            } else {
                Assert.assertEquals(access, Permission.read.toString());
            }
        }

        uSett.saveUsersDialog();

        uSett.openUsersDialog(U2);
        Assert.assertEquals(2, uSett.getDialogUserRows().size());
        uSett.closeUsersDialog();

        uSett.openUsersDialog(U3);
        Assert.assertEquals(1, uSett.getDialogUserRows().size());
        uSett.closeUsersDialog();

        uSett.openUsersDialog(U_NO_USERS);
        Assert.assertEquals(0, uSett.getDialogUserRows().size());
        uSett.closeUsersDialog();

        logger.info("Stopping EM and WV for the user migration check.");
        runSerializedCommandFlowFromRole(SimpleEmTestBed_10_X.EM_ROLE_ID, EmRole.ENV_STOP_WEBVIEW);
        runSerializedCommandFlowFromRole(SimpleEmTestBed_10_X.EM_ROLE_ID, EmRole.ENV_STOP_EM);

        logger.info("Starting EM and WV to verify migration does not happen twice");
        runSerializedCommandFlowFromRole(SimpleEmTestBed_10_X.EM_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(SimpleEmTestBed_10_X.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);
    }

    @Test(groups = {"smoke"})
    @Tas(testBeds = @TestBed(name = SimpleEmUpgradeTestBed.class, executeOn = "standalone"), owner = "surma04", size = SizeType.SMALL)
    public void validateAdminAccessRemainsChanged() throws Exception {

        UI ui = getUI();
        ui.login();
        ui.getLeftNavigationPanel().goToUniverses();
        UniverseSettings uSett = ui.getUniverseSettings();

        // check U1 again - all should have read (i.e. Admin did not get re-set to the 10.2 value)
        uSett.openUsersDialog(U1);

        List<PageElement> dialogUserRows = uSett.getDialogUserRows();
        Assert.assertTrue(dialogUserRows.size() == 3);
        for (PageElement userRow : dialogUserRows) {
            String access = uSett.getUserAccessToggle(userRow).getText();
            Assert.assertEquals(access, Permission.read.toString());
        }
    }
}
