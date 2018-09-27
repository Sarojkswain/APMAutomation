package com.ca.apm.tests.test;

import static org.testng.Assert.assertFalse;
import java.io.IOException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import com.ca.apm.tests.testbed.OneEmAbstractTestbed;
import com.ca.apm.tests.testbed.OneEmLinuxTestbed;
import com.ca.apm.tests.testbed.OneEmWindowsTestbed;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.EnvironmentPropertyException;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class ServiceOperationsTests {
    private EnvironmentPropertyContext envProps;

    @BeforeTest
    public void setUp() throws EnvironmentPropertyException, IOException {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();

    }

    /**
     * EM BAT tests #424132<br>
     * Author : Martin Batelka
     * 
     * @author batma08
     * 
     *         <h5>PRECONDITIONS</h5>
     *         <p>
     *         <ul>
     *         <li>TestBed with one running EM.</li>
     *         </ul>
     *         </p>
     *         <h5>TEST ACTIVITY</h5>
     *         <p>
     *         <ol>
     *         <li>Install and Start EM and let it run for some time</li>
     *         <li>Stop EM</li>
     *         </ol>
     *         </p>
     *         <h5>EXPECTED RESULTS</h5>
     *         <p>
     *         <ol>
     *         <li>There should not be any errors in log message like the one below. 5/17/12
     *         06:18:01.028 PM IST [ERROR] [Thread-91] [Manager] Caught an exception while stopping
     *         the EM: com.wily.util.exception.UnexpectedExceptionError: Service
     *         com.wily.introscope.server.enterprise.entity.domain.IDomainEntity not found</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>RISKS MITIGATED</h5>
     *         <p>
     *         <ul>
     *         <li>EM shutdown logic</li>
     *         </ul>
     *         </p>
     */
    @Tas(testBeds = {
            @TestBed(name = OneEmWindowsTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID),
            @TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"BAT", "EM_Basic"})
    public void noErrorsInLogTest() throws Exception {
        final String emInstallDir =
            (String) envProps.getMachineProperties().get(OneEmAbstractTestbed.EM_MACHINE_ID)
                .get(OneEmAbstractTestbed.KeyEmInstallDir);
        EmConfiguration config = new EmConfiguration(emInstallDir, OneEmAbstractTestbed.EM_PORT);
        Thread.sleep(60 * 1000); // we let run EM for some time (1 minute)
        EmBatLocalUtils.stopLocalEm(config);
        assertFalse(EmBatLocalUtils.isKeywordInLog(config.getLogPath(), "[ERROR]"));
    }
}
