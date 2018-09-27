package com.ca.apm.systemtest.fld.flow;

import java.util.Map.Entry;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

/**
 * This flow configures TIMs through their web interfaces.  It sets the network interfaces and configures
 * additional properties
 *
 * @author haiva01
 */
@Flow
public class ConfigureTimFlow extends FlowBase {
    @FlowContext
    ConfigureTimFlowContext flowContext;

    @Override
    public void run() throws Exception {
        try (TimUi timUi = new TimUi(flowContext.hostname, flowContext.port,
            flowContext.username, flowContext.password)) {

            // 0. Visit "http://%s:%s@%s:%d/cgi-bin/ca/apm/tim/index".

            logger.info("About to visit the TIMs configuration page");
            timUi.visitTimWebApp();

            // 1. Find interfaces link and click the link.

            logger.info("About to click on the interfaces link");
            timUi.clickInterfacesLink();

            // 2. Uncheck interfaces.

            logger.info("About to uncheck interfaces");
            for (String eth : flowContext.uncheckIfs) {
                timUi.uncheckIf(eth);
            }

            // 3. Check interfaces.

            logger.info("About to check interfaces");
            for (String eth : flowContext.checkIfs) {
                timUi.checkIf(eth);
            }

            // 4. Click "Set" button.

            logger.info("About to click \"set\"");
            timUi.clickSet();
            
            logger.info("About to set additional properties");
            if (flowContext.additionalProperties != null) {
                for (Entry<String, String> entry: flowContext.additionalProperties.entrySet()) {
                    timUi.configureProperty(entry.getKey(), entry.getValue());
                }
            }
            logger.info("Done!");
        } catch (Exception e) {
            logger.warn("Unable to configure TIM: " + flowContext.hostname, e);
        }
    }
}
