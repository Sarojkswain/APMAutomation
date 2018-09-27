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

import org.apache.http.util.Args;

import java.util.HashMap;
import java.util.Map;

/**
 * OracleTradeDbScriptFlowContext
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class OracleTradeDbScriptFlowContext extends OracleScriptFlowContext {

    public static final String DEFAULT_USER = "TRADE";
    public static final String DEFAULT_PASSWORD = "TRADE";
    public static final int DEFAULT_NUM_PROCESSES = 2500;
    public static final int DEFAULT_NUM_SESSIONS = 2755;
    public static final int DEFAULT_NUM_OPEN_CURSORS = 2400;
    public static final int DEFAULT_NUM_TRANSACTIONS = 600;

    private final Map<String, String> sqlFileOptions;

    private final String configDbSqlFileName;
    private final String createUserSqlFileName;

    private final String setupUser;
    private final String setupPassword;

    private final int numProcesses;
    private final int numSessions;
    private final int numOpenCursors;
    private final int numTransactions;


    protected OracleTradeDbScriptFlowContext(OracleTradeDbScriptFlowContext.Builder builder) {
        super(builder);
        this.sqlFileOptions = builder.sqlFileOptions;
        this.configDbSqlFileName = builder.configDbSqlFileName;
        this.createUserSqlFileName = builder.createUserSqlFileName;
        this.setupUser = builder.setupUser;
        this.setupPassword = builder.setupPassword;
        this.numProcesses = builder.numProcesses;
        this.numSessions = builder.numSessions;
        this.numOpenCursors = builder.numOpenCursors;
        this.numTransactions = builder.numTransactions;
    }

    public Map<String, String> getSqlFileOptions() {
        return sqlFileOptions;
    }

    public String getConfigDbSqlFileName() {
        return configDbSqlFileName;
    }

    public String getCreateUserSqlFileName() {
        return createUserSqlFileName;
    }

    public String getSetupUser() {
        return setupUser;
    }

    public String getSetupPassword() {
        return setupPassword;
    }

    public int getNumProcesses() {
        return numProcesses;
    }

    public int getNumSessions() {
        return numSessions;
    }

    public int getNumOpenCursors() {
        return numOpenCursors;
    }

    public int getNumTransactions() {
        return numTransactions;
    }

    public static class Builder extends OracleScriptFlowContext.Builder {

        private final Map<String, String> sqlFileOptions = new HashMap<>();

        protected String createUserSqlFileName;
        protected String configDbSqlFileName;

        protected String setupUser;
        protected String setupPassword;

        protected int numProcesses;
        protected int numSessions;
        protected int numOpenCursors;
        protected int numTransactions;

        public Builder() {
            super();
            this.setupUser(DEFAULT_USER);
            this.setupPassword(DEFAULT_PASSWORD);
            this.numOpenCursors(DEFAULT_NUM_OPEN_CURSORS);
            this.numProcesses(DEFAULT_NUM_PROCESSES);
            this.numSessions(DEFAULT_NUM_SESSIONS);
            this.numTransactions(DEFAULT_NUM_TRANSACTIONS);
        }

        public OracleTradeDbScriptFlowContext build() {
            super.build();
            this.initSqlFileData();
            OracleTradeDbScriptFlowContext context = this.getInstance();
            Args.notNull(context.configDbSqlFileName, "configDbSqlFileName");
            Args.notNull(context.createUserSqlFileName, "createUserSqlFileName");
            Args.notNull(context.setupUser, "setupUser");
            Args.notNull(context.setupPassword, "setupPassword");
            Args.notNull(context.numProcesses, "numProcesses");
            Args.notNull(context.numSessions, "numSessions");
            Args.notNull(context.numOpenCursors, "numOpenCursors");
            Args.notNull(context.numTransactions, "numTransactions");

            return context;
        }

        protected void initSqlFileData() {
            this.sqlFileOptions.put("NUM_PROCESSES", String.valueOf(this.numProcesses));
            this.sqlFileOptions.put("NUM_SESSIONS", String.valueOf(this.numSessions));
            this.sqlFileOptions.put("NUM_OPEN_CURSORS", String.valueOf(this.numOpenCursors));
            this.sqlFileOptions.put("NUM_TRANSACTIONS", String.valueOf(this.numTransactions));
            this.sqlFileOptions.put("DB_USER", this.setupUser);
            this.sqlFileOptions.put("DB_USER_PW", this.setupPassword);
        }

        protected OracleTradeDbScriptFlowContext getInstance() {
            return new OracleTradeDbScriptFlowContext(this);
        }

        public OracleTradeDbScriptFlowContext.Builder createUserSqlFileName(String createUserSqlFileName) {
            this.createUserSqlFileName = createUserSqlFileName;
            return this.builder();
        }

        public OracleTradeDbScriptFlowContext.Builder configDbSqlFileName(String configDbSqlFileName) {
            this.configDbSqlFileName = configDbSqlFileName;
            return this.builder();
        }

        public OracleTradeDbScriptFlowContext.Builder setupUser(String setupUser) {
            this.setupUser = setupUser;
            return this.builder();
        }

        public OracleTradeDbScriptFlowContext.Builder setupPassword(String setupPassword) {
            this.setupPassword = setupPassword;
            return this.builder();
        }

        public OracleTradeDbScriptFlowContext.Builder numProcesses(int numProcesses) {
            this.numProcesses = numProcesses;
            return this.builder();
        }

        public OracleTradeDbScriptFlowContext.Builder numSessions(int numSessions) {
            this.numSessions = numSessions;
            return this.builder();
        }

        public OracleTradeDbScriptFlowContext.Builder numOpenCursors(int numOpenCursors) {
            this.numOpenCursors = numOpenCursors;
            return this.builder();
        }

        public OracleTradeDbScriptFlowContext.Builder numTransactions(int numTransactions) {
            this.numTransactions = numTransactions;
            return this.builder();
        }

        protected OracleTradeDbScriptFlowContext.Builder builder() {
            return this;
        }
    }
}