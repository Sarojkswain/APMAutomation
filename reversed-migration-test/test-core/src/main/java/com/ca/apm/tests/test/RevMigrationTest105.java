package com.ca.apm.tests.test;

import com.ca.apm.tests.testbed.RevMigrationTestBed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.tests.annotations.TestBedParameter;
import com.ca.tas.type.SizeType;
import com.ca.tas.type.SnapshotMode;
import com.ca.tas.type.SnapshotPolicy;
import org.testng.annotations.Test;

/**
 * Reversed Migration from 10.5
 */
public class RevMigrationTest105 extends RevMigrationTestBase {
    public static final String TEST_GROUP_REVMIG_LOCAL = "revmig_105";

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO66),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER, snapshotPolicy = SnapshotPolicy.ON_FAILURE, snapshot = SnapshotMode.LIVE)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration105PostgresCO66() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO7),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER, snapshotPolicy = SnapshotPolicy.ON_FAILURE, snapshot = SnapshotMode.LIVE)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration105PostgresCO7() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_W64),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_POSTGRES),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER, snapshotPolicy = SnapshotPolicy.ON_FAILURE, snapshot = SnapshotMode.LIVE)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration105PostgresW64() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO66),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER, snapshotPolicy = SnapshotPolicy.ON_FAILURE, snapshot = SnapshotMode.LIVE)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration105OracleCO66() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_CO7),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER, snapshotPolicy = SnapshotPolicy.ON_FAILURE, snapshot = SnapshotMode.LIVE)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration105OracleCO7() throws Exception {
        test(false);
    }

    @Tas(testBeds = {@TestBed(name = RevMigrationTestBed.class, executeOn = RevMigrationTestBed.STANDALONE_MACHINE,
            params = {
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_TEMPLATE_ID, value = ITestbedMachine.TEMPLATE_W64),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_APM_VERSION, value = RevMigrationTestBed.APM_10_5),
                    @TestBedParameter(name = RevMigrationTestBed.PARAM_DB_TYPE, value = RevMigrationTestBed.DB_TYPE_ORACLE),
            })
    }, size = SizeType.BIG, owner = TEST_OWNER, snapshotPolicy = SnapshotPolicy.ON_FAILURE, snapshot = SnapshotMode.LIVE)
    @Test(groups = { TEST_GROUP_REVMIG, TEST_GROUP_REVMIG_LOCAL})
    public void testReversedMigration105OracleW64() throws Exception {
        test(false);
    }
}
