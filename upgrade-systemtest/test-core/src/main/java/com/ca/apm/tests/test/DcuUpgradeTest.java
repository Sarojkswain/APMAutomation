package com.ca.apm.tests.test;

import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.tests.flow.CheckEmConfigFlow;
import com.ca.apm.tests.flow.CheckEmConfigFlowContext;
import com.ca.apm.tests.flow.UpgradeEMFlow;
import com.ca.apm.tests.flow.UpgradeEMFlowContext;
import com.ca.apm.tests.role.EmUpgradeRole;
import com.ca.apm.tests.testbed.BigDataUpgradeTestbed;
import com.ca.apm.tests.testbed.UpgradeTestbed;
import com.ca.apm.tests.testbed.dcu.DynamicTestbed;
import com.ca.tas.envproperty.MachineEnvironmentProperties;
import com.ca.tas.role.EmRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.Platform;
import com.ca.tas.type.SizeType;
import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.ca.apm.test.atc.common.Utils.sleep;

/**
 * @author jirji01
 */
public class DcuUpgradeTest extends TasTestNgTest {

    private final Logger log = Logger.getLogger(getClass());

    @Test(groups = {"upgrade_big"})
    @Tas(testBeds = @TestBed(name = DynamicTestbed.class, executeOn = DynamicTestbed.CC_MACHINE_ID), size = SizeType.GIGANTIC, owner = "jirji01")
    public void test() throws Exception {

        log.info("sleep 5 days");
        sleep(1000*60*60*24*5);


        Assert.assertTrue(false);


    }

 }
