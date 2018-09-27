/**
 * 
 */
package com.ca.apm.systemtest.fld.test.loads;

import com.ca.apm.systemtest.fld.flow.ConfigureTessFlow;
import com.ca.apm.systemtest.fld.flow.ConfigureTessFlowContext;
import com.ca.tas.role.tess.ConfigureTessRole;

/**
 * @author keyja01
 *
 */
public class ConfigureTessLoadTest extends BaseFldLoadTest {

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#getLoadName()
     */
    @Override
    protected String getLoadName() {
        // TODO Auto-generated method stub
        return "ConfigureTESS";
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#startLoad()
     */
    @Override
    protected void startLoad() {
        try {
            ConfigureTessFlowContext ctx = deserializeFlowContextFromRole("configureTess", ConfigureTessRole.CONFIGURE_TESS_FLOW_KEY, ConfigureTessFlowContext.class);
            runFlowByMachineId("fldControllerMachine", ConfigureTessFlow.class, ctx);
        } catch (Exception e) {
            // we don't want to fail the test, but at least provide info that something didn't work
            // so it can be manually fixed
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
     * @see com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest#stopLoad()
     */
    @Override
    protected void stopLoad() {
        // noop in this load
    }

}
