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
package com.ca.apm.tests.testbed.machines;

import com.ca.apm.tests.artifact.MsSqlDbVersion;
import com.ca.apm.tests.artifact.MsSqlTradeDbScriptVersion;
import com.ca.apm.tests.role.MsSqlDbRole;
import com.ca.apm.tests.role.MsSqlTradeDbScriptRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.TestbedMachine;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class DbMsSqlTradeDbMachine {

    public static final String DEFAULT_TEMPLATE = "AgentPerf_DBIIS";

    // must have quadruple backslashes because it's used to replace in Regex
    public static final String DEFAULT_DB_MSSQL_INSTALL_PATH = "C:\\\\sw\\\\wily\\\\mssql"; // has to be in subdir 'wily' to match with DB scripts
    public static final String DEFAULT_DB_MSSQL_SOURCES_INSTALL_PATH = "c:\\\\sw\\\\mssql_sources";

    public static final String MSSQL_DB_ROLE_ID = "_dbRoleId";
    public static final String MSSQL_SCRIPT_ROLE_ID = "_mssqlScriptRoleId";

    protected final String machineId;
    protected final ITasResolver tasResolver;

    protected final String dbAdminPassword;

    protected boolean undeploy;
    protected boolean predeployed;

    public DbMsSqlTradeDbMachine(String machineId, ITasResolver tasResolver, String dbAdminPassword) {
        this.machineId = machineId;
        this.tasResolver = tasResolver;
        this.dbAdminPassword = dbAdminPassword;
    }

    public TestbedMachine undeploy() {
        this.undeploy = true;
        return init();
    }

    public TestbedMachine initPredeployed() {
        this.predeployed = true;
        return init();
    }

    public TestbedMachine init() {
        TestbedMachine machine = new TestbedMachine.Builder(machineId).templateId(DEFAULT_TEMPLATE).bitness(Bitness.b64).build();

        ///////////////////////////////////////////
        // DEPLOY DB
        ///////////////////////////////////////////

        MsSqlDbRole.Builder dbRoleBuilder = new MsSqlDbRole.Builder(machineId + MSSQL_DB_ROLE_ID, tasResolver)
                .version(MsSqlDbVersion.VER_2008)
                .installSourcesPath(DEFAULT_DB_MSSQL_SOURCES_INSTALL_PATH)
                .installPath(DEFAULT_DB_MSSQL_INSTALL_PATH)
                .adminUserPassword(dbAdminPassword)
                .predeployed(predeployed)
                .undeployOnly(undeploy);
        MsSqlDbRole dbRole = dbRoleBuilder.build();
        machine.addRole(dbRole);

        if (undeploy) {
//            FileModifierFlowContext deleteFlow = new FileModifierFlowContext.Builder().delete(dbRole.getInstallSourcesLocation() + "\\" + dbRole.getUnpackDirName()).build();
//            ExecutionRole deleteMssqlScriptRole = new ExecutionRole.Builder(machineId + "_deleteMssqlScriptRoleId").flow(FileModifierFlow.class, deleteFlow)
//                    .build();
        } else {

            ///////////////////////////////////////////
            // CONFIGURE DB
            ///////////////////////////////////////////

            MsSqlTradeDbScriptRole mssqlScriptRole = new MsSqlTradeDbScriptRole
                    .Builder(machineId + MSSQL_SCRIPT_ROLE_ID, tasResolver)
                    .version(MsSqlTradeDbScriptVersion.VER_55)
                    .recreateTablesOnly(false)
                    .dbDeploySourcesLocation(dbRole.getInstallSourcesLocation() + "\\" + dbRole.getUnpackDirName())
                    .predeployed(false) // todo predeployed
                    .build();
            mssqlScriptRole.after(dbRole);
            machine.addRole(mssqlScriptRole);

        }
        return machine;
    }

}
