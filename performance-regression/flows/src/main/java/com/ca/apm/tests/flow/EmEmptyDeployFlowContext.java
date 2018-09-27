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
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 *
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class EmEmptyDeployFlowContext implements IFlowContext, EnvPropSerializable<EmEmptyDeployFlowContext> {

    private final String installLocation;

    private Map<String, String> properties;

    private final transient EmEmptyDeployFlowContextSerializer envPropSerializer;

    protected EmEmptyDeployFlowContext(EmEmptyDeployFlowContext.Builder builder) {
        this.installLocation = builder.installLocation;
        this.properties = builder.properties;

        this.envPropSerializer = new EmEmptyDeployFlowContextSerializer(this);
    }

    @Override
    public EmEmptyDeployFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public String getInstallLocation() {
        return installLocation;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public static class Builder extends ExtendedBuilderBase<EmEmptyDeployFlowContext.Builder, EmEmptyDeployFlowContext> {

        protected String installLocation;
        protected Map<String, String> properties;

        public Builder() {
            this.installLocation(this.concatPaths(this.getDeployBase(), "intoscope_em"));

        }

        public EmEmptyDeployFlowContext build() {
            EmEmptyDeployFlowContext context = this.getInstance();
            Args.notNull(context.installLocation, "installLocation");

            return context;
        }

        protected EmEmptyDeployFlowContext getInstance() {
            return new EmEmptyDeployFlowContext(this);
        }


        public Builder installLocation(String installLocation) {
            this.installLocation = installLocation;
            return this.builder();
        }

        public Builder properties(Map<String, String> properties) {
            this.properties = properties;
            return this.builder();
        }

        protected EmEmptyDeployFlowContext.Builder builder() {
            return this;
        }
    }
}