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
package com.ca.apm.tests.flow.oracleDb;

import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * Flow Context for creating a TradeDb table structure in Oracle DB
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class WebAppTradeDbScriptFlowContext extends OracleScriptFlowContext implements EnvPropSerializable<WebAppTradeDbScriptFlowContext> {

    private final String sqlFileName;

    private final transient WebAppTradeDbScriptFlowContextSerializer envPropSerializer;


    protected WebAppTradeDbScriptFlowContext(WebAppTradeDbScriptFlowContext.Builder builder) {
        super(builder);
        this.sqlFileName = builder.sqlFileName;

        this.envPropSerializer = new WebAppTradeDbScriptFlowContextSerializer(this);
    }

    public String getSqlFileName() {
        return sqlFileName;
    }

    @Override
    public WebAppTradeDbScriptFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends OracleScriptFlowContext.Builder {

        protected String sqlFileName;

        public WebAppTradeDbScriptFlowContext build() {
            super.build();
            WebAppTradeDbScriptFlowContext context = this.getInstance();
            Args.notNull(context.sqlFileName, "sqlFileName");
            return context;
        }

        protected WebAppTradeDbScriptFlowContext getInstance() {
            return new WebAppTradeDbScriptFlowContext(this);
        }

        public WebAppTradeDbScriptFlowContext.Builder sqlFileName(String sqlFileName) {
            this.sqlFileName = sqlFileName;
            return this.builder();
        }

        protected WebAppTradeDbScriptFlowContext.Builder builder() {
            return this;
        }
    }
}