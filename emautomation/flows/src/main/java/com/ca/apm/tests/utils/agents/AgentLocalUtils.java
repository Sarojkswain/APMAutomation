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
 */


package com.ca.apm.tests.utils.agents;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.tests.utils.configutils.PropertiesUtility;
import com.ca.apm.tests.utils.emutils.EmBatLocalUtils;

/**
 * Class for using agent on some machine, uses agent properties utility class to manage agent
 * profile properties.
 * 
 * 
 * @author sobar03
 *
 */
public class AgentLocalUtils {



    private String serverPath;

    private String configPath;

    private String logPath;


    private static final Logger log = LoggerFactory.getLogger(AgentLocalUtils.class);


    public final int COMMAND_TIMEOUT = 10 * 60 * 1000;



    /**
     * Utilities constructor, sets server path and also using it sets config and logs
     * paths.
     * 
     * @param serverPath
     */
    public AgentLocalUtils(String serverPath) {
        this.serverPath = serverPath;
        this.configPath =
            serverPath + File.separator + "wily" + File.separator + "core" + File.separator
                + "config";
        this.logPath =
            serverPath + File.separator + "wily" + File.separator + "logs" + File.separator
                + "IntroscopeAgent.log";
    }


    /**
     * Sets properties in given profile<br>
     * Will fail if properties are not found in file.
     * 
     * 
     * @param properties
     * @param profileName
     * @throws Exception
     */
    public void setPropertiesInProfile(Map<String, String> properties, String profileName)
        throws Exception {

        File fl = new File(configPath);

        File[] files = fl.listFiles();

        for (File current : files) {
            if (current.getName().equals(profileName)) {
                PropertiesUtility.saveProperties(current.getAbsolutePath(), properties, true);
            }
        }

    }


    /**
     * Looks through log and checks if some keyword appears in it.
     * 
     * 
     * @param keyword
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    public boolean isKeywordInLog(String keyword) throws FileNotFoundException, IOException {

        log.info("Looking for " + keyword + " in " + logPath);
        return EmBatLocalUtils.isKeywordInLog(logPath, keyword);
    }



    /**
     * Waits for some keyword in log file to appear, check is repeated every second.<br>
     * There is 100[ms] delay while it goes to end of file and starts waiting
     * for keyword.<br>
     * So old logs that existed before method launch are ignored.
     * 
     * @author sobar03
     * @throws InterruptedException
     * @throws IOException
     * @throws FileNotFoundException
     * 
     */
    public void waitForKeywordInLog(String keyword, Long timeout) throws InterruptedException,
        FileNotFoundException, IOException {
        log.info("Waiting for " + keyword + " in " + logPath);
        EmBatLocalUtils.waitForKeywordInLog(logPath, keyword, timeout);
    }

    /**
     * Returns server path of this agent utility
     */
    public String getServerPath() {
        return serverPath;
    }

    /**
     * Returns profile path of this agent utility
     */
    public String getProfilePath() {
        return configPath + File.separator + "IntroscopeAgent.profile";
    }

    /**
     * Returns profile path of this agent utility
     */
    public String getLogPath() {
        return serverPath + File.separator + "wily" + File.separator + "logs" + File.separator
            + "IntroscopeAgent.log";
    }

    /**
     * This method enable secured communication of EM
     * 
     * @param path - EM config file
     * @throws Exception
     */
    public void setUpHttpsProperties() throws Exception {
        HashMap<String, String> propertiesToSet = new HashMap<String, String>();
        propertiesToSet.put(
            "introscope.agent.enterprisemanager.transport.tcp.socketfactory.DEFAULT",
            "com.wily.isengard.postofficehub.link.net.HttpsTunnelingSocketFactory");
        propertiesToSet
            .put("introscope.agent.enterprisemanager.transport.tcp.port.DEFAULT", "8444");
        PropertiesUtility.saveProperties(getProfilePath(), propertiesToSet, true);

    }


}
