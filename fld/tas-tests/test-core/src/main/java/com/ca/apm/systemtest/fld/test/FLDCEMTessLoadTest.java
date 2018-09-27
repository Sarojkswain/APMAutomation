package com.ca.apm.systemtest.fld.test;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
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
import java.util.Set;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.flow.ConfigureFLDCEMTessLoadFlowContext;
import com.ca.apm.systemtest.fld.role.CEMTessLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author filja01
 */
public class FLDCEMTessLoadTest extends BaseFldLoadTest {
    private final Logger log = LoggerFactory.getLogger(FLDCEMTessLoadTest.class);
    private volatile boolean stop = false;
    
    public static final String TEST_APP_URL = "tas-czfld-n45:8000";//socat on tim
    public static final String DATABASE = "flddb01c";//"jdbc:postgresql://flddb01c:5432/cemdb";
    public static final String DB_PORT = "5432";
    public static final String DB_USER = "postgres";
    public static final String DB_PASS = "password123";
    public static final int USERS = 300;
    public static final int DEFECTS = 20;
    public static final int LOGINS = 0;
    public static final int DATABASELOGINS = 200; // number of the newest logins to fetch from the database and use
    public static final int SPEED_RATE = 1;

    List<String> oldLogins = new ArrayList<>();
    Map<String,String> userGroupMap = new HashMap<>();
    int nextLogin = 0;
    
    String testAppUrl = TEST_APP_URL;
    String dbUser = DB_USER;
    String databaseUrl = "jdbc:postgresql://"+DATABASE+":"+DB_PORT+"/cemdb";
    String dbPass = DB_PASS;
    Integer users = USERS;
    Integer defects = DEFECTS;
    Integer logins = LOGINS;
    Integer databaseLogins = DATABASELOGINS;
    Integer speedRate = SPEED_RATE;
    
    @Override
    protected String getLoadName() {
        return "CEMTessLoad";
    }

    @Override
    protected void startLoad() {
        startLoad(FLDLoadConstants.CEM_TESS_LOAD_ROLE_ID);
    }

    protected void startLoad(String cemTessLoadRoleId) {
        final ConfigureFLDCEMTessLoadFlowContext ctx = deserializeFlowContextFromRole(
            cemTessLoadRoleId,
            CEMTessLoadRole.CEM_TESS_LOAD_FLOW_CTX_KEY,
            ConfigureFLDCEMTessLoadFlowContext.class);

        if (ctx.getTestAppUrl() != null && !ctx.getTestAppUrl().isEmpty()) {
            testAppUrl = ctx.getTestAppUrl();
        }
        String database;
        if (ctx.getDatabase() == null || ctx.getDatabase().isEmpty()) {
            database = DATABASE;
        } else {
            database = ctx.getDatabase();
        }
        String dbPort;
        if (ctx.getDbPort() == null || ctx.getDbPort().isEmpty()) {
            dbPort = DB_PORT;
        } else {
            dbPort = ctx.getDbPort();
        }
        if (ctx.getDbUser() != null && !ctx.getDbUser().isEmpty()) {
            dbUser = ctx.getDbUser();
        }
        if (ctx.getDbPass() != null && !ctx.getDbPass().isEmpty()) {
            dbPass = ctx.getDbPass();
        }
        if (ctx.getUsers() != null) {
            users = ctx.getUsers();
        }
        if (ctx.getDefects() != null) {
            defects = ctx.getDefects();
        }
        if (ctx.getLogins() != null) {
            logins = ctx.getLogins();
        }
        if (ctx.getDatabaselogins() != null) {
            databaseLogins = ctx.getDatabaselogins();
        }
        if (ctx.getSpeedRate() != null) {
            speedRate = ctx.getSpeedRate();
        }
        
        databaseUrl = "jdbc:postgresql://"+database+":"+dbPort+"/cemdb";
        
        ArrayList<Thread> threads = new ArrayList<>();
        
        final Map<String, Long[]> urlsTemp = getBizTtranUrls();
        
        final Set<String> urlsKeys = urlsTemp.keySet();
            
     // repeat urls creating for more users 
        for (int i = 0; i < USERS; i++) {
            
            if (stop) {
                break;
            }
            
            final int c = i;
            final String threadName = "USER_"+c;
            Thread th = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("Run {} thread", threadName);
                    theloop:
                        while (true) {
                            boolean login = true;
                            String loginName = "";
                            String cookie = null;
                            
                            for (String url : urlsKeys) {
                                
                                Long slowTime = urlsTemp.get(url)[0];
                                Long fastTime = urlsTemp.get(url)[1];
                                // generate a defective transaction (either slow or fast time) from time to time
                                boolean defective = (randomInt(0, 99) < defects);
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
                                
                                // the first biz tran is used for the login
                                if (!login) {
                                   // add group name as well
                                   if (userGroupMap.get(loginName) != null) {
                                       url += ("&userGroupName=" + userGroupMap.get(loginName));
                                   }
                                   connectWith(url,cookie);
                                   
                                } else {
                                    login = false;
                                    synchronized (this) {
                                        // generate a new login from time to time
                                        if (randomInt(0, 99) < LOGINS) {
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
                                        cookie = connectWith(url,null);
                                        log.info("Thread {} logged in", threadName);
                                        
                                        // recycle used logins
                                        if (!loginName.isEmpty()) {
                                            oldLogins.add(loginName);
                                        }
                                    }
                                }
                               
                                if (stop) {
                                    break theloop;
                                }
                                sleep(5000L*speedRate);//send new request every 5s
                            }
                        }
                }
            });
            th.start();
            threads.add(th);
            
            sleep(500L*speedRate);//start threads every 0,5s
        }
    }

    @Override
    protected void stopLoad() {
        stop = true;
        log.info("Wait for test end");
        sleep(60000L); //wait for closing of threads
        log.info("Test end");
    }
    
    private void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Map<String, Long[]> getBizTtranUrls() {
        
        Connection con = null;
        PreparedStatement stm = null;
        ResultSet rst = null;
        String urlBase = "http://"+testAppUrl+"/tesstest/webapp";
        
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
            
            con = DriverManager.getConnection(databaseUrl, dbUser, dbPass);
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
                return null;
            } else {
                
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

                Map<String, Long[]> urlsTemp = new HashMap<String, Long[]>(); 
                
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
                        
                        Long[] time = {slowTime, fastTime};
                        urlsTemp.put(url, time);
                    }
                    
                    rst.close();
                    stm.close();
                }
                
                return urlsTemp;
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
    }
    
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
    
    private String connectWith(String url, String cookie) {
        String cookieResp = null;
        try {
            URLConnection connection = new URL(url).openConnection();
            
            if (cookie != null) {
                //log.info("Send url: {} \n SessionID: {}", url, cookie);
                connection.addRequestProperty("Cookie", "SESSIONID="+cookie);
            }
            else {
                int id = randomInt(1000,99999);
                cookieResp = Integer.toString(id);
                connection.addRequestProperty("Cookie", "SESSIONID="+cookieResp);
            }
            //connection.setRequestProperty("Connection", "close");
            
            connection.getHeaderFields();
            
            //log.info("Send url: {}", url);
            //((HttpURLConnection) connection).disconnect();
            
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Cannot establish url connection. Exception: {0}");
        }
        
        return cookieResp;
    }
}
