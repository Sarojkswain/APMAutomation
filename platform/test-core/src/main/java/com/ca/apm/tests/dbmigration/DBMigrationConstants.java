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
 * Author : KETSW01
 */
package com.ca.apm.tests.dbmigration;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_CO66;
import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.commons.tests.BaseAgentTest;
import com.ca.tas.builder.TasBuilder;


public class DBMigrationConstants {

    public static final String SW_INSTALL_LOC_WIN = TasBuilder.WIN_SOFTWARE_LOC;
    public static final String SW_INSTALL_LOC_LINUX = TasBuilder.LINUX_SOFTWARE_LOC;

    public static final String EM_MACHINE_ID = "emMachine";
    public static final String EM_MACHINE_ORCL_ID = "emMachine_orcl";

    public static final String EM_POSTGRES_ROLE_ID = "emRolePostgres";
    public static final String EM_ORACLE_ROLE_ID = "emRoleOracle";

    public static final String EM_TEMPLATE_ID_WIN = TEMPLATE_W64;
    public static final String EM_TEMPLATE_ID_LINUX = TEMPLATE_CO66;

    public static final String DBBACKUP_ROLE_ID = "dbbackupRole";
    public static final String DBBACKUP_FILE_ARTIFACT_VERSION = "1.0";
    public static final String DBBACKUP_ARTIFACT_ID = "dbbackup10.3.0.0";
    public static final String DBBACKUP_ARTIFACT_GROUP_ID = "com.ca.apm.coda.testdata.dbmigration.dbbackup";
    public static final String DBBACKUP_ARTIFACT_TYPE = "zip";
    public static final String DBBACKUP_FILE_LOC_WIN = SW_INSTALL_LOC_WIN + "dbbackup"
        + TasBuilder.WIN_SEPARATOR;
    public static final String DBBACKUP_FILE_LOC_LINUX = SW_INSTALL_LOC_LINUX + "dbbackup"
        + TasBuilder.LINUX_SEPARATOR;
    public static final String DBBACKUP_FILE_NAME = "dbbackup_10.3.0.0.dbbackup";
    


    public static final String srcDbInstallDirWin = SW_INSTALL_LOC_WIN + "database";
    public static final String srcDbInstallDirLinux = SW_INSTALL_LOC_LINUX + "database";
    public static final String srcDbServiceUser = "postgres";
    public static final String srcDbServicePassword = "Lister@123";
    public static final String srcDbName = "cemdb";
    public static final String srcDbUser = "admin";
    public static final String srcDbPassword = "Lister@123";
    public static final String srcDbPort = "5432";
    public static final String srcDbBackupFileWin = DBBACKUP_FILE_LOC_WIN + DBBACKUP_FILE_NAME;
    public static final String srcDbBackupFileLinux = DBBACKUP_FILE_LOC_LINUX + DBBACKUP_FILE_NAME;
    public static final String srcDatabaseType = "Postgres";
    public static final String tgtDatabaseType = "Oracle";
    public static final String tgtDbHost = "tas-itc-n64";
    public static final String tgtDbPort = "1521";
    public static final String tgtDbName = "cemdb.ca.com";
    public static final String tgtDbUser = "C##APMUSER7";
    public static final String tgtDbPassword = "quality";
    public static final String tgtDbAdminUser = "system";
    public static final String tgtDbAdminPassword = "oracle";

    public static final String databaseScriptsDirWin = SW_INSTALL_LOC_WIN + "em"
        + TasBuilder.WIN_SEPARATOR + "install" + TasBuilder.WIN_SEPARATOR + "database-scripts"
        + TasBuilder.WIN_SEPARATOR + "windows" + TasBuilder.WIN_SEPARATOR;
    public static final String databaseScriptsDirLinux = SW_INSTALL_LOC_LINUX + "em"
        + TasBuilder.LINUX_SEPARATOR + "install" + TasBuilder.LINUX_SEPARATOR + "database-scripts"
        + TasBuilder.LINUX_SEPARATOR + "unix" + TasBuilder.LINUX_SEPARATOR;
    public static final String oracleDatabaseScriptsDirWin = SW_INSTALL_LOC_WIN + "em"
        + TasBuilder.WIN_SEPARATOR + "install" + TasBuilder.WIN_SEPARATOR + "oracle"
        + TasBuilder.WIN_SEPARATOR + "database-scripts";
    public static final String oracleDatabaseScriptsDirLinux = SW_INSTALL_LOC_LINUX + "em"
        + TasBuilder.LINUX_SEPARATOR + "install" + TasBuilder.LINUX_SEPARATOR + "oracle"
        + TasBuilder.LINUX_SEPARATOR + "database-scripts";
    public static final String migrationDirWin = SW_INSTALL_LOC_WIN + "em"
        + TasBuilder.WIN_SEPARATOR + "install" + TasBuilder.WIN_SEPARATOR + "migration"
        + TasBuilder.WIN_SEPARATOR;
    public static final String migrationDirLinux = SW_INSTALL_LOC_LINUX + "em"
        + TasBuilder.LINUX_SEPARATOR + "install" + TasBuilder.LINUX_SEPARATOR + "migration"
        + TasBuilder.LINUX_SEPARATOR;
    public static final String migrationStatusFileWin = migrationDirWin + "migrationstatus.txt";
    public static final String migrationStatusFileLinux = migrationDirLinux + "migrationstatus.txt";
    public static final String migrationLogsDirWin = migrationDirWin + "logs";
    public static final String migrationLogsDirLinux = migrationDirLinux + "logs";
    public static final String migrationLogFileWin = migrationDirWin + "logs" + TasBuilder.WIN_SEPARATOR + "migration.log";
    public static final String migrationLogFileLinux = migrationDirLinux + "logs" + TasBuilder.LINUX_SEPARATOR + "migration.log";

}
