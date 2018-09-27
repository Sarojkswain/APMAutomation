/**
 * 
 */
package com.ca.apm.systemtest.fld.run.loadtest;

/**
 * @author filja01
 *
 */
public interface RunLoadtest {
    class Configuration {
        public String fileDirectory;
        
        public String database;
        public String user;
        public String password;
        public Integer users;
        public Integer defects;
        public Integer logins;
        public String webServer;
        public Integer databaseLogins;
       
        public String logs;
    }

    /**
     * Export urls with loadtests to the CSV's file
     * 
     * @param config configuration
     */
    boolean exportURLsToCSV(Configuration config);
}
