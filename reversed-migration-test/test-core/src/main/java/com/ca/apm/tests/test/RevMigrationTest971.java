package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.RevMigrationTestBed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.tests.annotations.TestBedParameter;
import com.ca.tas.type.SizeType;
import org.testng.annotations.Test;

/**
 * Reversed Migration from 9.7.1
 */
public class RevMigrationTest971 extends RevMigrationTestBase {
    public static final String TEST_GROUP_REVMIG_LOCAL = "revmig_971";

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO66),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_9_7_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration971PostgresCO66() throws Exception {
        test(true);
    }

/*
 * unsupported OS for 9.7.1
    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO7),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_9_7_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration971PostgresCO7() throws Exception {
        test();
    }
*/

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_W64),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_9_7_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration971PostgresW64() throws Exception {
        test(true);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO66),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_9_7_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration971OracleCO66() throws Exception {
        test(true);
    }

/*
 * unsupported OS for 9.7.1
    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO7),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_9_7_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration971OracleCO7() throws Exception {
        test();
    }
*/

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_W64),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_9_7_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration971OracleW64() throws Exception {
        test(true);
    }
}
