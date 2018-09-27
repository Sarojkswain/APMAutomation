/**
 * 
 */
package com.ca.apm.systemtest.fld.smoke.tests;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.flow.ConfigureTessFlow;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext;
import com.ca.apm.systemtest.fld.smoke.testbed.agc.ConfigureTessSmokeTestbed;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.tess.ConfigureTessRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

/**
 * Not strictly a test, but used to configure CEM after the cluster is completely deployed
 * @author keyja01
 *
 */
public class ConfigureTessLoadTest extends TasTestNgTest {

    @Test
    @Tas(owner="keyja01", size=SizeType.BIG, testBeds=@TestBed(executeOn="fldControllerMachine", name=ConfigureTessSmokeTestbed.class))
    public void configureTess() {
        runSerializedCommandFlowFromRole(FLDConstants.EM_MOM_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(FLDConstants.EM_COLL01_ROLE_ID, EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(FLDConstants.EM_COLL02_ROLE_ID, EmRole.ENV_START_EM);
        ConfigureTessFlowContext ctx = deserializeFlowContextFromRole("configureTess", ConfigureTessRole.CONFIGURE_TESS_FLOW_KEY, ConfigureTessFlowContext.class);
        runFlowByMachineId("fldControllerMachine", ConfigureTessFlow.class, ctx);
    }
}
