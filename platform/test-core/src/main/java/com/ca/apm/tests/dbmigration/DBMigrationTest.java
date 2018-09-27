/*
 * Copyright (c) 2014 CA. All rights reserved.
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
 * 
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Date : 20/07/2016
 */
package com.ca.apm.tests.dbmigration;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.ca.apm.commons.common.TestUtils;
import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.apm.tests.testbed.DBMigrationWindowsTestbed;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


public class DBMigrationTest extends BaseAgentTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBMigrationTest.class);
    private static final long HARVEST_TIMEOUT = 120000;
    public final String srcHost = envProperties.getMachineHostnameByRoleId(DBMigrationConstants.EM_POSTGRES_ROLE_ID);

    public static final String srcDbServiceUser = DBMigrationConstants.srcDbServiceUser;
    public static final String srcDbServicePassword = DBMigrationConstants.srcDbServicePassword;
    public static final String srcDbName = DBMigrationConstants.srcDbName;
    public static final String srcDbUser = DBMigrationConstants.srcDbUser;
    public static final String srcDbPassword = DBMigrationConstants.srcDbPassword;
    public static final String srcDbPort = DBMigrationConstants.srcDbPort;
    public static final String srcDatabaseType = DBMigrationConstants.srcDatabaseType;
    public static final String tgtDatabaseType = DBMigrationConstants.tgtDatabaseType;
    public static final String tgtDbPort = DBMigrationConstants.tgtDbPort;
    public static final String tgtDbName = DBMigrationConstants.tgtDbName;
    public static final String tgtDbUser = DBMigrationConstants.tgtDbUser;
    public static final String tgtDbPassword = DBMigrationConstants.tgtDbPassword;
    public final String tgtHost = DBMigrationConstants.tgtDbHost;
    protected String srcDbInstallDir;
    protected String srcDbBackupFile;
    protected String migrationLogsDir;
    protected String migrationStatusFile;
    protected final String dbRestoreCmd;
    protected final String dbMigrationCmd;

    protected String migrationSucessMsg = "Migration completed successfully";
    protected String emMachineId;
    protected String emPostgresRoleId;
    protected String emOrclMachineId;
    protected String emOracleRoleId;
    TestUtils utility = new TestUtils();
    DBMigrationCommons dbMigCommons = new DBMigrationCommons();

    /**
     * Constructor
     */
    public DBMigrationTest() {
        emMachineId = DBMigrationConstants.EM_MACHINE_ID;
        emPostgresRoleId = DBMigrationConstants.EM_POSTGRES_ROLE_ID;
        emOrclMachineId = DBMigrationConstants.EM_MACHINE_ORCL_ID;
        emOracleRoleId = DBMigrationConstants.EM_ORACLE_ROLE_ID;

        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            srcDbInstallDir = DBMigrationConstants.srcDbInstallDirWin;
            srcDbBackupFile = DBMigrationConstants.srcDbBackupFileWin;
            migrationLogsDir =  DBMigrationConstants.migrationLogsDirWin;
            migrationStatusFile = DBMigrationConstants.migrationStatusFileWin;
            
            dbRestoreCmd =
                DBMigrationConstants.databaseScriptsDirWin + "dbrestore-postgres.bat" + " "
                    + srcHost + " " + srcDbInstallDir + " " + srcDbServiceUser + " "
                    + srcDbServicePassword + " " + srcDbName + " " + srcDbUser + " "
                    + srcDbPassword + " " + srcDbPort + " " + srcDbBackupFile;
            dbMigrationCmd =
                DBMigrationConstants.migrationDirWin + "migration.bat" + " -srcDatabaseName "
                    + srcDbName + " -srcDatabaseType " + srcDatabaseType + " -srcHost " + srcHost
                    + " -srcPassword " + srcDbPassword + " -srcPort " + srcDbPort + " -srcUser "
                    + srcDbUser + " -tgtDatabaseName " + tgtDbName + " -tgtDatabaseType "
                    + tgtDatabaseType + " -tgtHost " + tgtHost + " -tgtPassword " + tgtDbPassword
                    + " -tgtPort " + tgtDbPort + " -tgtUser " + tgtDbUser;
        } else {
            srcDbInstallDir = DBMigrationConstants.srcDbInstallDirLinux;
            srcDbBackupFile = DBMigrationConstants.srcDbBackupFileLinux;
            migrationLogsDir =  DBMigrationConstants.migrationLogsDirLinux;
            migrationStatusFile = DBMigrationConstants.migrationStatusFileLinux;
            
            dbRestoreCmd =
                DBMigrationConstants.databaseScriptsDirLinux + "dbrestore-postgres.sh " + " "
                    + srcHost + " " + srcDbInstallDir + " " + srcDbServiceUser + " "
                    + srcDbServicePassword + " " + srcDbName + " " + srcDbUser + " "
                    + srcDbPassword + " " + srcDbPort + " " + srcDbBackupFile;
            dbMigrationCmd =
                DBMigrationConstants.migrationDirLinux + "migration.sh" + " -srcDatabaseName "
                    + srcDbName + " -srcDatabaseType " + srcDatabaseType + " -srcHost " + srcHost
                    + " -srcPassword " + srcDbPassword + " -srcPort " + srcDbPort + " -srcUser "
                    + srcDbUser + " -tgtDatabaseName " + tgtDbName + " -tgtDatabaseType "
                    + tgtDatabaseType + " -tgtHost " + tgtHost + " -tgtPassword " + tgtDbPassword
                    + " -tgtPort " + tgtDbPort + " -tgtUser " + tgtDbUser;
        }
    }

    @BeforeClass(alwaysRun = true)
    public void DBMigrationInitialize() {
        LOGGER.info("Initializing DB Migration Class");       
    }

    //@Tas(testBeds = @TestBed(name = DBMigrationWindowsTestbed.class, executeOn = DBMigrationWindowsTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "ketsw01")
    @Test
    public void verify_ALM_292983_Migration_Status_Success() {
        int defectCount;
        try {
            runRestoreDbBackup();
            startEM(emPostgresRoleId);
            stopEM(emPostgresRoleId);
            runDBMigration();
            isKeywordInFile(envProperties, emMachineId, DBMigrationConstants.migrationLogFileWin,
                migrationSucessMsg);
            startEM(emOracleRoleId);
            LOGGER.info("EM Start Successful after DB Migration");
            defectCount = dbMigCommons.queryDBForDefectsCount();
            LOGGER.info("Defect count after migration in oracle Database: " + defectCount);
            Assert.assertTrue(defectCount != -1, "DB MIGRATION FAILED - No data in defects table");
            LOGGER.info("Data migrated is validated - Status SUCCESS");

        } catch (Exception e) {
            LOGGER.error("Test Failed: verify_ALM_292983_Migration_Status_Success");
            e.printStackTrace();
        }

    }



    public void runRestoreDbBackup() {
        try {
            LOGGER.info("Running dbrestore command: " + dbRestoreCmd);
            List<String> output1 = utility.runCmd(dbRestoreCmd, DBMigrationConstants.databaseScriptsDirWin);
            LOGGER.info("Dbrestore output: " + output1);
        } catch (Exception e) {
            LOGGER.error("DBRestore Failed");
            e.printStackTrace();
        }
    }

    public void runDBMigration() {
        try {
            // delete migrationstatus.txt and migration log files
            deleteFile(migrationLogsDir, emMachineId);
            deleteFile(migrationStatusFile, emMachineId);
            LOGGER.info("Running dbmigration command: " + dbMigrationCmd);
            List<String> output = utility.runCmd(dbMigrationCmd, DBMigrationConstants.migrationDirWin);
            LOGGER.info("DbMigration output: " + output);
        } catch (Exception e) {
            LOGGER.error("DBMigration Failed");
            e.printStackTrace();
        }
    }

   


}
