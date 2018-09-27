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

package com.ca.apm.tests.flow;

import com.ca.tas.builder.BuilderBase;
import com.ca.apm.automation.action.flow.IFlowContext;

/**
 * DeployEmptyFlowContext
 *
 * Template for new flow context objects
 *
 * @author ...
 */
public class DeployEmptyFlowContext implements IFlowContext {

    @SuppressWarnings("unused")
    protected DeployEmptyFlowContext(Builder builder) {}

    public static class LinuxBuilder extends Builder {

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }
    }

    public static class Builder extends BuilderBase<Builder, DeployEmptyFlowContext> {

        @Override
        public DeployEmptyFlowContext build() {

            return getInstance();
        }

        @Override
        protected DeployEmptyFlowContext getInstance() {
            return new DeployEmptyFlowContext(this);
        }

        @Override
        protected Builder builder() {
            return this;
        }
    }
}
