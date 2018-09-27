package com.ca.apm.systemtest.fld.hammond;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import com.ca.apm.systemtest.fld.hammond.data.AppmapData;
import com.ca.apm.systemtest.fld.hammond.data.SmartstorData;
import com.ca.apm.systemtest.fld.hammond.data.TransactionsData;
import com.wily.introscope.agent.KAgentConstants;
import com.wily.introscope.appmap.agent.AppMapService;
import com.wily.introscope.install.KIntroscopeConfigConstants;
import com.wily.introscope.spec.metric.AgentName;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.util.Log;
import com.wily.isengard.IsengardException;
import com.wily.util.adt.AAtomicCounter;
import com.wily.util.feedback.ApplicationFeedback;

public class AgentOrchestrator {

    protected static final Random rand = new Random(1449224256546L);

    private ArrayList<Agent> agents = new ArrayList<>();
    private SmartstorData smartstorData;

    private String agentCredential;
    private String collectorHostName;
    private double agentScale;

    private String[] included = new String[0];
    private String[] excluded = new String[0];

    private String prefix = "";

    public void setAgentCredential(String agentCredential) {
        
        this.agentCredential = agentCredential;
    }
    public void setAgentScale(double scale) {
        this.agentScale = scale;

        if (smartstorData != null) {
            agents.clear();
            createAgents(smartstorData);
        }
    }

    public void setCollectorHost(String hostName) {
        this.collectorHostName = hostName;
    }

    public void createAgents(SmartstorData smartstorData) {
        this.smartstorData = smartstorData;


        try {
            Field field = AppMapService.class.getDeclaredField("fConsecutiveProblems");
            field.setAccessible(true);
            field.set(null, new AAtomicCounter());
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
            | IllegalAccessException e) {
            e.printStackTrace();
        }

        for (Integer key : smartstorData.getAgentKeys()) {
            AgentName agentName = smartstorData.getAgent(key);
            if (!allowAgent(agentName)) {
                continue;
            }

            try {
                if (agentScale < 1.0) {
                    if (rand.nextDouble() > agentScale) {
                        continue;
                    }
                    agents.add(createAgent(agentName, prefix, key));
                } else if (agentScale > 1.0) {
                    int size = (int) Math.floor(agentScale);
                    size += rand.nextDouble() > (agentScale - size) ? 0 : 1;
                    for (int i = 1; i <= size; i++) {
                        agents.add(createAgent(agentName, prefix + i, key));
                    }
                } else {
                    agents.add(createAgent(agentName, prefix, key));
                }
            } catch (BadlyFormedNameException | IOException ex) {

            }
        }
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

    private Agent createAgent(AgentName agentName, String variant, int agentKey)
        throws IOException, BadlyFormedNameException {
        Agent result;

        AgentName newAgentName;
        if (!variant.isEmpty()) {
            newAgentName =
                AgentName.getAgentName(agentName.getDomain(), agentName.getHost() + variant,
                    agentName.getProcess(), agentName.getAgentName());
        } else {
            newAgentName = agentName;
        }

        createTempConfig(newAgentName.getHost(), collectorHostName, agentCredential);

        ApplicationFeedback feedback =
            Configuration.instance().createFeedback(newAgentName.getProcessURLWithoutDomain());
        result = new Agent(feedback, agentKey, agentName, newAgentName, variant);

        deleteTempConfig();

        return result;
    }

    public boolean startPlayback(SmartstorData smartstorData, TransactionsData transactionsData,
        AppmapData appmapData) {

        if (agents.isEmpty()) {
            return false;
        }
        
        try {
            createTempConfig(null, collectorHostName, agentCredential);
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        long playbackStartMillis = System.currentTimeMillis();
        
        for (Agent agent : agents) {
            long start = System.currentTimeMillis();
            try {
                agent.start(smartstorData, transactionsData, appmapData, playbackStartMillis);
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

    public void setPrefix(String prefix) {
        if (prefix == null) {
            this.prefix = "";
        } else {
            this.prefix = prefix;
        }
    }

    public void setIncluded(String filterList) {
        included = parseList(filterList);
    }

    public void setExcluded(String filterList) {
        excluded = parseList(filterList);
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
}
