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

import com.ca.apm.tests.artifact.KonakartTradeDbScriptVersion;
import com.ca.apm.tests.flow.oracleDb.OracleTradeDbScriptFlowContext;
import com.ca.apm.tests.role.KonakartTradeDbScriptRole;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.IRole;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
public class DbOracleKonakartTradeDbMachine extends DbOracleTradeDbMachine {

    public static final String KONAKART_SCRIPT_ROLE_ID = "_konakartScriptRoleId";

    public DbOracleKonakartTradeDbMachine(String machineId, ITasResolver tasResolver, String template) {
        super(machineId, tasResolver, template);
    }

    @Override
    protected IRole createTables() {

        KonakartTradeDbScriptRole konakartScriptRole = new KonakartTradeDbScriptRole
                .Builder(machineId + KONAKART_SCRIPT_ROLE_ID, tasResolver)
                .version(KonakartTradeDbScriptVersion.VER_5_2_0_0)
                .runAsUser(OracleTradeDbScriptFlowContext.DEFAULT_USER)
                .runAsPassword(OracleTradeDbScriptFlowContext.DEFAULT_PASSWORD).runAsSysdba(false)
                .plsqlExecutableLocation(DEFAULT_DB_ORACLE_HOME_PATH + "\\BIN\\sqlplus.exe")
                .predeployed(predeployed)
                .build();

        return konakartScriptRole;

    }

}
