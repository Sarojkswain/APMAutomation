/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.jboss;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.springframework.beans.factory.InitializingBean;

import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.ServerStatus;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.jboss.JBossPluginConfiguration.ServerConfig;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

/**
 * @author filja01
 *
 */
public class JBossPluginImpl extends AbstractAppServerPluginImpl<JBossPluginConfiguration> implements JBossPlugin, InitializingBean {

//    private boolean isAgentSet = false;
    
    public JBossPluginImpl() {
        super(PLUGIN, JBossPluginConfiguration.class);
    }
    

    @Override
    public void afterPropertiesSet() throws Exception {
        JBossPluginConfiguration cfg = readConfiguration();
        info("Loaded JBossPlugin config: " + cfg);
    }
    

    @Override
    protected JBossPluginConfiguration readConfiguration() {
        JBossPluginConfiguration cfg = configurationManager.loadPluginConfiguration(PLUGIN, JBossPluginConfiguration.class);
        return cfg;
    }
    
    
    @Override
    protected void saveConfiguration(JBossPluginConfiguration config) {
        configurationManager.savePluginConfiguration(PLUGIN, config);
        debug("Saved JBossPlugin config");
    }
    

    @Override
    @ExposeMethod(description = "Starts the jboss server instance. Returns true if server was started with return code 0")
    public boolean startServer(String serverId, boolean withAgent) {
        JBossPluginConfiguration cfg = configurationManager.loadPluginConfiguration(PLUGIN, JBossPluginConfiguration.class);
        info("Starting server " + serverId);
        
        ServerConfig serverConfig = (ServerConfig) cfg.getServers().get(serverId);
        if (serverConfig == null) {
            warn("Unknown server instance: " + serverId);
            return false;
        }
        
        if (withAgent && !serverConfig.agentInstalled) {
            warn("Start with agent requested - agent is not installed!");
            return false;
        }
        
        switch (serverConfig.version) {
            case JBossAS6_1:
                return startJBoss6(cfg, serverConfig, withAgent);
            case JBossAS7_1:
                return startJBoss7(cfg, serverConfig, withAgent);
            default:
                warn("Unknown version of JBoss - cannot start");
                return false;
        }
        
        
//        File standaloneConf = new File(config.jbossInstallDir + "/bin/standalone.conf.bat");
//        File standaloneConfJBoss = new File(config.jbossInstallDir + "/bin/standaloneAgent.conf.bat");
//        
//        if (isAgentSet || !StringUtils.isBlank(config.jbossInstallDir)) {
//            createStandaloneConfig(standaloneConf, standaloneConfJBoss, config);
//        } else {
//            try {
//            FileUtils.copyFile(standaloneConf, standaloneConfJBoss);
//            } catch (IOException e) {
//                throw ErrorUtils.logExceptionAndWrap(log, e, "Cannot copy standalone.conf.bat file.");
//            }
//        }
//            
//        ProcessBuilder ps =
//                ProcessUtils.newProcessBuilder().command("cmd", "/c", config.jbossInstallDir + "/bin/standalone.bat")
//                        .directory(new File(config.jbossInstallDir + "/bin")).redirectErrorStream(true)
//                        .redirectOutput(Redirect.appendTo(new File(config.logs, "start.txt")));
//        
//        ps.environment()
//                .put("STANDALONE_CONF", standaloneConfJBoss.getPath());
//      //set java enviroment because JBoss run only on Java 6 or Java 7
//        if (!StringUtils.isBlank(config.envJava)) {
//            ps.environment()
//                .put("JAVA_HOME", config.envJava);
//        }
//        
//        ProcessUtils.startProcess(ps);
//        return true;
    }
    
    
    private boolean startJBoss6(JBossPluginConfiguration cfg, ServerConfig serverConfig, boolean withAgent) {
        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        switch (osFamily) {
            case Windows:
                // TODO automate creation of runWithAgent.bat
                Path binPath = Paths.get(serverConfig.baseDir, "bin");
                Path runBat = Paths.get(serverConfig.baseDir, "bin", "run.bat");
                if (withAgent) {
                    runBat = Paths.get(serverConfig.baseDir, "bin", "runWithIntroscopeRemote.bat");
                }
                Path logFilePath = Paths.get(serverConfig.baseDir, "server", serverConfig.variant, "log");
                String logsDir = logFilePath.toString();
                boolean exists = logFilePath.toFile().exists();
                System.out.println("log file path exists: "+ exists);
                serverConfig.status = ServerStatus.Starting;
                File logFile = new File(logFilePath.toFile(), "start.txt");
                exists = logFile.exists();
                configurationManager.savePluginConfiguration(PLUGIN, cfg);
                
                ProcessBuilder pb = ProcessUtils.newProcessBuilder();
                pb.command("cmd.exe", "/c", runBat.toString())
                    .directory(binPath.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(Redirect.to(new File(logsDir, "start.txt")))
                    ;
                pb.environment().put("NOPAUSE", "true");
                Process process = ProcessUtils.startProcess(pb);
                return waitForServerStart(process, cfg, serverConfig);
            default:
                warn("Operating system family " + osFamily + " not yet supported");
                return false;
        }
    }
    

    private boolean startJBoss7(JBossPluginConfiguration cfg, ServerConfig serverConfig, boolean withAgent) {
        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        switch (osFamily) {
            case Windows:
                Path binPath = Paths.get(serverConfig.baseDir, "bin");
                Path runBat = Paths.get(serverConfig.baseDir, "bin", "standalone.bat");
                if (withAgent) {
                    runBat = Paths.get(serverConfig.baseDir, "bin", "runWithIntroscopeRemote.bat");
                }
                Path logFilePath = Paths.get(serverConfig.baseDir, serverConfig.variant, "log");
                String logsDir = logFilePath.toString();
                boolean exists = logFilePath.toFile().exists();
                System.out.println("log file path exists: "+ exists);
                serverConfig.status = ServerStatus.Starting;
                File logFile = new File(logFilePath.toFile(), "start.txt");
                exists = logFile.exists();
                configurationManager.savePluginConfiguration(PLUGIN, cfg);
                
                ProcessBuilder pb = ProcessUtils.newProcessBuilder();
                ArrayList<String> cmd = new ArrayList<String>();
                cmd.add("cmd.exe");
                cmd.add("/c");
                cmd.add(runBat.toString());
                if (serverConfig.extraArgs != null && serverConfig.extraArgs.length() > 0) {
                    StringTokenizer st = new StringTokenizer(serverConfig.extraArgs);
                    while (st.hasMoreTokens()) {
                        String s = st.nextToken();
                        cmd.add(s);
                    }
                }
                
                pb.command(cmd)
                    .directory(binPath.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(Redirect.to(new File(logsDir, "start.txt")))
                    ;
                pb.environment().put("NOPAUSE", "true");
                Process process = ProcessUtils.startProcess(pb);
                return waitForServerStart(process, cfg, serverConfig);
            default:
                warn("Operating system family " + osFamily + " not yet supported");
                return false;
        }
    }
    
    
    private boolean waitForServerStart(final Process process, JBossPluginConfiguration cfg, final ServerConfig serverConfig) {
        final Object lock = new Object();
        final boolean retval[] = new boolean[] {false};
        
        synchronized (lock) {
            Thread th = new Thread(new Runnable() {
                private long started = 0;
                
                @Override
                public void run() {
                    started = System.currentTimeMillis();
                    boolean done = false;
                    while (!done) {
                        try {
                            
                            Thread.sleep(10000L);
                        } catch (InterruptedException e) {
                        }
                        int exitCode = -1;
                        try {
                            exitCode = process.exitValue();
                            serverConfig.status = ServerStatus.Error;
                            warn("Server start script exited with code: " + exitCode);
                            break;
                        } catch (Exception e) {
                            // not to worry
                        }
                        done = isServerRunning("http://localhost:" + serverConfig.httpPort, 10000);
                        long elapsed = System.currentTimeMillis() - started;
                        if (done) {
                            serverConfig.status = ServerStatus.Started;
                            retval[0] = true;
                        } else if (elapsed > serverConfig.startupTimeout) {
                            serverConfig.status = ServerStatus.Timeout;
                            warn("Server did not respond on port " + serverConfig.httpPort + " withing " 
                                + serverConfig.startupTimeout + " ms");
                            break;
                        }
                    }
                    synchronized (lock) {
                        lock.notifyAll();
                    }
                }
            });
            th.start();
            try {
                lock.wait();
            } catch (InterruptedException e) {
                warn("Unable to wait for process to start");
            }
        }
        configurationManager.savePluginConfiguration(PLUGIN, cfg);
        return retval[0];
    }
    
    
    
//    /**
//     * Create own standalone.conf.bat
//     * 
//     * @param standaloneConf source config file
//     * @param standaloneConfJBoss destination config file
//     * @param config plugin configuration
//     */
//    private void createStandaloneConfig(File standaloneConf, File standaloneConfJBoss, InstallationParameters config) {
//        try {
//            BufferedReader reader = new BufferedReader(new FileReader(standaloneConf));
//            List<String> fileLst = IOUtils.readLines(reader);
//            reader.close();
//            ListIterator<String> lIt = fileLst.listIterator();
//            
//            Path agentJar = Paths.get(config.agentInstallDir + AGENT_JAR_PATH_REL);
//            Path agentProfile = Paths.get(config.agentInstallDir + AGENT_PROFILE_PATH_REL);
//            
//            String setMyAgent = "set \"JAVA_OPTS=%JAVA_OPTS% -javaagent:" 
//                                + agentJar + " -Dcom.wily.introscope.agentProfile=" + agentProfile + "\"";
//            
//            boolean setIt = false;
//            while (lIt.hasNext()) {
//                String line = lIt.next();
//                boolean chng = false;
//                if (isAgentSet) {
//                    if (line.matches(".*-Xms[0-9]+[Mm].*")) {
//                        line = line.replaceAll("-Xms[0-9]+[Mm]", "-Xms1024m");
//                        chng = true;
//                    } 
//                    if (line.matches(".*-Xmx[0-9]+[Mm].*")) {
//                        line = line.replaceAll("-Xmx[0-9]+[Mm]", "-Xmx1024m");
//                        chng = true;
//                    } 
//                    if (line.matches(".*-XX:MaxPermSize=[0-9]+[Mm].*")) {
//                        line = line.replaceAll("-XX:MaxPermSize=[0-9]+[Mm]", "-XX:MaxPermSize=256m");
//                        chng = true;
//                    }
//                    if (line.contains("-Djboss.modules.system.pkgs=org.jboss.byteman")) {
//                        line = line.replace("-Djboss.modules.system.pkgs=org.jboss.byteman", 
//                                            "-Djboss.modules.system.pkgs=org.jboss.byteman,com.wily,com.wily.*");
//                        chng = true;
//                    } 
//                    if (line.contains(":JAVA_OPTS_SET")) {
//                        lIt.previous();
//                        lIt.add(setMyAgent);
//                        lIt.next();
//                        setIt = true;
//                    } 
//                }
//                if (line.matches(".*-Djboss\\.server\\.default\\.config=[\\S]+\\.xml.*") 
//                        && (!StringUtils.isBlank(config.jbossConfigFile))) {
//                    line = line.replaceAll("-Djboss\\.server\\.default\\.config=[a-zA-Z]+\\.xml", 
//                                    "-Djboss.server.default.config="+config.jbossConfigFile);
//                    chng = true;
//                }
//                if (chng)
//                lIt.set(line);
//            }
//            if (isAgentSet && !setIt) {
//                lIt.add(setMyAgent);
//            }
//            
//            BufferedWriter writer = new BufferedWriter(new FileWriter(standaloneConfJBoss));
//            IOUtils.writeLines(fileLst, null, writer);
//            writer.close();
//        } catch (IOException e) {
//            throw ErrorUtils.logExceptionAndWrap(log, e, "Cannot edit standalone.conf.bat file.");
//        }   
//    }

    @Override
    @ExposeMethod(description = "Stops the jboss server. Returns true if server was stopped with return code 0")
    public boolean stopServer(String serverId) {
        JBossPluginConfiguration cfg = configurationManager.loadPluginConfiguration(PLUGIN, JBossPluginConfiguration.class);
        info("Stopping server " + serverId);
        
        ServerConfig serverConfig = (ServerConfig) cfg.getServers().get(serverId);
        if (serverConfig == null) {
            warn("Unknown server instance: " + serverId);
            return false;
        }
        
        switch (serverConfig.version) {
            case JBossAS6_1:
                return stopJBoss6(cfg, serverConfig);
            case JBossAS7_1:
                warn("JBoss7 not implemented yet");
                return false;
            default:
                warn("Unknown version of JBoss - cannot stop");
                return false;
        }

        /*
        ProcessBuilder ps =
                ProcessUtils.newProcessBuilder().command("cmd", "/c", config.jbossInstallDir + "/bin/jboss-cli.bat --connect \":shutdown\"")
                        .directory(new File(config.jbossInstallDir + "/bin")).redirectErrorStream(true)
                        .redirectOutput(Redirect.appendTo(new File(config.logs, "stop.txt")));

        ProcessUtils.startProcess(ps);
        */
    }
    
    
    private boolean stopJBoss6(JBossPluginConfiguration cfg, ServerConfig serverConfig) {
        OperatingSystemFamily osFamily = SystemUtil.getOsFamily();
        switch (osFamily) {
            case Windows:
                ProcessBuilder pb = ProcessUtils.newProcessBuilder();
                String shutdownFile = serverConfig.baseDir + "/bin/shutdown.bat";
                String logsDir = serverConfig.baseDir + "/server/" + serverConfig.variant + "/log";
                serverConfig.status = ServerStatus.Starting;
                configurationManager.savePluginConfiguration(PLUGIN, cfg);
                pb.command("cmd", "/c", shutdownFile, "-S", "--port=" + serverConfig.rmiPort)
                    .redirectErrorStream(true)
                    .redirectOutput(Redirect.appendTo(new File(logsDir, "shutdown.txt")));
                pb.environment().put("NOPAUSE", "true");
                ProcessUtils.startProcess(pb);
                serverConfig.status = ServerStatus.Stopping;
                configurationManager.savePluginConfiguration(PLUGIN, cfg);
                boolean stopped = isServerStopped("http://localhost:" + serverConfig.httpPort, 300000);
                if (stopped) {
                    serverConfig.status = ServerStatus.Stopped;
                }
                configurationManager.savePluginConfiguration(PLUGIN, cfg);
                return stopped;
            default:
                warn("Operating system family " + osFamily + " not yet supported");
                return false;
        }
    }
    

    @Override
    protected String getAppServerName() {
        return "jboss";
    }



    @Override
    protected String createStartScript(AppServerConfiguration serverConfig) {
        if (!(serverConfig instanceof ServerConfig)) {
            return null;
        }
        
        ServerConfig jbossConfig = (ServerConfig) serverConfig;
        switch (jbossConfig.version) {
            case JBossAS6_1:
                // copy run.bat to runWithIntroscope.bat
                break;
            case JBossAS7_1:
                // copy standalone.cmd to runStandaloneWithIntroscope.cmd
                break;
        }
        
        return null;
    }
    
    
    @Override
    protected AppServerConfiguration createServerConfiguration(String serverId) {
        JBossPluginConfiguration cfg = readConfiguration();
        ServerConfig sc = new ServerConfig();
        sc.id = serverId;
        cfg.addServerConfig(serverId, sc);
        saveConfiguration(cfg);
        return sc;
    }
}
