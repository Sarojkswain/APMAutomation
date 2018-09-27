package com.ca.apm.systemtest.fld.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.ca.apm.systemtest.fld.flow.ConfigureAPMJDBCQueyLoadFlowContext;
import com.ca.apm.systemtest.fld.role.APMJDBCQueryLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author filja01
 */
public class APMJDBCQueryLoadTest extends BaseFldLoadTest {
    private final Logger log = LoggerFactory.getLogger(APMJDBCQueryLoadTest.class);
    private Timer timer;
    private String apmServer = "fldmom01c.ca.com";
    
    @Override
    protected String getLoadName() {
        return "APMSQLJDBCQueryLoad";
    }
    
    @Override
    protected void startLoad() {
        final ConfigureAPMJDBCQueyLoadFlowContext ctx = deserializeFlowContextFromRole(
            FLDLoadConstants.APM_JDBC_QUERY_LOAD_ROLE_ID,
            APMJDBCQueryLoadRole.APM_JDBC_QUERY_LOAD_FLOW_CTX_KEY,
            ConfigureAPMJDBCQueyLoadFlowContext.class);

        if (ctx.getApmServer() != null && !ctx.getApmServer().isEmpty()) {
            apmServer = ctx.getApmServer();
        }
        
        timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                ResultSet rs = null;
                Statement stmt = null;
                Connection conn = null;
                
                try {
                 // Load the database driver
                    Class.forName("org.teiid.jdbc.TeiidDriver");
                    
                    conn = DriverManager.getConnection( "jdbc:teiid:apm_base@mm://"+apmServer+":54321", "Admin", "");
                    
                    Date now = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
                    Calendar c = Calendar.getInstance();
                    c.setTime(now);
                    c.add(Calendar.DATE, -1);
                    String time = dateFormat.format(c.getTime());
                    
                    
                    String query1 = "select * from wsmodel.metric_data where agent_name='1ComplexAgent_11' and metric_attribute like 'Average%'"+ 
                                    "and ts between {ts '"+time+" 09:00:00.0'} AND {ts '"+time+" 10:00:00.0'} AND frequency = 15000";
                    
                    String query2 = "select * from wsmodel.metric_data where agent_name='1PortletAgent_10' and metric_attribute like 'Average%'"+ 
                        "and ts between {ts '"+time+" 09:00:00.0'} AND {ts '"+time+" 10:00:00.0'} AND frequency = 15000";
                    
                    stmt = conn.createStatement();
                    stmt.setQueryTimeout(30);
                    rs = stmt.executeQuery(query1);
                    if (rs == null) {
                        log.warn("No data returned - check agents");
                    }
                    int lines = 0;
                    while(rs.next()){
                            lines++;
                    }
                    log.info("Query1 returned {} lines", lines);
                    rs.close();
                    
                    stmt.setQueryTimeout(30);
                    rs = stmt.executeQuery(query2);
                    if (rs == null) {
                        log.warn("No data returned - check agents");
                    }
                    lines = 0;
                    while(rs.next()){
                            lines++;
                    }
                    log.info("Query2 returned {} lines", lines);
                    
                } catch (SQLException | ClassNotFoundException e) {
                    log.error("SQL query error, when processing query! \n{}", e);
                } finally {
                    try {
                        if (rs != null) {rs.close();}
                        if (stmt != null) {stmt.close();}
                        if (conn != null) {conn.close();}
                        log.info("Connection is closed!");
                    } catch (SQLException e) {
                        log.error("Connection error, when closing connection! \n{}", e);
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1800000L);
    }
    
    @Override
    protected void stopLoad() {
        timer.cancel();
        log.info("Test end");
    }
    
    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
