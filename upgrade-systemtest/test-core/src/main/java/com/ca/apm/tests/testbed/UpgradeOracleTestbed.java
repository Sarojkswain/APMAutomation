package com.ca.apm.tests.testbed;

import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.oracle.OracleApmDbRole;
import com.ca.tas.testbed.ITestbed;

import java.util.Arrays;

/**
 * Created by jirji01 on 6/5/2017.
 */
public abstract class UpgradeOracleTestbed extends UpgradeTestbed {

    private EmRole wvRole;

    @Override
    protected EmRole addWvRole(ITestbed testbed, ITasResolver resolver) {

        EmRole.Builder builder = emBuilder(DB_ROLE_ID, resolver)
                .silentInstallChosenFeatures(Arrays.asList("Database", "WebView"))
                .version(version())
                .useAndInstallOracle()
                .wvEmHost(resolver.getHostnameById(MOM_ROLE_ID))
                .wvEmPort(((EmRole)testbed.getRoleById(MOM_ROLE_ID)).getEmPort())
                .wvLaxNlClearJavaOption(WV_LAXNL_JAVA_OPTION)
                .nostartWV()
                .nostartEM()
                .ignoreStopCommandErrors()
                .ignoreUninstallCommandErrors();

        wvRole = builder.build();

        wvRole.before(testbed.getRoleById(MOM_ROLE_ID));
        testbed.getMachineById(DB_MACHINE_ID).addRole(wvRole);
        return wvRole;
    }

    @Override
    protected void setDb(ITasResolver resolver, EmRole.Builder builder, String roleId) {
        builder
                .useOracle()
                .oracleDbHost(resolver.getHostnameById(DB_ROLE_ID))
                .oracleDbPassword(OracleApmDbRole.Builder.DEFAULT_APM_PASSWORD)
                .oracleDbUsername(OracleApmDbRole.Builder.DEFAULT_APM_USER)
                .oracleDbSidName("tradedb");
    }
}
