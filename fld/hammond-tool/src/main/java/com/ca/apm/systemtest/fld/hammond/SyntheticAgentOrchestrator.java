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
 */
package com.ca.apm.systemtest.fld.hammond;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;

import org.eclipse.equinox.weaving.internal.caching.Log;

import com.wily.introscope.agent.KAgentConstants;
import com.wily.introscope.install.KIntroscopeConfigConstants;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.isengard.IsengardException;
import com.wily.util.feedback.ApplicationFeedback;

public class SyntheticAgentOrchestrator {

    protected static final Random rand = new Random(1449224256546L);

    private ArrayList<SyntheticAgent> agents = new ArrayList<>();

    private String agentCredential;
    private String collectorHostName;

    public void setAgentCredential(String agentCredential) {
        
        this.agentCredential = agentCredential;
    }
    public void setCollectorHost(String hostName) {
        this.collectorHostName = hostName;
    }

    public void createAgents(int agentsCount) {
        try {
            for (int i = 0; i < agentsCount; i++) {
                agents.add(createAgent(AgentName.getAgentName("Superdomain|host" + i + "|process|name")));
            }
        } catch (BadlyFormedNameException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createTempConfig(String agentHost, String collectorHost, String agentCredential) throws IOException {
        Path tempFile = Files.createTempFile("agent", ".properties");
        // Path tempFile = new File("c:\\foo\\agent-config-" + count++ + ".properties").toPath();
        Properties prop = new Properties();

        if (agentHost != null && !agentHost.isEmpty()) {
            prop.put(KAgentConstants.kHostNameKey, agentHost);
        }

        String logDir = System.getProperty("agent.log.dir");
        if (logDir != null) {
            prop.put("log4j.logger.IntroscopeAgent", "DEBUG, logfile");
            prop.put("log4j.appender.logfile.File", "c:/foo/agent.log");
            prop.put("log4j.additivity.IntroscopeAgent", "false");
            prop.put("log4j.appender.console", "com.wily.org.apache.log4j.ConsoleAppender");
            prop.put("log4j.appender.console.layout", "com.wily.org.apache.log4j.PatternLayout");
            prop.put("log4j.appender.console.layout.ConversionPattern",
                "%d{M/dd/yy hh:mm:ss a z} [%-3p] [%c] %m%n");
            prop.put("log4j.appender.console.target", "System.err");
            prop.put("log4j.appender.logfile",
                "com.wily.introscope.agent.AutoNamingRollingFileAppender");
            prop.put("log4j.appender.logfile.layout", "com.wily.org.apache.log4j.PatternLayout");
            prop.put("log4j.appender.logfile.layout.ConversionPattern",
                "%d{M/dd/yy hh:mm:ss a z} [%-3p] [%c] %m%n");
            prop.put("log4j.appender.logfile.MaxBackupIndex", "4");
            prop.put("log4j.appender.logfile.MaxFileSize", "2MB");
        }

        // max agent metric clamp orginal value is 50000
        prop.put("introscope.agent.metricClamp", "100000000");
        
        if(collectorHost != null && collectorHost.startsWith("http")) {
            
            prop.put(KIntroscopeConfigConstants.kAgentEMServerConnectionUrlPropertyKey+".1", collectorHost);
            
        } else if (collectorHost != null && !collectorHost.isEmpty()) {
            prop.put(KIntroscopeConfigConstants.kAgentServerConnectionOrderPropertyKey,
                collectorHost);
            prop.put(KIntroscopeConfigConstants.kAgentServerTCPPortPropertyKey + "."
                + collectorHost, "5001");
            prop.put(KIntroscopeConfigConstants.kAgentServerTCPHostnamePropertyKey + "."
                + collectorHost, InetAddress.getByName(collectorHost).getCanonicalHostName());
        }
        if(agentCredential != null && !agentCredential.isEmpty()) {
            
            prop.put(KIntroscopeConfigConstants.kAgentEMServerCredentialShortPropertyKey, agentCredential);
        }

        prop.store(Files.newOutputStream(tempFile, StandardOpenOption.CREATE), "");
        System.setProperty("com.wily.introscope.agentProfile", tempFile.toString());
    }

    private void deleteTempConfig() throws IOException {
        String fileName = System.setProperty("com.wily.introscope.agentProfile", "");

        if (fileName != null && !fileName.isEmpty()) {
            Files.delete(Paths.get(fileName));
        }
    }

    private SyntheticAgent createAgent(AgentName agentName)
        throws IOException, BadlyFormedNameException {
        SyntheticAgent result;

        createTempConfig(agentName.getHost(), collectorHostName, agentCredential);

        ApplicationFeedback feedback =
            Configuration.instance().createFeedback(agentName.getProcessURLWithoutDomain());
        result = new SyntheticAgent(feedback, agentName);

        deleteTempConfig();

        return result;
    }

    public boolean startPlayback() {

        if (agents.isEmpty()) {
            return false;
        }
        
        try {
            createTempConfig(null, collectorHostName, agentCredential);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        for (SyntheticAgent agent : agents) {
            try {
                agent.start();
                Thread.sleep(rand.nextInt(1000));
            } catch (InterruptedException | BadlyFormedNameException | IsengardException | IOException e) {
                Log.error(String.format("Cannot start agent '%s'", agent.getName()), e);
                return false;
            }
        }
        return true;
    }

}
