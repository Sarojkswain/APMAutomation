/*
 * Copyright (c) 2014 CA. All rights reserved.
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

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.apm.automation.action.flow.IFlowContext;

import org.apache.http.util.Args;
import org.jetbrains.annotations.NotNull;

public class StartTomcatFlowContext implements IFlowContext {

    private final String tomcatDir;

    public StartTomcatFlowContext(Builder builder) {
        tomcatDir = builder.tomcatDir;
    }

    public String getTomcatDir() {
        return tomcatDir;
    }

    public static class Builder implements IBuilder<StartTomcatFlowContext> {

        private final String tomcatDir;

        public Builder(@NotNull String tomcatDir) {
            this.tomcatDir = tomcatDir;
        }

        @Override
        public StartTomcatFlowContext build() {
            Args.notNull(tomcatDir, "tomcatDir");

            return new StartTomcatFlowContext(this);
        }

    }

    @Override
    public String toString() {
        return "StartTomcatFlowContext{" + "tomcatDir=" + tomcatDir + '}';
    }
}
