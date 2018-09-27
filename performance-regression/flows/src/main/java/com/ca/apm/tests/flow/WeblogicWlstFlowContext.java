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

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import org.apache.http.util.Args;

/**
 * Flow Context for installing StockTrader WebApp into Weblogic
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class WeblogicWlstFlowContext implements IFlowContext {

    public static final String WLST_PATH_REL = "\\common\\bin\\wlst.cmd";
    public static final String WEBLOGIC_JAR_PATH_REL = "\\server\\lib\\weblogic.jar";
    public static final String JSF_LIB_WAR_PATH_REL = "\\common\\deployable-libraries\\jsf-1.2.war";
    public static final String JSTL_LIB_WAR_PATH_REL = "\\common\\deployable-libraries\\jstl-1.2.war";

    private final String weblogicInstallPath;

    protected WeblogicWlstFlowContext(WeblogicWlstFlowContext.Builder builder) {
        this.weblogicInstallPath = builder.weblogicInstallPath;
    }

    public String getWeblogicInstallPath() {
        return weblogicInstallPath;
    }

    public static class Builder extends ExtendedBuilderBase<WeblogicWlstFlowContext.Builder, WeblogicWlstFlowContext> {
        protected String weblogicInstallPath;

        public Builder() {

        }

        public WeblogicWlstFlowContext build() {
            WeblogicWlstFlowContext context = this.getInstance();
            Args.notNull(context.weblogicInstallPath, "weblogicInstallPath");
            return context;
        }

        protected WeblogicWlstFlowContext getInstance() {
            return new WeblogicWlstFlowContext(this);
        }

        public WeblogicWlstFlowContext.Builder weblogicInstallPath(String weblogicInstallPath) {
            this.weblogicInstallPath = weblogicInstallPath;
            return this.builder();
        }

        protected WeblogicWlstFlowContext.Builder builder() {
            return this;
        }
    }
}