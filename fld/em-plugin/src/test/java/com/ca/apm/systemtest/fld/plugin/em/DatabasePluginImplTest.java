package com.ca.apm.systemtest.fld.plugin.em;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * Created by haiva01 on 2.7.2015.
 */
public class DatabasePluginImplTest {
    Logger log = LoggerFactory.getLogger(DatabasePluginImplTest.class);

    static final String TEST_CODE_NAME = "10.0.0-ISCP";
    static final String TEST_EXPECTED_RELEASE = "10.0.0";

    @Test
    public void testTargetReleaseFromCodeName() throws Exception {
        String release = DatabasePluginImpl.targetReleaseFromCodeName(TEST_CODE_NAME);
        log.info("Extracted {} out of {}", release, TEST_CODE_NAME);
        assertEquals(release, TEST_EXPECTED_RELEASE, "Expected " + TEST_EXPECTED_RELEASE);
    }

    @Test
    public void testDatabaseForConfigImport() {
        assertEquals(DatabasePluginImpl.databaseForConfigImport(EmPlugin.Database.oracle), "oracle");
        assertEquals(DatabasePluginImpl.databaseForConfigImport(EmPlugin.Database.postgre), "postgres");
    }

    @Test(expectedExceptions = {RuntimeException.class},
        expectedExceptionsMessageRegExp = "ConfigImport recognizes only 'oracle' and 'postgres' "
            + "database types\\.")
    public void testDatabaseForConfigImportException() {
        DatabasePluginImpl.databaseForConfigImport(EmPlugin.Database.local);
    }
}
