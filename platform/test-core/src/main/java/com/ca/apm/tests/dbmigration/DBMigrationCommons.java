package com.ca.apm.tests.dbmigration;

/**
 * Test class for APM OracleDB user/schema creation test cases
 * 
 * @author: ketsw01
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DBMigrationCommons{

    private String dbAdmin;
    private String dbAdminPwd;
    private String dbHost;
    private String dbPort;
    private String dbUser;
    private String dbUserPwd;
    private String dbName;

    Connection con;

    public DBMigrationCommons() {
        dbAdmin = DBMigrationConstants.tgtDbAdminUser;
        dbAdminPwd = DBMigrationConstants.tgtDbAdminPassword;
        dbHost = DBMigrationConstants.tgtDbHost;
        dbPort = DBMigrationConstants.tgtDbPort;
        dbUser = DBMigrationConstants.tgtDbUser;
        dbUserPwd = DBMigrationConstants.tgtDbPassword;
        dbName = DBMigrationConstants.tgtDbName;
    }

    public void initializeDB() {
        System.out.println("**********Initializing connection to Oracle Database*******");
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            String dbURL = "jdbc:oracle:thin:@" + dbHost + ":" + dbPort + "/" + dbName;
            con = DriverManager.getConnection(dbURL, dbAdmin, dbAdminPwd);
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("**********Connection to Oracle Database successful*******");
    }



    public void createAPMOracleUser() {
        try {
            initializeDB();
            createAPMUser(dbUser, dbUserPwd);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }

    }

    public int queryDBForDefectsCount() {
        int result = -1;
        try {
            initializeDB();
            Statement st = con.createStatement();
            ResultSet rs =
                st.executeQuery("SELECT COUNT(*) AS total FROM " + dbUser + ".TS_DEFECTS");
            while (rs.next()) {
                result = rs.getInt("total");
                System.out.println("The count is " + result);
                return result;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cleanup();
        }
        return result;
    }

    private void createAPMUser(String dbUser, String dbUserPwd) {
        executeDDLQuery("DROP USER " + dbUser + " cascade");
        executeDDLQuery("CREATE USER " + dbUser + " IDENTIFIED BY " + dbUserPwd);
        executeDDLQuery("GRANT \"RESOURCE\",\"CONNECT\" TO " + dbUser);
        executeDDLQuery("GRANT CREATE TRIGGER, CREATE SEQUENCE, CREATE TYPE, CREATE PROCEDURE, CREATE TABLE, CREATE SESSION, CREATE VIEW , UNLIMITED TABLESPACE, Analyze Any TO " + dbUser);
        
        System.out.println("APM user " + dbUser + " created successfully");
    }

    public boolean executeDDLQuery(String sqlStatement) {
        boolean result = false;
        try {
            Statement st = con.createStatement();
            result = st.execute(sqlStatement);
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }


    public void cleanup() {
        try {
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
