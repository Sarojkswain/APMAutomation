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

import static com.ca.apm.testbed.atc.TeamCenterRegressionTestBed.MACHINE_ID_GATEWAY;
import static com.ca.apm.testbed.atc.TeamCenterRegressionTestBed.MACHINE_ID_TRADE_SERVICE_APP;
import static java.lang.String.format;

import java.util.Arrays;

import org.apache.commons.io.FilenameUtils;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.test.atc.common.WebViewTestNgTest;
import com.ca.apm.testbed.atc.TeamCenterRegressionTestBed;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.test.em.appmap.PhantomJSTest;
import com.ca.tas.test.utils.LocalStorage;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class SetupRegressionTest extends WebViewTestNgTest {

    private boolean isLocalRun = false;

    private final LocalStorage localStorage;
    
    public SetupRegressionTest() {
        localStorage =
                new LocalStorage(FilenameUtils.separatorsToSystem("storage/"
                    + getClass().getSimpleName()));
    }

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() throws Exception {
        isLocalRun = Boolean.parseBoolean(envProperties.getTestbedPropertyById("localRun"));

        // prepare the application environment for test
        if (!isLocalRun) {
            localStorage.fetchResource(envProperties.getRolePropertiesById("trade-service")
                .getProperty("tbexports.default.url"));
            PhantomJSTest.execute("importBT_TradingService.js", envProperties,
                localStorage.getFileLocations("*bt-exports*.zip"));

            // do initial visit of all TradeService pages to get the whole application map into ATC

            String tradeServiceHostname =
                envProperties.getMachinePropertyById(MACHINE_ID_TRADE_SERVICE_APP,
                    MachineEnvironmentProperties.HOSTNAME);
            RunCommandFlowContext initialTradeServiceLoadCxt =
                new RunCommandFlowContext.Builder("wget").args(
                    Arrays.asList("-r", "--delete-after", "-nd",
                        format("http://%s:7080/TradeService/", tradeServiceHostname))).build();
            runCommandFlowByMachineId(MACHINE_ID_GATEWAY, initialTradeServiceLoadCxt);
        }

        checkWebview("introscope");
    }
    
    @Test(groups = "dependency")
    @Tas(testBeds = @TestBed(name = TeamCenterRegressionTestBed.class, executeOn = "endUserMachine"), owner = "valja03", size = SizeType.SMALL)
    public void testDummy() throws Exception {
        // Nothing
    }
    
}
