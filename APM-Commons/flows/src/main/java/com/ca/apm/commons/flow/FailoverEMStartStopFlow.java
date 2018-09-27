/*
 * Copyright (c) 2015 CA. All rights reserved.
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
 */
package com.ca.apm.commons.flow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.ApmbaseUtil;

/**
 * This Flow is for Starting and Stopping the Failover/Secondary EM
 *
 */
@Flow
public class FailoverEMStartStopFlow extends FlowBase {
    @FlowContext
    private FailoverEMStartStopFlowContext context;

    private static final Logger LOGGER = LoggerFactory.getLogger(FailoverEMStartStopFlow.class);

    @Override
    public void run() throws Exception {

        LOGGER.info("Checking the Secondary EM start or Stop {} {} {}", context.getdir(),
            context.getEmType());

        if (context.getEmType().contains("startFailOverEM")) {
            startSecondaryEM();
        }

        if (context.getEmType().contains("stopFailOverEM")) {
            stopSecondaryEM();
        }
        
        if(context.getEmType().contains("startFailOverLocalEM")){
            startSecondaryLocalEM();
        }
        
        if(context.getEmType().contains("stopFailOverLocalEM")){
            stopSecondaryLocalEM();
        }
        
        if(context.getEmType().contains("startFailOverPrimaryEM")){
            startSecondPrimaryEM();
        }        

        if(context.getEmType().contains("startPrimaryEMWithoutLock")){
        	startPrimaryEMWithoutLock();
        }        
        
        if(context.getEmType().contains("startSecondPrimaryEMFirst")){
        	startSecondPrimaryEMFirst();
        } 
        
        LOGGER.info("Task completed.");
    }


