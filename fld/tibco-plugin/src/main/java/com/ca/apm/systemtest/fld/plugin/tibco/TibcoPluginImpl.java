package com.ca.apm.systemtest.fld.plugin.tibco;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl;
import com.ca.apm.systemtest.fld.plugin.AgentInstallException;
import com.ca.apm.systemtest.fld.plugin.AppServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.tibco.TibcoPluginConfiguration.TibcoServerConfiguration;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil;
import org.apache.commons.configuration.ConfigurationException;
import org.codehaus.plexus.util.FileUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Plugin that has common code for Tibco BW server
 *
 * @author rsssa02.
 */

@PluginAnnotationComponent(pluginType = TibcoPlugin.TIBCO_PLUGIN)
@Qualifier(TibcoPlugin.TIBCO_PLUGIN)

public class TibcoPluginImpl extends AbstractAppServerPluginImpl<TibcoPluginConfiguration>
        implements TibcoPlugin {
    public static final String AGENT_OPTS           = "java.extended.properties"; //for javaagent params
    public static final String SET_HEAP_STR         = "java.heap.size.max"; //for heap settings
    public static final String TIB_HTTP_MIN_PROC    = "bw.plugin.http.server.minProcessors"; // server threads need to be increased to make sure load runs smooth
    public static final String TIB_HTTP_MAX_PROC    = "bw.plugin.http.server.maxProcessors"; // always try to mainatin min == max
    public static final String DEFAULT_HTTP_SIZE    = "1200"; // total number of server thread to process http requests
    public static final String TRADE_TRA_ORIGINAL   = ".ORIGINAL";
    public static final long START_THREAD_BUFFER    = 30000;

    public TibcoPluginImpl() {
        super(TibcoPlugin.TIBCO_PLUGIN, TibcoPluginConfiguration.class);
    }

    @Override
    @ExposeMethod(description = "to create startup script")
    protected String createStartScript(AppServerConfiguration serverConfig) {
        return null;
    }

    @Override
    @ExposeMethod(description = "returns default as serverName")
    protected String getAppServerName() {
        return "default";
    }

    /* (non-Javadoc)
      * @see com.ca.apm.systemtest.fld.plugin.AbstractAppServerPluginImpl#createServerConfiguration(java.lang.String)
      */
    @Override
    @ExposeMethod(description = "reads and creates needed configuration from tibco config file")
    protected AppServerConfiguration createServerConfiguration(String serverId) {
        TibcoPluginConfiguration cfg = readConfiguration();
        TibcoServerConfiguration sc = new TibcoServerConfiguration();
        sc.id = serverId;
        cfg.addServerConfig(serverId, sc);
        saveConfiguration(cfg);
        return sc;
    }


    @Override
    public void unsetAgent(Configuration config) {
        //TODO implement when necessary
    }

    @Override
    public void setAgent(Configuration config) {
        //TODO implement when necessary
    }

    /**
     * sets up the agent in new mode (no legacy for performance run). configures Agent parameters and heap settings.
     * @param serverId
     * @param unConfigure
     * @param legacy     - boolean flag to use legacy .NoRedef.profile
     */
    public void setupAgent(String serverId, boolean unConfigure, boolean legacy) {
        TibcoPluginConfiguration pluginConfig = readConfiguration();
        TibcoServerConfiguration serverConfig = pluginConfig.getServerConfig(serverId);
        try {
            SystemUtil.OperatingSystemFamily osfam = SystemUtil.getOsFamily();
            switch (osfam) {
                case Windows: {
                    setupWindowsStartup(serverConfig, unConfigure);
                    break;
                }
                default: {
                    throw new AgentInstallException("Operating system " + osfam + " not yet supported");
                }
            }
        }
        catch (Exception e) {
            if (e instanceof AgentInstallException) {
                throw (AgentInstallException) e;
            }
            throw new AgentInstallException("Unable to configure startup script", e);
        }
    }

    private void setupWindowsStartup(TibcoServerConfiguration serverConfig, boolean unConfigure) {
        try {
            Map<String, String> traPropsMap = new LinkedHashMap<>();
            if(!unConfigure) {
                String agentFormattedStr = "-javaagent:"
                        + serverConfig.baseDir.replace("\\","/")
                        + AGENT_JAR_PATH_REL + " -Dcom.wily.introscope.agentProfile="
                        + serverConfig.baseDir.replace("\\","/") + AGENT_PROFILE_PATH_REL;

                traPropsMap.put(AGENT_OPTS,agentFormattedStr);
            }
            //has to be set in agent config tibcoPlugin.conf.json
            traPropsMap.put(SET_HEAP_STR, serverConfig.getTibHeapSzie() + "M");
            // giving the flexibility of configuring the tomcat http threads size
            // ex: "tibHTTPThreadSize" : "1200"
            if(serverConfig.getTibHTTPThreadSize() == null) {
                traPropsMap.put(TIB_HTTP_MIN_PROC, DEFAULT_HTTP_SIZE);
                traPropsMap.put(TIB_HTTP_MAX_PROC, DEFAULT_HTTP_SIZE);
            } else {
                traPropsMap.put(TIB_HTTP_MIN_PROC, serverConfig.getTibHTTPThreadSize());
                traPropsMap.put(TIB_HTTP_MAX_PROC, serverConfig.getTibHTTPThreadSize());
            }

            //Adding the needed properties to TRA file
            configurePropertiesFile(serverConfig.getTibTradeDir() + "/"
                    + serverConfig.getTibTraProcessName(),traPropsMap,false);

        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    @ExposeMethod(description = "Starts tibcobw engine")
    public boolean startServer(Configuration config) {
        return false;
    }

    /**
     * Start the needed services and trade app using bwengine.exe process
     * @param serverId - Server ID to decide which conf file to pickup from agent.
     */
    @Override
    @ExposeMethod(description = "Starts tibcobw engine")
    public void startAppServer(String serverId) {
        TibcoPluginConfiguration pluginConfig = readConfiguration();
        TibcoServerConfiguration serverConfig = pluginConfig.getServerConfig(serverId);

        if (serverConfig == null) {
            log.warn("Unable to find the server configuration");
            log.error("Cannot find the server configuration for server {}",serverId);
        }

        log.info("Stopping the services, if already started...!");
        boolean servicStop = tibServiceRestart(serverConfig, false);

        if (!servicStop) {
            log.error("the service was not available or already stopped, please "
                    + "check the environment if start fails");
        }
        log.info("Stopped the running services... Successfully !");
        log.info("Starting the needed tibco services before app start");
        boolean servicStart = tibServiceRestart(serverConfig, true);
        if (!servicStart) {
            log.error("Sorry the services did not start, please check your env for errors");
            return;
        }

        File appTra = new File(serverConfig.baseDir);
        if (!appTra.exists()) {
            log.info("Application doesnt seem to exists, either your server config variable is wrong"
                    + "or the application was undeployed from tibco" );
            return;
        }

        log.info("Starting the app");
        List<String> appManageCmd = new ArrayList<>();
        runBWEngineCmd(serverConfig, appManageCmd);
        log.info("Successfully started");
    }

    /**
     * Start any app using bwengine.exe with applications TRA file
     * there is a more formal way to start (i.e, AppManage.exe
     * check "To do" from the caller method for explanation
     * @param serverConfig Server configuration parameters
     * @param tradeTRACmd command List.
     */
    private void runBWEngineCmd(TibcoServerConfiguration serverConfig, List<String> tradeTRACmd) {
        log.info("Constructing Start parameters to start Tib Trade6 TRA");
        tradeTRACmd.add(serverConfig.getTibBWHome() + "/bin/bwengine.exe");
        tradeTRACmd.add("--pid");
        tradeTRACmd.add("--run");
        tradeTRACmd.add("--propFile");
        tradeTRACmd.add(serverConfig.getTibTradeDir()
                + "/" + serverConfig.getTibTraProcessName());
        try {
            Path binDir = Paths.get(serverConfig.getTibBWHome(), "bin");
            info("Starting Trade app from this Dir {0}", binDir.toString());
            ProcessBuilder ps =
                    ProcessUtils.newProcessBuilder()
                            .command(tradeTRACmd)
                            .directory(new File(serverConfig.getTibBWHome() + "/bin/"))
                            .redirectErrorStream(true)
                            .redirectOutput(ProcessBuilder.Redirect.to(new
                                    File(serverConfig.getTibBWHome(), "start.txt")));
            ProcessUtils.startProcess(ps);
            Thread.sleep(START_THREAD_BUFFER);
        } catch (Exception e) {
            String msg = "There was an interruption during the start of server";
            throw ErrorUtils.logExceptionAndWrap(log,e,msg);
        }
    }

    /**
     * this does a simple job of replacing the file with Original to unhook javaagent params
     * @param serverConfig
     */
    private void unhookTibcoStartScript(TibcoServerConfiguration serverConfig) {
        File origTra = new File(serverConfig.getTibTradeDir() + "\\"
                + serverConfig.getTibTraProcessName() + TRADE_TRA_ORIGINAL);

        File fileTraName = new File(serverConfig.getTibTradeDir() + "\\"
                + serverConfig.getTibTraProcessName());

        if(!origTra.exists()) {
            log.info("Original file doesn't exists creating one"
                    + " assuming it has no Agent parameters");
            try {
                FileUtils.copyFile(fileTraName, origTra);
            } catch (IOException e) {
                log.info("File copy did not happen!!");
                log.error("Hard luck, the copy did not happen {}",e);
            }
        } else {
            try {
                fileTraName.delete();
                FileUtils.copyFile(origTra, fileTraName);
            } catch (IOException e) {
                log.info("File copy did not happen!!");
                log.error("Hard luck, the copy did not happen {}",e);
            }
        }
    }

    /**
     * Needed method for stopping or start tibco BW services
     * there are 4 services which are needed to be started/stopped for tradeapp/wily
     * to run or shutdown completely
     *
     * @param serverConfig - server config that is read from agent conf folder
     * @param sflag - flag that decides whether to start or stop the needed services
     * @return - returns boolean.
     */
    private boolean tibServiceRestart(TibcoServerConfiguration serverConfig, boolean sflag) {
        String[] cmdListAll = serverConfig.getTibServiceNames().split(";");
        int exitCode = 0;
        String srvcFunction = "stop";

        List<String> tibCmd = new ArrayList<>();
        if (sflag) {
            srvcFunction = "start";
        }
        for (String srvName : cmdListAll) {
            tibCmd.add("net");
            tibCmd.add(srvcFunction);
            tibCmd.add(srvName);
            log.info("lets print the debugger! {}",Paths.get("C:\\Windows\\", "system32").toString());
            exitCode = processRestart(tibCmd, Paths.get("C:\\Windows\\", "system32"));
            tibCmd.clear();
        }
        if (exitCode == 0 || exitCode == 2) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * stop App using appmanage -stop cmd and stop the rest of the services
     * App manage is the proper way to start stop
     * @param serverId server id to pickup server configurations
     */
    @Override
    @ExposeMethod(description = "Stops tibcobw engine")
    public void stopAppServer(String serverId) {
        TibcoPluginConfiguration pluginConfig = readConfiguration();
        TibcoServerConfiguration serverConfig = pluginConfig.getServerConfig(serverId);

        List<String> appManageCmd = new ArrayList<>();
        log.info("Constructing appmanage command");
        getAppManageCmd(serverConfig, appManageCmd, false);
        unhookTibcoStartScript(serverConfig);
        boolean servicStop = tibServiceRestart(serverConfig, false);

        if (!servicStop) {
            log.error("the service was not available or already stopped, please check the environment if start fails");
        }
        log.info("Services Stopped successfully");
        File appTra = new File(serverConfig.baseDir);
        if (!appTra.exists()) {
            log.info("Application doesnt seem to exists, either your server config variable is wrong"
                    + "or the application was undeployed from tibco");
            return;
        }
    }

    /**
     * A common method to construct list of cmd line args
     * @param serverConfig - gets the needed parameters from conf folder
     * @param appManageCmd - List to construct the args.
     */
    private void getAppManageCmd(TibcoServerConfiguration serverConfig,
                                         List<String> appManageCmd, boolean status) {
        log.info("server config: {}",serverConfig.toString());
        appManageCmd.add(serverConfig.getTibServerStartScript());
        appManageCmd.add("-app");
        appManageCmd.add(serverConfig.getTibServerName());
        appManageCmd.add("-domain");
        appManageCmd.add(serverConfig.getTibcoDomainName());
        appManageCmd.add("-user");
        appManageCmd.add("admin");
        appManageCmd.add("-pw");
        appManageCmd.add("admin");
        if (status) {
            appManageCmd.add("-start");
        } else {
            appManageCmd.add("-stop");
        }
        Path binDir = Paths.get(serverConfig.getTibTraBinLocation(),"bin");
        log.info(binDir.toString());
        int retCode = processRestart(appManageCmd, binDir);
        if (retCode == 0 || retCode == 2) {
            log.info("AppManage has run successfully");
        } else {
            log.info("Failed to run the application {}",retCode);
            return;
        }
    }

    /**
     *  This method is used in common to run all needed commands for tibco BW server
     *
     * @param cmdTibService - pass a string list of cmd line args.
     * @return - returns exit code.
     */
    private int processRestart(List<String> cmdTibService, Path binLoc) {
        info(MessageFormat.format("Executing command: {0}", cmdTibService.toString()));

        ProcessExecutor pb = ProcessUtils2.newProcessExecutor(Slf4jStream.ofCaller().asDebug(),
                Slf4jStream.ofCaller().asError())
                .command(cmdTibService)
                .directory(binLoc.toFile());

        StartedProcess process = ProcessUtils2.startProcess(pb);
        int exitCode = ProcessUtils2.waitForProcess(process, 120, TimeUnit.SECONDS, false);
        info(MessageFormat.format("Service Restart exited with {0}", exitCode));
        return exitCode;
    }

    @Override
    public boolean stopServer(Configuration config) {
        return false;
    }

    @Override
    public void installAgent(Configuration config) {

    }


}
