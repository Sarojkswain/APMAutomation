/*
 * Copyright (c) 2014 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and proprietary and shall not
 * be duplicated, used, disclosed or disseminated in any way except as authorized by the applicable
 * license agreement, without the express written permission of CA. All authorized reproductions
 * must be marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT PERMITTED BY APPLICABLE
 * LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF ANY KIND, INCLUDING WITHOUT LIMITATION, ANY
 * IMPLIED WARRANTIES OF MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR INDIRECT, FROM THE
 * USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST PROFITS, BUSINESS INTERRUPTION,
 * GOODWILL, OR LOST DATA, EVEN IF CA IS EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.test;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.apm.automation.action.flow.utility.ConfigureFlow;
import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;
import com.ca.apm.tests.flow.RunMemoryMonitorFlow;
import com.ca.apm.tests.flow.RunMemoryMonitorFlowContext;
import com.ca.apm.tests.flow.RunWebViewLoadFlow;
import com.ca.apm.tests.flow.RunWebViewLoadFlowContext;
import com.ca.apm.tests.role.*;
import com.ca.apm.tests.testbed.G1TestBed;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.HammondRole;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertTrue;

/**
 * EM regression test.
 *
 * starts loads, monitors collectors, mom and webview.
 */
public class IsengardTest extends MyTasTestNgTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(IsengardTest.class);

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_com_wily() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.com.wily", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_com_timestock() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.com.timestock", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_com_ca_wily() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.com.ca.wily", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Apm_Data_Model() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Apm.Data.Model", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_Support() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.Support", "DEBUG,supportlogfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_AT() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.AT", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_AppMap_PublicApi() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.AppMap.PublicApi", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_AppMap_Alert_Mapping() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.AppMap.Alert-Mapping", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_AppMap_UVB_Mapping() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.AppMap.UVB-Mapping", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_JavaScriptCalculator() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.JavaScriptCalculator", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_apm_events() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.apm.events", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_apm_dynamicDomainsConfiguration() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.apm.dynamicDomainsConfiguration", "DEBUG,console,dynamicDomainslog");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_com_ca_apm_em_idp() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.com.ca.apm.em.idp", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_com_ca_apm_saml() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.com.ca.apm.saml", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_com_ca_apm_axa() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.com.ca.apm.axa", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_Shibboleth() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.Shibboleth", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_org_apache_jasper_compiler_TagLibraryInfo() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.org.apache.jasper.compiler.TagLibraryInfo", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_com_wily_apm_tess_supportability_TessPerformanceCollector() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.com.wily.apm.tess.supportability.TessPerformanceCollector", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_QueryLog() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.QueryLog", "DEBUG,console,logfile");
        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_TeamCenterRegistration() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.TeamCenterRegistration", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }
    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"debuglog"})
    public void test_debug_Manager_CLW() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("log4j.logger.Manager.CLW", "DEBUG,console,logfile");

        runTest(laxOpts, propOpts);
    }


    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"isengard"})
    public void test_ootb() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        runTest(laxOpts, null);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"isengard"})
    public void test_6000_10_10() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                    "-Djava.awt.headless=true " +
                    "-XX:MaxPermSize=256m " +
                    "-Dmail.mime.charset=UTF-8 " +
                    "-Dorg.owasp.esapi.resources=./config/esapi " +
                    "-XX:+UseConcMarkSweepGC " +
                    "-XX:+UseParNewGC " +
                    "-Xss512k " +
                    "-Dcom.wily.assert=false " +
                    "-showversion " +
                    "-Dcom.sun.management.jmxremote " +
                    "-Dcom.sun.management.jmxremote.port=4444 " +
                    "-Dcom.sun.management.jmxremote.authenticate=false " +
                    "-Dcom.sun.management.jmxremote.ssl=false " +
                    "-XX:CMSInitiatingOccupancyFraction=50 " +
                    "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                    "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                    "-verbose:gc " +
                    "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("transport.outgoingMessageQueueSize", "6000");
        propOpts.put("transport.override.isengard.high.concurrency.pool.min.size", "10");
        propOpts.put("transport.override.isengard.high.concurrency.pool.max.size", "10");

        runTest(laxOpts, propOpts);
    }

    @Tas(testBeds = @TestBed(name = G1TestBed.class, executeOn = G1TestBed.LOAD1_MACHINE_ID), size = SizeType.BIG, exclusivity = ExclusivityType.EXCLUSIVE)
    @Test(groups = {"isengard"})
    public void test_12000_20_20() throws  Exception {
        Map<String, String> laxOpts = Collections.singletonMap(
                "lax.nl.java.option.additional",
                "-Djava.awt.headless=true " +
                        "-XX:MaxPermSize=256m " +
                        "-Dmail.mime.charset=UTF-8 " +
                        "-Dorg.owasp.esapi.resources=./config/esapi " +
                        "-XX:+UseConcMarkSweepGC " +
                        "-XX:+UseParNewGC " +
                        "-Xss512k " +
                        "-Dcom.wily.assert=false " +
                        "-showversion " +
                        "-Dcom.sun.management.jmxremote " +
                        "-Dcom.sun.management.jmxremote.port=4444 " +
                        "-Dcom.sun.management.jmxremote.authenticate=false " +
                        "-Dcom.sun.management.jmxremote.ssl=false " +
                        "-XX:CMSInitiatingOccupancyFraction=50 " +
                        "-Xms" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-Xmx" + G1TestBed.getMem(G1TestBed.XM_COL_MEM) + " " +
                        "-verbose:gc " +
                        "-Xloggc:" + G1TestBed.GC_EM_LOG_FILE);

        Map<String, String> propOpts = new HashMap<>();
        propOpts.put("transport.outgoingMessageQueueSize", "12000");
        propOpts.put("transport.override.isengard.high.concurrency.pool.min.size", "20");
        propOpts.put("transport.override.isengard.high.concurrency.pool.max.size", "20");

        runTest(laxOpts, propOpts);
    }

    private void runTest(Map<String, String> laxOpts, Map<String, String> propOpts) throws Exception {
        // set java opts
        String installDir = envProperties.getRolePropertyById(G1TestBed.EM_ROLE_ID, EmRole.ENV_PROPERTY_INSTALL_DIR);

        if (laxOpts != null && !laxOpts.isEmpty()) {
            ConfigureFlowContext laxCtx = new ConfigureFlowContext.Builder()
                    .configurationMap(
                            installDir + "\\Introscope_Enterprise_Manager.lax", laxOpts)
                    .build();
            runFlowByMachineId(G1TestBed.EM_MACHINE_ID, ConfigureFlow.class, laxCtx);
        }
        if (propOpts != null && !propOpts.isEmpty()) {
            ConfigureFlowContext propCtx = new ConfigureFlowContext.Builder()
                    .configurationMap(
                            installDir + "\\config\\IntroscopeEnterpriseManager.properties", propOpts)
                    .build();
            runFlowByMachineId(G1TestBed.EM_MACHINE_ID, ConfigureFlow.class, propCtx);
        }

        startEm();
        startPerfMon();

        startHammondLoad();
        startFakeWorkstationLoad();
        startWurlitzerLoad();
        startMemoryMonitor();
        startDashboard();

        long endTimestamp =
                System.currentTimeMillis() + G1TestBed.getRunDuration(TimeUnit.MILLISECONDS);
        LOGGER.info("end timestamp: " + endTimestamp);

        while (endTimestamp > System.currentTimeMillis()) {

            checkServiceIsRunning(G1TestBed.EM_ROLE_ID, 5001);
            checkServiceIsRunning(G1TestBed.EM_ROLE_ID, 8082);

            LOGGER.info("cluster is running. Test ends in {} minutes.", (endTimestamp - System.currentTimeMillis()) / 60000);
            Thread.sleep(60000);
        }
        LOGGER.info("test duration is over");

        runSerializedCommandFlowFromRole(G1TestBed.PERFMON_EM_ROLE_ID, PerfMonitorRole.ENV_GET_PERF_LOG);

        assertTrue(true);
    }

    private void checkServiceIsRunning(String roleId, int port) {
        String host = envProperties.getMachineHostnameByRoleId(roleId);
        utilities.createPortUtils().waitTillRemotePortIsBusyInSec(host, port, 10);
    }

    @AfterTest
    public void stopProcesses() throws Exception {
        stopPerfMon();
        stopMemoryMonitor();
        stopEm();
    }

    private void startEm() {
        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_START_EM);

        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_START_WEBVIEW);
    }

    private void stopEm() {
        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_STOP_WEBVIEW);
        runSerializedCommandFlowFromRole(G1TestBed.EM_ROLE_ID,
            EmRole.ENV_STOP_EM);
    }

    private void startWurlitzerLoad() {
        runSerializedCommandFlowFromRole(G1TestBed.WURLITZER_ROLE_ID,
            WurlitzerRole.ENV_RUN_WURLITZER);
    }

    private void startFakeWorkstationLoad() {
        for (String id : getSerializedIds(G1TestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID,
                CautlRole.ENV_CAUTL_START)) {
            runSerializedCommandFlowFromRole(
                    G1TestBed.FAKE_WORKSTATION_CAUTL_ROLE_ID, id);
        }
    }

    private void startHammondLoad() {
        for (final String id : getSerializedIds(G1TestBed.HAMMOND_ROLE_ID,
            HammondRole.ENV_HAMMOND_START)) {
            new Thread() {
                public void run() {
                    runSerializedCommandFlowFromRole(G1TestBed.HAMMOND_ROLE_ID, id);
                }
            }.start();
        }
    }

    private void startPerfMon() {
        try {
            runSerializedCommandFlowFromRole(
                    G1TestBed.PERFMON_EM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        } catch (Exception e) {
            runSerializedCommandFlowFromRole(
                    G1TestBed.PERFMON_EM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_STOP);
            runSerializedCommandFlowFromRole(
                    G1TestBed.PERFMON_EM_ROLE_ID,
                PerfMonitorRole.ENV_PERF_MONITOR_START);
        }
    }

    private void startMemoryMonitor() {
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;

        String roleId = "memoryMonitorRole_" + G1TestBed.EM_MACHINE_ID;
        try {
            Map<String, String> roleProps =
                    Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
            IFlowContext startFlowContext = deserializeFromProperties(roleId,
                    MemoryMonitorRole.ENV_MEMORY_MONITOR_START, roleProps,
                    RunMemoryMonitorFlowContext.class);
            runFlowByMachineIdAsync(G1TestBed.EM_MACHINE_ID, flowClass, startFlowContext, TimeUnit.DAYS, 2);

        } catch (Exception e) {
            LOGGER.warn("Unable to start memory monitor on {}, {}", roleId,
                    e.getMessage());
        }
    }

    private void stopMemoryMonitor() {
        Class<? extends IAutomationFlow> flowClass = RunMemoryMonitorFlow.class;

        String roleId = "memoryMonitorRole_" + G1TestBed.EM_MACHINE_ID;
        try {
            Map<String, String> roleProps =
                    Maps.fromProperties(envProperties.getRolePropertiesById(roleId));
            IFlowContext startFlowContext = deserializeFromProperties(roleId,
                    MemoryMonitorRole.ENV_MEMORY_MONITOR_STOP, roleProps,
                    RunMemoryMonitorFlowContext.class);
            runFlowByMachineIdAsync(G1TestBed.EM_MACHINE_ID, flowClass, startFlowContext, TimeUnit.MINUTES, G1TestBed.getRunDuration(TimeUnit.MINUTES) + 30);

        } catch (Exception e) {
            LOGGER.warn("Unable to start memory monitor on {}, {}", roleId,
                    e.getMessage());
        }
    }

    private void stopPerfMon() {
        runSerializedCommandFlowFromRole(G1TestBed.PERFMON_EM_ROLE_ID,
            PerfMonitorRole.ENV_PERF_MONITOR_STOP);
    }

    private void startDashboard() {
        String roleId = G1TestBed.WEB_VIEW_LOAD_ROLE;
        String machineId = envProperties.getMachineIdByRoleId(roleId);
        Map<String, String> roleProps = Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        IFlowContext startFlowContext = deserializeFromProperties(roleId,
                WebViewLoadRole.ENV_WEBVIEW_LOAD_START, roleProps,
                RunWebViewLoadFlowContext.class);
        runFlowByMachineIdAsync(machineId, RunWebViewLoadFlow.class, startFlowContext, TimeUnit.MINUTES, G1TestBed.getRunDuration(TimeUnit.MINUTES) + 30);
    }

    private Iterable<String> getSerializedIds(String roleId, String prefix) {
        Map<String, String> roleProperties =
            Maps.fromProperties(envProperties.getRolePropertiesById(roleId));

        HashSet<String> startIds = new HashSet<>();
        for (String key : roleProperties.keySet()) {
            if (key.startsWith(prefix)) {
                startIds.add(key.split("::")[0]);
            }
        }
        return startIds;
    }
}
