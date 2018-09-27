/*
 * Copyright (c) 2017 CA. All rights reserved.
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
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import com.ca.apm.systemtest.fld.hammond.imp.HammondTables;
import com.wily.introscope.agent.ConcurrentAgentMetricPool;
import com.wily.introscope.agent.KAgentConstants;
import com.wily.introscope.appmap.agent.AppMapService;
import com.wily.introscope.install.KIntroscopeConfigConstants;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.util.Log;
import com.wily.isengard.IsengardException;
import com.wily.util.adt.AAtomicCounter;
import com.wily.util.feedback.ApplicationFeedback;
import org.apache.commons.lang.StringUtils;
import org.apache.http.util.Args;

public class HammondAgentOrchestrator {

    protected static final Random rand = new Random(1449224256546L);

    private List<HammondAgent> agents = new ArrayList<>();
    private String collectorHost;
    private HammondTables hammondData;
    private String variant = "";
    private int groupId = 1;
    private int groupSize = 1;
    private Double agentScale;
    private String[] included;
    private String[] excluded;
    private String agentCredential;

    public void createAgents() throws BadlyFormedNameException, IOException {

        Args.notBlank(collectorHost, "collectorHost");
        Args.notNull(hammondData, "hammondData");

        try {
            Field field = AppMapService.class.getDeclaredField("fConsecutiveProblems");
            field.setAccessible(true);
            field.set(null, new AAtomicCounter());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
            | IllegalAccessException e) {
            e.printStackTrace();
        }

        int index = 0;
        for (AgentName agentName : hammondData.getAgents()) {
            if ((index++ % groupSize) != (groupId - 1)) {
                continue;
            }

            if (agentScale != null && agentScale > 0 && agentScale < 0.999) {
                if (rand.nextDouble() > agentScale) {
                    continue;
                }
                agents.add(createAgent(agentName, variant));
            } else if (agentScale != null && agentScale > 1.001) {
                int size = (int) Math.floor(agentScale);
                size += rand.nextDouble() > (agentScale - size) ? 0 : 1;
                for (int i = 1; i <= size; i++) {
                    agents.add(createAgent(agentName, variant + i));
                }
            } else {
                agents.add(createAgent(agentName, variant));
            }

        }
    }

    private void createTempConfig(String agentHost) throws IOException {
        Path tempFile = Files.createTempFile("agent", ".properties");
        // Path tempFile = new File("c:\\foo\\agent-config-" + count++ + ".properties").toPath();
        Properties prop = new Properties();

        if (agentHost != null && !agentHost.isEmpty()) {
            prop.put(KAgentConstants.kHostNameKey, agentHost);
        }

        prop.put("introscope.agent.metricClamp", "100000000");

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

        if(collectorHost != null && collectorHost.startsWith("http")) {

            prop.put(KIntroscopeConfigConstants.kAgentEMServerConnectionUrlPropertyKey+".1", collectorHost);

        } else if (collectorHost != null && !collectorHost.isEmpty()) {
            String host = collectorHost;
            String port = "5001";
            if (collectorHost.contains(":")) {
                String[] s = collectorHost.split(":");
                if (collectorHost.contains("http")) {
                    if (s.length==3) {
                        host = s[1];
                        port = s[2];
                    }
                }
                else {
                    host = s[0];
                    port = s[1];
                }
            }

            prop.put(KIntroscopeConfigConstants.kAgentServerConnectionOrderPropertyKey,
                    host);
            prop.put(KIntroscopeConfigConstants.kAgentServerTCPPortPropertyKey + "."
                    + host, port);
            prop.put(KIntroscopeConfigConstants.kAgentServerTCPHostnamePropertyKey + "."
                    + host, InetAddress.getByName(host).getCanonicalHostName());
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

    private HammondAgent createAgent(AgentName agentName, String hostSuffix)
        throws IOException, BadlyFormedNameException {

        AgentName newAgentName;
        if (StringUtils.isNotBlank(hostSuffix)) {
            newAgentName =
                AgentName.getAgentName(agentName.getDomain(), agentName.getHost() + hostSuffix,
                    agentName.getProcess(), agentName.getAgentName());
        } else {
            newAgentName = agentName;
        }

        createTempConfig(newAgentName.getHost());

        ApplicationFeedback feedback =
            Configuration.instance().createFeedback(newAgentName.getProcessURLWithoutDomain());
        HammondAgent result = new HammondAgent(feedback, agentName, newAgentName, hostSuffix);

        deleteTempConfig();

        return result;
    }

    public boolean startPlayback(long playMetricsFrom, long playMetricsTo, long playTracesFrom, long playTracesTo) {

        if (agents.isEmpty()) {
            return false;
        }
        
        try {
            createTempConfig(null);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        long playbackStartMillis = System.currentTimeMillis();
        for (HammondAgent agent : agents) {
            long start = System.currentTimeMillis();
            try {
                agent.start(hammondData, playMetricsFrom, playMetricsTo, playTracesFrom, playTracesTo);
                if (!Configuration.instance().isPlayOnce()) {
                    Thread.sleep(rand.nextInt(1500));
                }
            } catch (InterruptedException | BadlyFormedNameException | IsengardException | IOException e) {
                Log.out.error(String.format("Cannot start agent '%s'%s", agent.getName()), e);
                return false;
            }
            long now = System.currentTimeMillis();
            Log.out.info(String.format("Started agent %s in %d ms, total time %d ms", agent.getName(),
                    now - start, now - playbackStartMillis));
        }
        return true;
    }

    private boolean allowAgent(AgentName agentName) {
        if (included.length == 0 && excluded.length == 0) {
            return true;
        }

        boolean incl = included.length == 0;
        boolean excl = false;

        if (agentName.toString().toLowerCase().contains("ignored")) {
            return false;
        }


        String name = agentName.toString().toLowerCase();
        for (String filter : included) {
            if (name.toLowerCase().contains(filter)) {
                incl = true;
            }

        }
        for (String filter : excluded) {
            if (name.toLowerCase().contains(filter)) {
                excl = true;
            }

        }

        return incl && !excl;
    }

    public void setVariant(String variant) {
        this.variant = variant;
    }

    public void setGroup(String group) {
        if (StringUtils.isNotBlank(group)) {
            String[] split = group.split("/");

            try {
                groupId = Integer.parseInt(split[0]);
                groupSize = Integer.parseInt(split[1]);
            } catch (Exception e) {
                groupId = 1;
                groupSize = 1;
                Log.out.warn("Cannot parse group id and group size: '" + group +"'");
            }
        }
    }

    public void setAgentScale(Double agentScale) {
        this.agentScale = agentScale;
    }

    public void setIncluded(String filterList) {
        this.included = parseList(filterList);
    }

    public void setExcluded(String filterList) {
        this.excluded = parseList(filterList);
    }

    private String[] parseList(String filterList) {
        String[] arr = new String[0];
        if (filterList == null) {
            return arr;
        }
        StringTokenizer st = new StringTokenizer(filterList, ",");
        ArrayList<String> list = new ArrayList<>();
        while (st.hasMoreTokens()) {
            String t = st.nextToken().trim();
            if (t.length() == 0) {
                continue;
            }
            list.add(t.toLowerCase());
        }
        arr = list.toArray(arr);

        return arr;
    }

    public void setCollectorHost(String collectorHost) {
        this.collectorHost = collectorHost;
    }

    public void setHammondData(HammondTables hammondData) {
        this.hammondData = hammondData;
    }

    public void setAgentCredential(String agentCredential) {
        this.agentCredential = agentCredential;
    }

}
