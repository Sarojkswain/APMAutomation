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

import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class CopyResultsFlowContext implements IFlowContext, EnvPropSerializable<CopyResultsFlowContext> {

    private final String copyResultsDestinationDir;
    private final String copyResultsDestinationFileName;

    private final transient CopyResultsFlowContextSerializer envPropSerializer;

    protected CopyResultsFlowContext(CopyResultsFlowContext.Builder builder) {
        this.copyResultsDestinationDir = builder.copyResultsDestinationDir;
        this.copyResultsDestinationFileName = builder.copyResultsDestinationFileName;

        this.envPropSerializer = new CopyResultsFlowContextSerializer(this);
    }


    public String getCopyResultsDestinationDir() {
        return copyResultsDestinationDir;
    }

    public String getCopyResultsDestinationFileName() {
        return copyResultsDestinationFileName;
    }


    @Override
    public CopyResultsFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<CopyResultsFlowContext.Builder, CopyResultsFlowContext> {

        protected String copyResultsDestinationDir;
        protected String copyResultsDestinationFileName;

        public Builder() {

        }

        public CopyResultsFlowContext build() {
            CopyResultsFlowContext context = this.getInstance();

            return context;
        }

        protected CopyResultsFlowContext getInstance() {
            return new CopyResultsFlowContext(this);
        }


        public CopyResultsFlowContext.Builder copyResultsDestinationDir(String copyResultsDestinationDir) {
            this.copyResultsDestinationDir = copyResultsDestinationDir;
            return this.builder();
        }

        public CopyResultsFlowContext.Builder copyResultsDestinationFileName(String copyResultsDestinationFileName) {
            this.copyResultsDestinationFileName = copyResultsDestinationFileName;
            return this.builder();
        }

        protected CopyResultsFlowContext.Builder builder() {
            return this;
        }
    }

}