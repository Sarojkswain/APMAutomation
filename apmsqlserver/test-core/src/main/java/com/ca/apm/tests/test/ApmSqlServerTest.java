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
 * Date : 13/05/2016
 */

package com.ca.apm.tests.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.GregorianCalendar;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;

import com.ca.apm.tests.utils.ZipUtils;
import com.ca.apm.tests.utils.IntroscopeDBAccessUtil;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlow;
import com.ca.apm.automation.action.flow.utility.TimeSyncFlowContext;
import com.ca.apm.automation.action.test.ClwUtils;
import com.ca.apm.automation.action.test.EmUtils;
import com.ca.apm.tests.utils.AssertTests;
import com.ca.apm.tests.utils.EmConfiguration;
import com.ca.apm.tests.utils.osutils.OsLocalUtils;
import com.ca.apm.tests.common.CLWCommons;
import com.ca.apm.tests.testbed.ApmSqlServerWindowsClusterTestbed;
import com.ca.tas.builder.TasBuilder;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.webapp.TomcatRole;
import com.ca.tas.role.webapp.JbossRole;
import com.ca.tas.test.TasTestNgTest;


public class ApmSqlServerTest extends TasTestNgTest {

    AssertTests assertTest = new AssertTests();
    CLWCommons clwCommon = new CLWCommons();

    
    private static final String emInstallDir = TasBuilder.WIN_SOFTWARE_LOC+ "em";
    private static final String apmSqlClientDir=emInstallDir+"/APMSqlServer"+"/client";
    private static final String teiidDriverFile=apmSqlClientDir+"/teiid-9.0.1-jdbc.jar";
    protected String momMachineId;
    protected String agentMachineId;

    protected String collector1MachineId;
    protected String collector2MachineId;
    protected String collector1RoleId;
    protected String collector2RoleId;

    protected String momRoleId;
    protected String tomcatRoleId;
    protected String jbossRoleId;
    protected String qaAppTomcatRoleId;
    protected String qaAppjbossRoleId;
    protected String tomcatAgentRoleId;
    protected String jbossAgentRoleId;

    protected String host;
    protected String agentHost;
    protected int port;
    protected String emLibDir;
    private static final String user = "Admin";
    private static final String password = "quality";

    protected Integer metricValue;
    private String username = "Admin";
    private String urlPrefix = "jdbc:teiid:apm_base@mm://";
    private String url;
    private int sleep = 15000;
 
    /**
     * Agent Expressions
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ApmSqlServerTest.class);
    private EnvironmentPropertyContext envProps;
    public int resultCount;
    public String stringData;
    public int dataValue;   
       /***
     * Constructor for Initialization
     */

    public ApmSqlServerTest() {

        momMachineId = ApmSqlServerWindowsClusterTestbed.MOM_MACHINE_ID;

        momRoleId = ApmSqlServerWindowsClusterTestbed.MOM_ROLE_ID;

        collector1MachineId = ApmSqlServerWindowsClusterTestbed.COLLECTOR1_MACHINE_ID;
        collector2MachineId = ApmSqlServerWindowsClusterTestbed.COLLECTOR2_MACHINE_ID;

        collector1RoleId = ApmSqlServerWindowsClusterTestbed.COLLECTOR1_ROLE_ID;
        collector2RoleId = ApmSqlServerWindowsClusterTestbed.COLLECTOR2_ROLE_ID;

        tomcatRoleId = ApmSqlServerWindowsClusterTestbed.TOMCAT_ROLE_ID;
        jbossRoleId = ApmSqlServerWindowsClusterTestbed.JBOSS_ROLE_ID;

        qaAppTomcatRoleId = ApmSqlServerWindowsClusterTestbed.QA_APP_TOMCAT_ROLE_ID;
        qaAppjbossRoleId = ApmSqlServerWindowsClusterTestbed.QA_APP_JBOSS_ROLE_ID;

        tomcatAgentRoleId = ApmSqlServerWindowsClusterTestbed.TOMCAT_AGENT_ROLE_ID;
        jbossAgentRoleId = ApmSqlServerWindowsClusterTestbed.JBOSS_AGENT_ROLE_ID;

        host =
            envProperties
                .getMachineHostnameByRoleId(ApmSqlServerWindowsClusterTestbed.MOM_ROLE_ID);
        port =
            Integer.parseInt(envProperties.getRolePropertyById(momRoleId,
                DeployEMFlowContext.ENV_EM_PORT));
        emLibDir = envProperties.getRolePropertyById(momRoleId, DeployEMFlowContext.ENV_EM_LIB_DIR);

        agentHost =
            envProperties
                .getMachineHostnameByRoleId(ApmSqlServerWindowsClusterTestbed.TOMCAT_AGENT_ROLE_ID);


    }
    