    protected void startSecondaryEM() {

        LOGGER.info("About to start Failover EM from {}" + context.getdir());
        String command =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? "Introscope_Enterprise_Manager.exe"
                : "./Introscope_Enterprise_Manager";
        String startCommand = context.getdir() + "/" + command;

        ProcessBuilder pb =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? new ProcessBuilder("cmd.exe", "/C", startCommand)
                : new ProcessBuilder(startCommand);

        pb.redirectErrorStream(true);
        Process subprocess;
        try {
            subprocess = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = subprocess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader)) {

            String textToMatch =
                "The Introscope Enterprise Manager is configured as a Secondary EM";
            String line;
            while ((line = br.readLine()) != null) {
                LOGGER.info(line);
                if (line.contains(textToMatch)) {
                    LOGGER
                        .info("Found what we have been looking for. Failover EM should be started");
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void startSecondaryLocalEM() {

        LOGGER.info("About to start Failover LOCAL EM from {}" + context.getdir());
        String command =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? new String("Introscope_Enterprise_Manager2.exe")
                : new String("./Introscope_Enterprise_Manager2");
        String startCommand = Os.isFamily(Os.FAMILY_WINDOWS) ?context.getdir() + "\\" + command :context.getdir() + "/" + command ;

        ProcessBuilder pb =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? new ProcessBuilder("cmd.exe", "/C", startCommand)
                : new ProcessBuilder(startCommand);

        pb.redirectErrorStream(true);
        Process subprocess;
        try {
            subprocess = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = subprocess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader)) {

            String textToMatch =
                "The Introscope Enterprise Manager is configured as a Secondary EM";
            String line;
            while ((line = br.readLine()) != null) {
                LOGGER.info(line);
                if (line.contains(textToMatch)) {
                    LOGGER
                        .info("Found what we have been looking for. Failover LOCAL EM should be started");
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    protected void stopSecondaryEM() {

        LOGGER.info("About to STOP Failover EM from {}" + context.getdir());
        
        List<String> shutdownCommand = new ArrayList<String>();
        if(Os.isFamily(Os.FAMILY_WINDOWS)){
        shutdownCommand.add("taskkill /IM Introscope_Enterprise_Manager.exe /F /T");
        }
        else{
            shutdownCommand.add("kill -9 `ps -ef |grep -i \"/mnt/em/./Introscope_Enterprise_Manager.lax\" |cut -d\" \" -f6,7|head -n 1`"); 
        }

        try {
            ApmbaseUtil.invokeProcessBuilder(shutdownCommand);
        } catch (Exception e) {
            LOGGER.error("Unable to Kill the Failover EM Process");
            e.printStackTrace();
        }
    }

    protected void stopSecondaryLocalEM() {

        LOGGER.info("About to STOP Failover LOCAL EM from {}" + context.getdir());
        
        List<String> shutdownCommand = new ArrayList<String>();
        if(Os.isFamily(Os.FAMILY_WINDOWS)){
            shutdownCommand.add("taskkill /IM Introscope_Enterprise_Manager2.exe /F /T");
            }
            else{
                shutdownCommand.add("kill -9 `ps -ef |grep -i \"/mnt/em/./Introscope_Enterprise_Manager2.lax\" |cut -d\" \" -f6,7|head -n 1"); 
            }

        try {
            ApmbaseUtil.invokeProcessBuilder(shutdownCommand);
        } catch (Exception e) {
            LOGGER.error("Unable to Kill the Failover Local EM Process");
            e.printStackTrace();
        }
    }
    
    //To Start Second Primary instance when there exists multiple Primary EMs
    protected void startSecondPrimaryEM() {

        LOGGER.info("About to start Second Primary EM from {}", context.getdir());
        String command =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? "Introscope_Enterprise_Manager.exe"
                : "./Introscope_Enterprise_Manager";
        String startCommand = context.getdir() + "/" + command;

        ProcessBuilder pb =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? new ProcessBuilder("cmd.exe", "/C", startCommand)
                : new ProcessBuilder(startCommand);

        pb.redirectErrorStream(true);
        Process subprocess;
        try {
            subprocess = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = subprocess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader)) {

            String emPrimaryConfigMsg = ApmbaseConstants.emFailoverPrimaryConfigurationMessage;
            String emPrimaryLockMsg = ApmbaseConstants.primaryLockMessage;
            String line;
            boolean textFound = false;
            while ((line = br.readLine()) != null) {
                LOGGER.info(line);
                if (line.contains(emPrimaryConfigMsg)) {
                    LOGGER
                        .info("Found the em Failover configuration message, checking for lock message");
                    textFound = true;
                }
                
                if (textFound && line.contains(emPrimaryLockMsg)) {                	
                    LOGGER
                        .info("Found the lock message");
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    //To Start Primary instance when there exists multiple Primary EMs
    protected void startPrimaryEMWithoutLock() {

        LOGGER.info("About to start Primary EM when both Primary and Secondary locks are acquired  "
        		+ "from {}", context.getdir());
        String command =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? "Introscope_Enterprise_Manager.exe"
                : "./Introscope_Enterprise_Manager";
        String startCommand = context.getdir() + "/" + command;

        ProcessBuilder pb =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? new ProcessBuilder("cmd.exe", "/C", startCommand)
                : new ProcessBuilder(startCommand);

        pb.redirectErrorStream(true);
        Process subprocess;
        try {
            subprocess = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = subprocess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader)) {

            String emPrimaryConfigMsg = ApmbaseConstants.emFailoverPrimaryConfigurationMessage;
            String emSecondaryLockMsg  = ApmbaseConstants.secondaryLockMessage;
            String line;
            boolean textFound = false;
            while ((line = br.readLine()) != null) {
                LOGGER.info(line);
                if (line.contains(emPrimaryConfigMsg)) {
                    LOGGER
                        .info("Found the em Failover configuration message, checking for lock message");
                    textFound = true;
                }
                
                if (textFound && line.contains(emSecondaryLockMsg)) {
                    LOGGER
                        .info("Found the lock message");
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    
    
    //To Start Second Primary instance first to be in running status when there exists multiple Primary EMs
    protected void startSecondPrimaryEMFirst() {

        LOGGER.info("About to start Failover Primary EM first from {}", context.getdir());
        String command =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? "Introscope_Enterprise_Manager.exe"
                : "./Introscope_Enterprise_Manager";
        String startCommand = context.getdir() + "/" + command;

        ProcessBuilder pb =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? new ProcessBuilder("cmd.exe", "/C", startCommand)
                : new ProcessBuilder(startCommand);

        pb.redirectErrorStream(true);
        Process subprocess;
        try {
            subprocess = pb.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (InputStream inputStream = subprocess.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(inputStreamReader)) {

            String emPrimaryConfigMsg = ApmbaseConstants.emFailoverPrimaryConfigurationMessage;
            String emPrimaryLockMsg = ApmbaseConstants.primaryLockMessage;
            String emPrimaryLogMessage = ApmbaseConstants.primaryEmLogMessage;
            String line;
            boolean configTextFound = false;
            boolean lockTextFound = false;
            while ((line = br.readLine()) != null) {
                LOGGER.info(line);
                if (line.contains(emPrimaryConfigMsg)) {
                    LOGGER
                        .info("Found the em Failover configuration message, checking for lock message");
                    configTextFound = true;
                }
                
                if (configTextFound && line.contains(emPrimaryLockMsg)) {
                    LOGGER
                        .info("Found the lock message");
                    lockTextFound = true;                    
                }
                
                if (lockTextFound && line.contains(emPrimaryLogMessage)) {
                    LOGGER
                        .info("Found the primary  EM status running message");
                    break;                    
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    

}
