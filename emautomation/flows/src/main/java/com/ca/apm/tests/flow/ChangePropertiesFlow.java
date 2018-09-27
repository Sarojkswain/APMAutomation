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
package com.ca.apm.tests.flow;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.tests.utils.configutils.PropertiesUtility;

/**
 * Class for changing properties in remote machines.
 * Configuration stored in context
 * 
 * @author sobar03
 *
 */
public class ChangePropertiesFlow implements IAutomationFlow {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangePropertiesFlow.class);

    /**
     * Context containing map of properties and path to properties.
     */
    @FlowContext
    private ChangePropertiesFlowContext context;

    /*
     * Uses context and properties utility to make properties change.
     */

    public ChangePropertiesFlow(ChangePropertiesFlowContext context) {
        this.context = context;
    }

    /**
     * Empty constructor
     */
    public ChangePropertiesFlow() {}

    /*
     * (non-Javadoc)
     * Run method
     * 
     * @see com.ca.apm.automation.action.core.IAutomationFlow#run()
     */
    public void run() throws Exception {
        LOGGER.info("Running change properties flow. Properties path : "
            + context.getPropertiesPath());
        PropertiesUtility.saveProperties(context.getPropertiesPath(),
            context.getDesiredProperties(), context.isAddNotExisting());
        LOGGER.info("Change properties flow finished fine.");
    }

}