    @BeforeClass(alwaysRun = true)
    public void ApmSqlServerTests_MOM() {
        /*
        *//**
         * This method is to start all services
         * MOM
         * COLLECTORS
         * TOMCAT
         * JBOSS
         */
        try{
        startAll();
        }catch(Exception e){
            e.printStackTrace();
        }
           
    }
    
     
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454272_environmentSetUp(){
         LOGGER
             .info("Test ID & Name :454272 Setting up Environment for Executing tests");        
               
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
       
        LOGGER
        .info("Cluster(1 MOM,2 Collectors,2 Agents are deployed & are started.ApmSqlServer is also up and running");
        
       
    }
    
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454273_RetrieveNoOfrecords(){
         LOGGER
             .info("Test ID & Name :454273 Valid Count Aggregation Function");        
               
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
       
       
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
        
        try{
        String teiidQuery ="select COUNT(AGENT_NAME) from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 1";
        
        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionCount(teiidQuery) == 1, true); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    }
    
    @Test(groups = {"ApmSqlServer", "BAT"})
        
    private void verify_ALM_454274_RetreiveAgentInfo(){
        LOGGER
            .info("Test ID & Name :454274 Agent details retreived from Sql Server");    
       
       
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
       
        try{
        String teiidQuery ="select AGENT_NAME from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 1";
        
        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionString(teiidQuery), "Tomcat Agent"); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    }    
    
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454275_RetreiveFrequencyInterval(){
        LOGGER
            .info("Test ID & Name :454275 Frequency Validation for records retreived at 15s frequency interval"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
        try{
        String teiidQuery ="select FREQUENCY from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 1";
        
        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionValue(teiidQuery), 15000); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 
  
 //Not Supported fro Current release.. Can uncomment when supported in future.
 /*   @Test(groups = {"ApmSqlServer", "SMOKE"})
    private void verify_ALM_454276_RetreiveHigherFrequencyInterval(){
        LOGGER
            .info("Test ID & Name :454276 Retreive Higher frequency interval from APM Sql Server with specified constraints"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
        try{
        String teiidQuery ="select FREQUENCY from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=60000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 1";

        LOGGER.info(teiidQuery);
        
        assertEquals(teiidQueryExecutionValue(teiidQuery), 60000); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    }*/
    
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454277_RetreiveRecordsCount(){
        LOGGER
            .info("Test ID & Name :454277 Retreive records count from APM Sql Server with specified constraints"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
       
        try{
        String teiidQuery ="select * from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+ "limit 3";
        
        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionCount(teiidQuery), 3); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 
 
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454278_LikeConditionValidation(){
        LOGGER
            .info("Test ID & Name :454278 Query Validation with LIKE Condition"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        try{
        String teiidQuery ="select AGENT_NAME from numerical_metric_data where AGENT_NAME like '%JBoss%' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+ "limit 1";

        LOGGER.info(teiidQuery);
        
        assertEquals(teiidQueryExecutionString(teiidQuery),"JBoss Agent"); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 
 
   @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454279_NotLikeConditionValidation(){
        LOGGER
            .info("Test ID & Name :454279 Query Validation with NOT LIKE Condition"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
        try{
        String teiidQuery ="select AGENT_NAME from numerical_metric_data where AGENT_NAME NOT LIKE '%JBoss%' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+ "limit 1";

        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionString(teiidQuery),"Tomcat Agent"); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 
    
    @Test(groups = {"ApmSqlServer", "SMOKE"})
    private void verify_ALM_454280_GroupByValidation(){
        LOGGER
            .info("Test ID & Name :454280 Query Validation with GROUP BY Clause"); 
        
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
        try{
        String teiidQuery ="SELECT AGENT_NAME FROM numerical_metric_data WHERE frequency =15000 AND AGENT_NAME = 'JBoss Agent' AND metric_attribute='Average Response Time (ms)' AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+" "+"GROUP BY AGENT_NAME,METRIC_PATH,AGG_VALUE";
   
        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionString(teiidQuery),"JBoss Agent"); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    }
    
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454281_inConditionValidation(){
        LOGGER
            .info("Test ID & Name :454281 Query Validation for IN condition");        
        
     
     try{
         Thread.sleep(5000);
     }catch(Exception e){
         e.printStackTrace();
     }
     
     Calendar start = new GregorianCalendar();
     Calendar end = (Calendar) start.clone();
     start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
     
     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
     String startFormated = formatter.format(start.getTime());
     String endFormated = formatter.format(end.getTime());

     
     try{
         String teiidQuery ="SELECT AGENT_NAME FROM numerical_metric_data WHERE frequency =15000 AND AGENT_NAME in ('JBoss Agent') AND metric_attribute='Average Response Time (ms)' AND ts "
                 + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'}"+ "limit 1";
         
         LOGGER.info(teiidQuery);
                 
         assertEquals(teiidQueryExecutionString(teiidQuery),"JBoss Agent");  
     
     }catch(Exception E){
         E.printStackTrace();
     }
    
 }  
    
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454400_NotInConditionValidation(){
        LOGGER
            .info("Test ID & Name :454400 Query Validation with NOT IN Condition");        
        
     
     try{
         Thread.sleep(5000);
     }catch(Exception e){
         e.printStackTrace();
     }
     
     Calendar start = new GregorianCalendar();
     Calendar end = (Calendar) start.clone();
     start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
     
     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
     String startFormated = formatter.format(start.getTime());
     String endFormated = formatter.format(end.getTime());
     
    
     try{
         String teiidQuery ="SELECT AGENT_NAME FROM numerical_metric_data WHERE frequency =15000 AND AGENT_NAME NOT in ('JBoss Agent') AND metric_attribute='Average Response Time (ms)' AND ts "
                 + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+" "+"ORDER BY AGENT_NAME DESC"+" "+"limit 1";
         
                 
         LOGGER.info(teiidQuery);
                 
         assertEquals(teiidQueryExecutionString(teiidQuery),"Tomcat Agent");  
     
     }catch(Exception E){
         E.printStackTrace();
     }
    
 }
    
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454401_NofrequencyConditionCheck(){
         LOGGER
             .info("Test ID & Name :454401 Query Validation without Frequency Attribute");        
               
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
       
       
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
       
        try{
        String teiidQuery ="select FREQUENCY from numerical_metric_data where ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 1";
        
        LOGGER.info(teiidQuery);
        
        System.out.println(teiidQuery);
        assertEquals(teiidQueryExecutionValue(teiidQuery), 15000);
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    }    
    
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454282_OrderByDescValidation(){
        LOGGER
            .info("Test ID & Name :454282 Query Validation with ORDERBY & DESC constraints");        
        
     
     try{
         Thread.sleep(5000);
     }catch(Exception e){
         e.printStackTrace();
     }
     
     Calendar start = new GregorianCalendar();
     Calendar end = (Calendar) start.clone();
     start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
     
     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
     String startFormated = formatter.format(start.getTime());
     String endFormated = formatter.format(end.getTime());

     
     try{
         String teiidQuery ="SELECT AGENT_NAME FROM numerical_metric_data WHERE frequency =15000 AND AGENT_NAME in ('JBoss Agent','Tomcat Agent') AND metric_attribute='Average Response Time (ms)' AND ts "
                 + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+" "+"ORDER BY AGENT_NAME DESC";
         
                 
         LOGGER.info(teiidQuery);  
         
         assertEquals(teiidQueryExecutionString(teiidQuery),"Tomcat Agent");  
     
     }catch(Exception E){
         E.printStackTrace();
     }
    
 } 
 
    @Test(groups = {"ApmSqlServer", "BAT"})
    private void verify_ALM_454283_OrderByAscValidation(){
        LOGGER
            .info("Test ID & Name :454283 Query Validation with ORDERBY & ASC Validation"); 
        
     try{
         Thread.sleep(5000);
     }catch(Exception e){
         e.printStackTrace();
     }
     
     Calendar start = new GregorianCalendar();
     Calendar end = (Calendar) start.clone();
     start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
     
     SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
     String startFormated = formatter.format(start.getTime());
     String endFormated = formatter.format(end.getTime());
     
     try{
         String teiidQuery ="SELECT AGENT_NAME FROM numerical_metric_data WHERE frequency =15000 AND AGENT_NAME in ('JBoss Agent','Tomcat Agent','Custom Metric Agent (Virtual)') AND metric_attribute='Average Response Time (ms)' AND ts "
                 + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+" "+"ORDER BY AGENT_NAME ASC";
         
                 
         LOGGER.info(teiidQuery);
        
         assertEquals(teiidQueryExecutionString(teiidQuery),"Custom Metric Agent (Virtual)");  
     
     }catch(Exception E){
         E.printStackTrace();
     }
    
 }

    @Test(groups = {"ApmSqlServer", "SMOKE"})
    private void verify_ALM_454284_MultipleQueriesValidation(){
        LOGGER
            .info("Test ID & Name :454284 Validating Mutliple queries triggered to Sql Server"); 
          
    
    try{
        Thread.sleep(5000);
    }catch(Exception e){
        e.printStackTrace();
    }
    
    Calendar start = new GregorianCalendar();
    Calendar end = (Calendar) start.clone();
    start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
    
    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
    String startFormated = formatter.format(start.getTime());
    String endFormated = formatter.format(end.getTime());
    
    
    try{
        String teiidQuery ="select AGENT_NAME from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 1";
        
        String teiidQuery1 ="select AGENT_NAME from numerical_metric_data where AGENT_NAME = 'JBoss Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 1";        
        
        LOGGER.info(teiidQuery);
         LOGGER.info(teiidQuery1);
         
        assertEquals(teiidQueryExecutionString(teiidQuery),"Tomcat Agent");  
        assertEquals(teiidQueryExecutionString(teiidQuery1),"JBoss Agent"); 
    
    }catch(Exception E){
        E.printStackTrace();
    }
   
} 
    
      
    @Test(groups = {"ApmSqlServer", "DEEP"})
    private void verify_ALM_454286_wildCardConditionValidation(){
        LOGGER
            .info("Test ID & Name :454286 Query Validation using a wildcard constraint check"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        
        try{
        String teiidQuery ="select AGENT_NAME from numerical_metric_data where AGENT_NAME LIKE '_Boss Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+ "limit 1";
        
        LOGGER.info(teiidQuery);
        
        assertEquals(teiidQueryExecutionString(teiidQuery),"JBoss Agent"); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    }     
  
    @Test(groups = {"ApmSqlServer", "SMOKE"})
    private void verify_ALM_454287_subQueryValidation(){
        LOGGER
            .info("Test ID & Name :454287 Query Validation using sub query"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        
        
        try{
        String teiidQuery ="select AGENT_NAME from numerical_metric_data where AGENT_NAME in (select DISTINCT AGENT_NAME from numerical_metric_data where AGENT_NAME = 'JBoss Agent' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'}) and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+ "limit 1";
        
                
        LOGGER.info(teiidQuery);
        
        
        assertEquals(teiidQueryExecutionString(teiidQuery),"JBoss Agent"); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    }
    
    @Test(groups = {"ApmSqlServer", "SMOKE"})
    
    private void verify_ALM_454288_UNIONOperatorValidation(){
        LOGGER
            .info("Test ID & Name :454288 Retreving results from multiple queries using UNION operator");    
       
       
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        

        try{
        String teiidQuery ="select AGENT_NAME from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+"UNION"+" "+"select AGENT_NAME from numerical_metric_data where AGENT_NAME = 'JBoss Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} ";        
        
        
        LOGGER.info(teiidQuery);
        
        
        assertEquals(teiidQueryExecutionCount(teiidQuery), 2); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 
    
    @Test(groups = {"ApmSqlServer", "SMOKE"})
    private void verify_ALM_454289_SUMAggregatorValidation(){
        LOGGER
            .info("Test ID & Name :454289 Validating SUM Aggregator function"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());
        

        try{
        String teiidQuery ="select SUM(FREQUENCY) from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 3";
        
        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionValue(teiidQuery), 60000); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 
    
    
    @Test(groups = {"ApmSqlServer", "SMOKE"})
    private void verify_ALM_454290_AVgAggregatorValidation(){
        LOGGER
            .info("Test ID & Name :454289 Validating AVG Aggregator function"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        
        try{
        String teiidQuery ="select AVG(FREQUENCY) from numerical_metric_data where AGENT_NAME = 'Tomcat Agent' and metric_attribute = 'Bytes In Use' and frequency=15000 AND ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} " + "limit 3";

        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionValue(teiidQuery), 15000); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 
    
    @Test(groups = {"ApmSqlServer", "smoke"})
    private void verify_ALM_424291_aliasValidation(){
        LOGGER
            .info("Test Name : Retreive details using alias of a table"); 
        
        try{
            Thread.sleep(5000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);
        
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.0");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        try{
        String teiidQuery ="select COUNT(pnd.FREQUENCY) from numerical_metric_data pnd where pnd.AGENT_NAME = 'JBoss Agent' and pnd.metric_attribute = 'Bytes In Use' and pnd.frequency=15000 AND pnd.ts "
                + "between "+"{ts '"+ startFormated + "'} and "+"{ts '" + endFormated + "'} "+ "limit 3";
        
        LOGGER.info(teiidQuery);
        assertEquals(teiidQueryExecutionValue(teiidQuery),4); 
        
        }catch(Exception E){
            E.printStackTrace();
        }
       
    } 

   public int teiidQueryExecutionCount(String teiidQuery){
       
           
       try{
               System.out.println(apmSqlClientDir); 
               EmConfiguration config =
                   new EmConfiguration(emInstallDir, ApmSqlServerWindowsClusterTestbed.EM_PORT,apmSqlClientDir);
               
                             
               Thread.sleep(10 * 1000);               
               
               resultCount=0;
               try (IntroscopeDBAccessUtil dbAccess = new IntroscopeDBAccessUtil()) {
                   dbAccess.setUpDriver(config.getJDBCTeiidDriverPath());
                   dbAccess.setUpConnection("localhost:54321");
                   resultCount = dbAccess.getCount(teiidQuery);
                   System.out.println(resultCount);
                   
               }catch(Exception e){
                   e.printStackTrace();
               }                    
          
      }catch (Exception e) {
         e.printStackTrace(); 
      }
       System.out.println(resultCount);
       return resultCount;
       
   }
   
   public String teiidQueryExecutionString(String teiidQuery){
       List<String> StringList = new ArrayList<String>();
       
       try{
           
           System.out.println(emInstallDir);
           
               EmConfiguration config =
                   new EmConfiguration(emInstallDir, ApmSqlServerWindowsClusterTestbed.EM_PORT,apmSqlClientDir);
               
                             
               Thread.sleep(10 * 1000);               
               
              
               try (IntroscopeDBAccessUtil dbAccess = new IntroscopeDBAccessUtil()) {
                   dbAccess.setUpDriver(config.getJDBCTeiidDriverPath());
                   dbAccess.setUpConnection("localhost:54321");
                   StringList = dbAccess.getString(teiidQuery);
                   stringData=StringList.get(0);
                   System.out.println(StringList.get(0));
                   
               }catch(Exception e){
                   e.printStackTrace();
               }                    
          
      }catch (Exception e) {
         e.printStackTrace(); 
      }
       System.out.println(stringData);
       return stringData;
       
   }
    
   public int teiidQueryExecutionValue(String teiidQuery){  
       List<Integer> dataList = new ArrayList<Integer>();
       
       try{
          
               EmConfiguration config =
                   new EmConfiguration(emInstallDir, ApmSqlServerWindowsClusterTestbed.EM_PORT,apmSqlClientDir);
               
                             
               Thread.sleep(10 * 1000);               
               
               try (IntroscopeDBAccessUtil dbAccess = new IntroscopeDBAccessUtil()) {
                   dbAccess.setUpDriver(config.getJDBCTeiidDriverPath());
                   dbAccess.setUpConnection("localhost:54321");
                   dataList = dbAccess.getValue(teiidQuery);
                   dataValue=dataList.get(0);
                   System.out.println(dataValue);
                   
               }catch(Exception e){
                   e.printStackTrace();
               }                    
          
      }catch (Exception e) {
         e.printStackTrace(); 
      }
       System.out.println(dataValue);
       return dataValue;
       
   }
   
    private void syncTimeOnMachines(Collection<String> machineIds) {
        for (String machineId : machineIds) {
            runFlowByMachineId(machineId, TimeSyncFlow.class,
                new TimeSyncFlowContext.Builder().build());
        }
    }
    
  
    private void startAll() throws Exception{

        syncTimeOnMachines(Arrays.asList(ApmSqlServerWindowsClusterTestbed.MOM_MACHINE_ID,
            ApmSqlServerWindowsClusterTestbed.COLLECTOR1_MACHINE_ID,
            ApmSqlServerWindowsClusterTestbed.COLLECTOR2_MACHINE_ID,
            ApmSqlServerWindowsClusterTestbed.AGENT_MACHINE_ID));

       
        runSerializedCommandFlowFromRole(ApmSqlServerWindowsClusterTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_START);
        runSerializedCommandFlowFromRole(ApmSqlServerWindowsClusterTestbed.JBOSS_ROLE_ID,
            JbossRole.ENV_JBOSS_START);

        runSerializedCommandFlowFromRole(ApmSqlServerWindowsClusterTestbed.MOM_ROLE_ID,
            EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(ApmSqlServerWindowsClusterTestbed.COLLECTOR1_ROLE_ID,
            EmRole.ENV_START_EM);
        runSerializedCommandFlowFromRole(ApmSqlServerWindowsClusterTestbed.COLLECTOR2_ROLE_ID,
            EmRole.ENV_START_EM);

        waitForAgentNodes();
        
       /*
        //No Need Unzip hence installer is placing in correct location  	   
	    //Unzip APMSql Server Folder present in EM home directory
        ZipUtils zp = new ZipUtils();
        String zipFileSourceLoc=emInstallDir+"/APMSQLServer"+"/APMSQL Server-99.99.centaurusSP-bin.zip";
        String zipfileDestLoc=emInstallDir+"/APMSQLServer";
        System.out.println(zipFileSourceLoc);
        System.out.println(zipfileDestLoc);
        try{
            zp.unZipUpdate(zipFileSourceLoc,zipfileDestLoc);
            }catch(Exception e){
            e.printStackTrace();
        }
      */
    
        String apmSqlBatLocation=emInstallDir+"/APMSqlServer"+"/bin";
        
        //Start ApmSqlServer by passing emHost and emPort arguments
        try{
        Thread.sleep(3000);
        }catch(Exception e){
            e.printStackTrace();
        }
        
        Assert.assertTrue(execCmdNoWait("cmd /C "+"CD"+" "+apmSqlBatLocation+" "+"&"+" "+"apmsql.bat"));
                
        try{
            Thread.sleep(50000);
            copyFileToDir(teiidDriverFile, emLibDir);
            }catch(Exception e){
                e.printStackTrace();
            }      
       
    }      
    
    
    private void stopAll() {
        
        
        //Stopping SqlServer
        
        String appname="com.ca.apm.server.teiid.APMSQLServer";
        try{
        killProcess(getProcessId(appname));
        }        
        catch(Exception e){
            e.printStackTrace();
        }

        EmUtils emUtils = utilities.createEmUtils();
        ClwUtils clwUtilsMOM =
            utilities.createClwUtils(ApmSqlServerWindowsClusterTestbed.MOM_ROLE_ID);
        ClwUtils clwUtilsCollector1 =
            utilities.createClwUtils(ApmSqlServerWindowsClusterTestbed.COLLECTOR1_ROLE_ID);
        ClwUtils clwUtilsCollector2 =
            utilities.createClwUtils(ApmSqlServerWindowsClusterTestbed.COLLECTOR2_ROLE_ID);
        
        // Stop Collector2
        try {
            emUtils.stopRemoteEmWithTimeoutSec(clwUtilsMOM.getClwRunner(),
                clwUtilsCollector2.getClwRunner(), 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown Collector 2");
        }
        // Stop Collector1
        try {
            emUtils.stopRemoteEmWithTimeoutSec(clwUtilsMOM.getClwRunner(),
                clwUtilsCollector1.getClwRunner(), 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown Collector 1");
        }

        // Stop MOM
        try {
            emUtils.stopLocalEmWithTimeoutSec(clwUtilsMOM.getClwRunner(),
               ApmSqlServerWindowsClusterTestbed.MOM_ROLE_ID, 240);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(" Improper Shutdown MOM");
        }
        
  
        
        runSerializedCommandFlowFromRole(ApmSqlServerWindowsClusterTestbed.TOMCAT_ROLE_ID,
            TomcatRole.ENV_TOMCAT_STOP);
        runSerializedCommandFlowFromRole(ApmSqlServerWindowsClusterTestbed.JBOSS_ROLE_ID,
            JbossRole.ENV_JBOSS_STOP);

    }    
    
    private void waitForAgentNodes() {
        final String tomcatNodeString = agentHost + "|Tomcat|Tomcat Agent";
        final String jbossNodeString = agentHost + "|JBoss|JBoss Agent";
        String value;
        int i = 0;
        List<String> nodeList;
        int count = 0;
        for (i = 0; i < 20; i++) {
            nodeList = clwCommon.getNodeList(user, "", ".*", host, port, emLibDir);

            Iterator<String> nodeListIterator = nodeList.iterator();
            while (nodeListIterator.hasNext()) {
                value = nodeListIterator.next();
                if (value.equalsIgnoreCase(tomcatNodeString))
                    count++;
                else if (value.equalsIgnoreCase(jbossNodeString)) count++;
            }
            if (count == 2)
                break;
            else {
                count = 0;
                harvestWait(60);
            }
        }
        if (i == 20) Assert.assertTrue(false);
    }       
    
    private void harvestWait(int seconds) {
        try {
            LOGGER.info("Harvesting crops.");
            Thread.sleep(seconds * 1000);
            LOGGER.info("Crops harvested.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static String getProcessId(String name,
                                      InputStream inputStream) throws IOException {

        LOGGER.info("[getProcessId] checking if " + name + " is running.");
        BufferedReader reader = null;
        String pid = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.info("[getProcessId] available process: " + line);
                if (line.contains(name)) {
                    LOGGER.info("[getProcessId] *** process " + name + " is running ***");
                    String[] out = line.split(" ");
                    pid = out[0]; break;
                }
            }
            if(pid == null) LOGGER.info("[getProcessId] process " + name + " is not running.");

            } catch (IOException e) {
                e.printStackTrace();
            }
        finally {
            if (reader != null) {
                reader.close();
            }
        }
        System.out.println(pid);
        return pid;
    }
        
        public static String getProcessId(String name)
        {
            String pid = null;
             try {
                List<String> lOptions = new ArrayList<String>();
                lOptions.add("jps");
                lOptions.add("-l");
                ProcessBuilder builder = new ProcessBuilder(lOptions);
                Process process = builder.start();
                pid = getProcessId(name, process.getInputStream());

                if (pid == null ) {
                    System.out.println("jps didnt return result");
                    }
                
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            return pid;
        }           
    public static void killProcess (String pid) {

        String command = null;

        if (pid != null) {

            System.out.println ("[killProcess] killing pid " + pid);
            if (System.getProperty("os.name").contains("Windows")) {
                command = "taskkill /F /PID " + pid;
            }
            else {
                command = "kill -9 " + pid;
            }
            runOSCommand(command);
        }
    }

    public static void runOSCommand (String command) {

        try {
            if (command == null) {
                System.out.println("System command to execute wasn't provided.");
                return;
            }

            System.out.println("Running command: " + command);
            Process p = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println (line);
            }
            reader.close();
        } catch  (IOException e) {
            System.out.println("Error occurred while trying to execute command " + command);
            e.printStackTrace();
        }
    }
    
      /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }
    
    /**
     * @return the urlPrefix
     */
    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }
    
    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }
    

   /**
    * @return the sleep
    */
   public int getSleep() {
       return sleep;
   }
   
   public static boolean execCmdNoWait(String cmd, String workingDir){
       try{
           System.out.println("Running command: "+cmd);
           Runtime.getRuntime().exec(cmd, null, workingDir==null?null:new File(workingDir));
           return true;
         
       }
       catch(IOException e){
           e.printStackTrace();
           return false;
       }
   }
   
   public static boolean execCmdNoWait(String cmd){
       try{
           System.out.println("Running command: "+cmd);
           Runtime.getRuntime().exec(cmd);
           return true;
       }
       catch(IOException e){
           e.printStackTrace();
           return false;
       }
   }
   
   private Set<Integer> portCheck() throws Exception {
       Set<Integer> ports = OsLocalUtils.getEnabledPorts();
       Assert.assertFalse(ports.isEmpty());
       return ports;
   }
   
   public static boolean copyFileToDir(String file, String targetDirectory){
       
       try{
           FileUtils.copyFileToDirectory(new File(file), new File(targetDirectory));
           return true;
       }
       catch(Exception e){
           e.printStackTrace();
           return false;
       }
       
   }
    
    @AfterClass(alwaysRun = true)
    public void teardown() {
        stopAll();
    }    
    
}
