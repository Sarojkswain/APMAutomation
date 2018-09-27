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

import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class JMeterLogConverterFlowContext implements IFlowContext, EnvPropSerializable<JMeterLogConverterFlowContext> {

    private final String jmeterLogConverterJarPath;
    private final String outputFileName;

    private final transient JMeterLogConverterFlowContextSerializer envPropSerializer;

    protected JMeterLogConverterFlowContext(JMeterLogConverterFlowContext.Builder builder) {
        this.jmeterLogConverterJarPath = builder.jmeterLogConverterJarPath;
        this.outputFileName = builder.outputFileName;

        this.envPropSerializer = new JMeterLogConverterFlowContextSerializer(this);
    }

    public String getJmeterLogConverterJarPath() {
        return jmeterLogConverterJarPath;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    @Override
    public JMeterLogConverterFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<JMeterLogConverterFlowContext.Builder, JMeterLogConverterFlowContext> {

        protected String jmeterLogConverterJarPath;
        protected String outputFileName;

        public Builder() {
        }

        public JMeterLogConverterFlowContext build() {
            JMeterLogConverterFlowContext context = this.getInstance();
            Args.notNull(context.jmeterLogConverterJarPath, "jmeterLogConverterJarPath");
            Args.notNull(context.outputFileName, "outputFileName");

            return context;
        }

        protected JMeterLogConverterFlowContext getInstance() {
            return new JMeterLogConverterFlowContext(this);
        }

        public JMeterLogConverterFlowContext.Builder jmeterLogConverterJarPath(String jmeterLogConverterJarPath) {
            this.jmeterLogConverterJarPath = jmeterLogConverterJarPath;
            return this.builder();
        }

        public JMeterLogConverterFlowContext.Builder outputFileName(String outputFileName) {
            this.outputFileName = outputFileName;
            return this.builder();
        }

        protected JMeterLogConverterFlowContext.Builder builder() {
            return this;
        }
    }

}