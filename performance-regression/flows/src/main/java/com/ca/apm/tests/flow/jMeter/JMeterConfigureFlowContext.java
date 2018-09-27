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
package com.ca.apm.tests.flow.jMeter;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.HashMap;
import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterConfigureFlowContext implements IFlowContext, EnvPropSerializable<JMeterConfigureFlowContext> {

    private final transient JMeterConfigureFlowContextSerializer envPropSerializer;
    private String scriptFilePath;
    private Map<String, String> params;

    protected JMeterConfigureFlowContext(JMeterConfigureFlowContext.Builder builder) {
        this.scriptFilePath = builder.scriptFilePath;
        this.params = builder.params;

        this.envPropSerializer = new JMeterConfigureFlowContextSerializer(this);
    }

    public String getScriptFilePath() {
        return scriptFilePath;
    }

    public void setScriptFilePath(String scriptFilePath) {
        this.scriptFilePath = scriptFilePath;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    @Override
    public JMeterConfigureFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<JMeterConfigureFlowContext.Builder, JMeterConfigureFlowContext> {

        protected String scriptFilePath;
        protected Map<String, String> params;

        public Builder() {

            this.params = new HashMap<>();

        }

        public JMeterConfigureFlowContext build() {
            JMeterConfigureFlowContext context = this.getInstance();
            Args.notNull(context.scriptFilePath, "scriptFilePath");

            return context;
        }

        protected JMeterConfigureFlowContext getInstance() {
            return new JMeterConfigureFlowContext(this);
        }

        public JMeterConfigureFlowContext.Builder scriptFilePath(String scriptFilePath) {
            this.scriptFilePath = scriptFilePath;
            return this.builder();
        }

        public JMeterConfigureFlowContext.Builder params(Map<String, String> params) {
            this.params = params;
            return this.builder();
        }

        protected JMeterConfigureFlowContext.Builder builder() {
            return this;
        }
    }

}