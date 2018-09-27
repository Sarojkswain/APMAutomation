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
 * 
 * Author : KETSW01/ KETHIREDDY SWETHA
 * Date : 20/06/2016
 */
package com.ca.apm.tests.testbed;


import org.eclipse.aether.artifact.DefaultArtifact;

import static com.ca.tas.testbed.ITestbedMachine.TEMPLATE_W64;

import com.ca.apm.tests.dbmigration.DBMigrationConstants;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.utility.GenericRole;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.TestBedDefinition;

/**
 * The testbed is for deployment of EM's for DB migration
 *  
 */

@TestBedDefinition
public class DBMigrationWindowsTestbed implements ITestbedFactory {

   public static final String EM_MACHINE_ID = DBMigrationConstants.EM_MACHINE_ID;
   public static final String EM_MACHINE_ORCL_ID =DBMigrationConstants.EM_MACHINE_ORCL_ID;
   
   public static final String EM_POSTGRES_ROLE_ID = DBMigrationConstants.EM_POSTGRES_ROLE_ID;
   public static final String EM_ORACLE_ROLE_ID = DBMigrationConstants.EM_ORACLE_ROLE_ID;
   
   public static final String DBBACKUP_ROLE_ID = DBMigrationConstants.DBBACKUP_ROLE_ID;
   public static final String DBBACKUP_FILE_ARTIFACT_VERSION = DBMigrationConstants.DBBACKUP_FILE_ARTIFACT_VERSION;
   public static final String DBBACKUP_ARTIFACT_ID = DBMigrationConstants.DBBACKUP_ARTIFACT_ID;
   public static final String DBBACKUP_ARTIFACT_GROUP_ID = DBMigrationConstants.DBBACKUP_ARTIFACT_GROUP_ID;
   public static final String DBBACKUP_ARTIFACT_TYPE = DBMigrationConstants.DBBACKUP_ARTIFACT_TYPE;
   public static final String DBBACKUP_FILE_LOC = DBMigrationConstants.DBBACKUP_FILE_LOC_WIN;
   public static final String DBBACKUP_FILE_NAME = DBMigrationConstants.DBBACKUP_FILE_NAME;
   
   public static final String tgtDbHost = DBMigrationConstants.tgtDbHost;
   public static final String tgtDbPort = DBMigrationConstants.tgtDbPort;
   public static final String tgtDbName = DBMigrationConstants.tgtDbName;
   public static final String tgtDbUser = DBMigrationConstants.tgtDbUser;
   public static final String tgtDbPassword = DBMigrationConstants.tgtDbPassword;
   
   public static final String EM_TEMPLATE_ID = TEMPLATE_W64;
   //public static final String EM_TEMPLATE_ID = "w64_8G";
   
    @Override
    public ITestbed create(ITasResolver tasResolver) {                
       
        //EM role with Postgres DB
        EmRole emRole =
            new EmRole.Builder(EM_POSTGRES_ROLE_ID, tasResolver)
                .nostartEM().nostartWV().build();
        
        //EM role with Oracle DB
        EmRole emRoleOrcl =
            new EmRole.Builder(EM_ORACLE_ROLE_ID, tasResolver)
            .useOracle()
            .oracleDbHost(tgtDbHost)
            .oracleDbPort(Integer.parseInt(tgtDbPort))
            .oracleDbSidName(tgtDbName)
            .oracleDbUsername(tgtDbUser)
            .oracleDbPassword(tgtDbPassword)
            .nostartEM().nostartWV().build();
        
        //creates Generic roles to download artifacts
        GenericRole downloadDbBackupRole = new GenericRole.Builder(DBBACKUP_ROLE_ID, tasResolver) 
        .download(new DefaultArtifact(DBBACKUP_ARTIFACT_GROUP_ID, DBBACKUP_ARTIFACT_ID, DBBACKUP_ARTIFACT_TYPE, DBBACKUP_FILE_ARTIFACT_VERSION), DBBACKUP_FILE_LOC+DBBACKUP_FILE_NAME)
        .build();
        
        
        //Configuration of em postgres machine
        TestbedMachine emMachine =
            TestBedUtils
                .createWindowsMachine(EM_MACHINE_ID, EM_TEMPLATE_ID, emRole, downloadDbBackupRole);
        
        //Configuration of em oracle machine
        TestbedMachine emMachine_orcl =
            TestBedUtils
                .createWindowsMachine(EM_MACHINE_ORCL_ID, EM_TEMPLATE_ID, emRoleOrcl);
       
        return Testbed.create(this, emMachine, emMachine_orcl);
    }


}
