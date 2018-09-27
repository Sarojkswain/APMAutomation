/**
 * 
 */
package com.ca.apm.systemtest.fld.run.loadtest;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

/**
 * @author filja01
 *
 */
public class RunLoadtestImpl implements RunLoadtest {
    
    private static final Logger log = LoggerFactory.getLogger(RunLoadtestImpl.class);
    
    //Delimiter used in CSV file
    private static final String NEW_LINE_SEPARATOR = "\n";


    /**
     * Returns a psuedo-random number between min and max, inclusive.
     * 
     * @param min Minimim value
     * @param max Maximim value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     */
    private static int randomInt(int min, int max) {
        Random rand = new Random();
        
        int randomNum = rand.nextInt((max - min) + 1) + min;
        
        return randomNum;
    }
    
    @Override
    public boolean exportURLsToCSV(Configuration config) {
        
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rst = null;
        List<String> urls = new ArrayList<>();
        String urlBase = "http://"+config.webServer+"/tesstest/webapp";
        
        try{
            Class.forName("org.postgresql.Driver");     
        }

        catch(ClassNotFoundException e)
        {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Postgresql driver not found. Exception: {0}");
        }
        
        try {
            System.out.println("Exporting URLs for loadtest...");
            
            con = DriverManager.getConnection(config.database, config.user, config.password);
            // get the created biz procs
            stm = con.prepareStatement("select ts_id from ts_tran_def_groups where ts_app_id = 100000");
            rst = stm.executeQuery();
            
            List<Integer> bps = new ArrayList<Integer>();
            Integer tmp;
            while (rst.next()) {
                tmp = rst.getInt("ts_id"); 
                bps.add(tmp);
            }
            stm.close();
            rst.close();
            
            if (bps.isEmpty()) {
                // no transaction defined, no csv export
                return false;
            } else {
                List<String> oldLogins = new ArrayList<>();
                Map<String,String> userGroupMap = new HashMap<>();
                int nextLogin = 0;
                // get any previously created logins together with userGroups
                stm = con.prepareStatement("select a.ts_login_name, b.ts_name from ts_users a "
                    + "join ts_user_def_groups b on b.ts_id = a.ts_userdef_group_id "
                    + "where a.ts_login_name like 'created-login-name-%'");
                rst = stm.executeQuery();
                
                while (rst.next()) {
                    String lg = rst.getString("ts_login_name");
                    String group = rst.getString("ts_name");
                    userGroupMap.put(lg, group);
                    
                    oldLogins.add(lg);
                    String[] s = lg.split("created-login-name-");
                    int lgNumber = Integer.valueOf(s[1]);
                    nextLogin = nextLogin > lgNumber? nextLogin:lgNumber;
                }

                stm.close();
                rst.close();

                List<String> urlsTemp = new ArrayList<>(); 
                
                for (Integer bpi : bps) {
                    stm = con.prepareStatement("select a.ts_id,a.ts_name,b.ts_value from ts_transets a "
                        + "join ts_defect_defs b on b.ts_transet_id = a.ts_id "
                        + "where a.ts_trandef_group_id = ? and b.ts_tranunit_id is null "
                        + "and (b.ts_type = 1 or b.ts_type = 2) order by a.ts_id,b.ts_type");
                    stm.setInt(1, bpi);
                    rst = stm.executeQuery();
                    
                    while (rst.next()) {
                        String trans = rst.getString("ts_name");
                        Long slowTime= rst.getLong("ts_value");
                        if (!rst.next())
                            break;
                        Long fastTime= rst.getLong("ts_value");
                        String url = urlBase + "/"+trans+"-tr0-tc0.html";
                        // generate a defective transaction (either slow or fast time) from time to time
                        boolean defective = (randomInt(0, 99) < config.defects);
                        if (! defective) {
                            if (fastTime+1 >= slowTime) {
                                url += ("?wait=" + slowTime);
                            } else {
                                url += ("?wait=" + randomInt(fastTime.intValue()+1, slowTime.intValue()));
                            }
                        } else if (randomInt(0, 99) < 50) {
                            // generate fast time defect
                            url += "?wait=0";
                        } else {
                        // generate slow time defect
                            url += ("?wait=" + (slowTime+100));
                        }
                        urlsTemp.add(url);
                    }
                    
                    rst.close();
                    stm.close();
                }
                
                if (config.users == null || config.users < 1) config.users = 1;
                // repeat urls creating for more users
                for (int i = 0; i < config.users; i++) {
                    boolean login = true;
                    String loginName = "";
                    for (String url : urlsTemp) {
                       // the first biz tran is used for the login
                       if (!login) {
                           // add group name as well
                           if (userGroupMap.get(loginName) != null) {
                               url += ("&userGroupName=" + userGroupMap.get(loginName));
                           }
                           urls.add(url);
                       } else {
                           login = false;
                           
                           // generate a new login from time to time
                           if (randomInt(0, 99) < config.logins) {
                               loginName = "created-login-name-" + nextLogin;
                               nextLogin += 1;
                           } else if (!oldLogins.isEmpty()) {
                               loginName = oldLogins.remove(0);
                           } else {
                               loginName = "";
                           }
                           
                           if (!loginName.isEmpty()) {
                               url += ("&loginName=" + loginName);
                               // add group name as well
                               if (userGroupMap.get(loginName) != null) {
                                   url += ("&userGroupName=" + userGroupMap.get(loginName));
                               }
                           }
                           urls.add(url);
                           
                           // recycle used logins
                           if (!loginName.isEmpty()) {
                               oldLogins.add(loginName);
                           }
                       }
                    }
                }
            }
            
            
        } catch (SQLException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Error when executing sql select. Exception: {0}");
        } finally {
            try {
                if (rst != null) {
                    rst.close();
                }
                if (stm != null) {
                    stm.close();
                }
                if (con != null) {
                    con.close();
                }
    
            } catch (SQLException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Cannot close connection to DB. Exception: {0}");
            }
        }
        
        exportList(urls, config);
        System.out.println("URLs exported into the file "+config.fileDirectory+"/loadtest.csv");
        
        return true;
    }

    private void exportList(List<String> urls, Configuration config) {
        
        FileWriter fileWriter = null;
        CSVPrinter csvFilePrinter = null;
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator(NEW_LINE_SEPARATOR);
        
        try {
            // initialize FileWriter object
            fileWriter = new FileWriter(config.fileDirectory + "/loadtest.csv");
            // initialize CSVPrinter object
            csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
            
            for(String url : urls) {
                csvFilePrinter.printRecord(url);
            }
            
        } catch (Exception e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Error in CsvFileWriter. Exception: {0}");
        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.flush();
                    fileWriter.close();
                }
                if (csvFilePrinter != null) {
                    csvFilePrinter.close();
                }
            } catch (IOException e) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                    "Error while flushing/closing fileWriter/csvPrinter. Exception: {0}");
            }
        }
        
        
    }

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        // Config file
        Configuration config = new Configuration();
        
        config.fileDirectory = "c:/dev";
        
        config.database = "jdbc:postgresql://flddb01c:5432/cemdb";
        config.user = "postgres";
        config.password = "password123";
        config.users = 300;
        config.defects = 20;
        config.logins = 0;
        config.webServer = "${LOCALHOST}:${PORT}";
        config.databaseLogins = 200; // number of the newest logins to fetch from the database and use
       
        config.logs = "c:/dev";

        RunLoadtestImpl loadtest = new RunLoadtestImpl();

        loadtest.exportURLsToCSV(config);
    }
}
