package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.RevMigrationTestBed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.tests.annotations.TestBedParameter;
import com.ca.tas.type.SizeType;
import org.testng.annotations.Test;

/**
 * Reversed Migration from 10.5.1
 */
public class RevMigrationTest1051 extends RevMigrationTestBase {
    public static final String TEST_GROUP_REVMIG_LOCAL = "revmig_1051";

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO66),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration1051PostgresCO66() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO7),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration1051PostgresCO7() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_W64),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration1051PostgresW64() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO66),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration1051OracleCO66() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO7),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration1051OracleCO7() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_W64),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5_1),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration1051OracleW64() throws Exception {
        test(false);
    }
}
