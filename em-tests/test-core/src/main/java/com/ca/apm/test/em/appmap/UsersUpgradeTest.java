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

import java.util.Arrays;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.test.atc.UITest;
import com.ca.apm.test.atc.common.FilterBy;
import com.ca.apm.test.atc.common.UI;
import com.ca.apm.test.atc.common.UniverseSettings;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.em.appmap.SimpleEmTestBed_10_2;
import com.ca.tas.test.em.appmap.SimpleEmTestBed_10_X;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.Platform;
import com.ca.tas.type.SizeType;

/**
 * Tests that the users defined in 10.2 version are successfully migrated after the upgrade.
 * 10.3 users have read access unless they are states as admin users in a configured realm.
 * 
 * @author Martin Surab (surma04)
 *
 */
public class UsersUpgradeTest extends UITest {

    private static final String USER1 = "user1";
    private static final String ADMIN = "admin";
    private static final String U1 = "universe1";
    private static final String U2 = "universe2";
    private static final String U3 = "universe3";
    private static final String U_NO_USERS = "universeNoUsers";

    @BeforeClass
    public void startEm() {
        logger.info("Starting EM and WV before the test starts");
        runSerializedCommandFlowFromRole(SimpleEmTestBed_10_X.EM_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(SimpleEmTestBed_10_X.EM_ROLE_ID, EmRole.ENV_START_WEBVIEW);
    }

    @Test(groups = {"smoke"})
    @Tas(testBeds = @TestBed(name = SimpleEmTestBed_10_2.class, executeOn = "standalone"), owner = "surma04", size = SizeType.SMALL)
    public void createUniversesAndAddUsers() throws Exception {
        UI ui = getUI();
        ui.login();

        ui.getLeftNavigationPanel().goToUniverses();
        UniverseSettings universeSettings = ui.getUniverseSettings();

        logger.info("Creating universes and assigning users to them.");
        createNewUniverse(ui, universeSettings, U1);
        createNewUniverse(ui, universeSettings, U2);
        createNewUniverse(ui, universeSettings, U3);
        createNewUniverse(ui, universeSettings, U_NO_USERS);
        universeSettings.addUsersFor10_2Universe(U1, Arrays.asList(ADMIN, "guest", USER1));
        universeSettings.addUsersFor10_2Universe(U2, Arrays.asList(USER1, "user2"));
        universeSettings.addUsersFor10_2Universe(U3, Arrays.asList("user3"));

        // sample verification
        logger.info("Validating user1 was added to U1 and U2.");
        Assert
            .assertTrue(universeSettings.getUsersCell(U1).getText().contains(USER1.toLowerCase()));
        Assert
            .assertTrue(universeSettings.getUsersCell(U2).getText().contains(USER1.toLowerCase()));

        logger.info("Stopping EM and WV for the upgrade step.");
        killWebview();
        killEm();
    }

    private void killEm() {
        String machineId = envProperties.getMachineIdByRoleId(SimpleEmTestBed_10_X.EM_ROLE_ID);
        Platform platform =
            Platform.fromString(envProperties.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.PLATFORM));
        if (platform == Platform.WINDOWS) {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("taskkill").args(
                    Arrays.asList("/F", "/T", "/IM", EmRole.Builder.INTROSCOPE_EXECUTABLE)).build();
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        } else {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("pkill").args(
                    Arrays.asList("-f", EmRole.LinuxBuilder.INTROSCOPE_EXECUTABLE)).build();
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        }
    }

    private void killWebview() {
        String machineId = envProperties.getMachineIdByRoleId(SimpleEmTestBed_10_X.EM_ROLE_ID);
        Platform platform =
            Platform.fromString(envProperties.getMachinePropertyById(machineId,
                MachineEnvironmentProperties.PLATFORM));
        if (platform == Platform.WINDOWS) {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("taskkill")
                    .args(Arrays.asList("/F", "/T", "/IM", EmRole.Builder.WEBVIEW_EXECUTABLE))
                    .name(SimpleEmTestBed_10_X.EM_ROLE_ID).build();
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        } else {
            RunCommandFlowContext runCommandFlowContext =
                new RunCommandFlowContext.Builder("pkill").args(
                    Arrays.asList("-f", EmRole.LinuxBuilder.WEBVIEW_EXECUTABLE)).build();
            runCommandFlowByMachineId(machineId, runCommandFlowContext);
        }
    }

    /**
     * @param ui
     * @param universeSettings
     * @throws Exception
     */
    private void createNewUniverse(final UI ui, final UniverseSettings universeSettings, String name)
        throws Exception {
        universeSettings.getCreateUniverseButton().click();
        ui.getCanvas().waitForDisplay();
        ui.getCanvas().waitForUpdate();
        ui.getDriver().findElement(FilterBy.UniverseButton.SAVE.getSelector()).click();
        universeSettings.createUniverseName(name);
    }

}
