
package com.ca.apm.systemtest.fld.plugin.em;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

public class InstallerProperties {
    public enum InstallerType {
        EM, DATABASE, WEBVIEW, AGC
    }

    private static final String GC_LOG_FILE_NAME = "gclog.txt";

    private static final Logger log = LoggerFactory.getLogger(InstallerProperties.class);

    private Properties config;
    private Path configFile;
    private InstallerType installerType;
    private boolean headless = false;

    private int heapSize = 4096;
    private Path gcLogFile = Paths.get("/tmp/", GC_LOG_FILE_NAME);
    private String jvmExtraArgs;

    public InstallerProperties(Path file, InstallerType it) {
        this.configFile = file;
        config = new Properties();
        installerType = it;

        try {
            InputStream input = Files.newInputStream(configFile);
            config.load(input);
            input.close();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to read response file {1}. Exception: {0}",
                configFile.toAbsolutePath());
        }

        setDefaultValues();
    }

    private void setDefaultValues() {
        config.setProperty("eulaFile", "eula.txt");
        config.setProperty("ca-eulaFile", "ca-eula.txt");
        
        switch (installerType) {
            case EM:
                config.setProperty("shouldClusterEm", "true");
                config.setProperty("emClusterRole", "Collector");
                config.setProperty("silentInstallChosenFeatures", "Enterprise Manager,ProbeBuilder,EPA");
                break;

            case DATABASE:
                config.setProperty("silentInstallChosenFeatures", "Database");
                break;

            case WEBVIEW:
                config.setProperty("wvPort", "8080");
                config.setProperty("silentInstallChosenFeatures", "WebView");
                break;
                
            case AGC:
                config.setProperty("shouldClusterEm", "true");
                config.setProperty("emClusterRole", "Manager");
                config.setProperty("silentInstallChosenFeatures", "Enterprise Manager,WebView,ProbeBuilder,Database");
                break;

            default:
                break;
        }
    }
    
    
    
    

    public InstallerProperties writeResponseFile() {
        String headlessOption = "";
        if (headless) {
            headlessOption = "-Djava.awt.headless=true";
        }
        
        switch(installerType) {
            case EM:
                if (!StringUtils.isEmpty(jvmExtraArgs)) {
                    config.setProperty("emLaxNlJavaOptionAdditional", jvmExtraArgs);
                } else {
                    String aditional =
                        String
                            .format(
                                "-Xms%1$dm -Xmx%1$dm %3$s -Dorg.owasp.esapi.resources=./config/esapi -Dcom.wily.assert=false "
                                    + "-XX:MaxPermSize=256m -showversion -XX:+UseConcMarkSweepGC "
                                    + "-XX:+UseParNewGC -XX:CMSInitiatingOccupancyFraction=50 "
                                    + "-XX:+HeapDumpOnOutOfMemoryError -verbose:gc -Xloggc:%2$s",
                                heapSize, gcLogFile.toAbsolutePath(), headlessOption);
                    
                    config.setProperty("emLaxNlJavaOptionAdditional", aditional);
                }
                break;
            case WEBVIEW:
                String adition =
                    String
                        .format(
                            "-Xms%1$dm -Xmx%1$dm -XX:PermSize=128m -XX:MaxPermSize=256m "
                                + "-Xloggc:%2$s -verbose:gc -XX:+PrintGCDateStamps "
                                + "-XX:+HeapDumpOnOutOfMemoryError "
                                + "-Dorg.owasp.esapi.resources=./config/esapi -Dsun.java2d.noddraw=true "
                                + "-javaagent:./product/webview/agent/wily/Agent.jar "
                                + "-Dcom.wily.introscope.agentProfile=./product/webview/agent/wily/core/config/IntroscopeAgent.profile "
                                + "-Dcom.wily.introscope.wilyForWilyPrefix=com.wily  %3$s",
                            heapSize, gcLogFile.toAbsolutePath(), headlessOption);
                config.setProperty("wvLaxNlJavaOptionAdditional", adition);
                break;
            default:
                break;
        }
        
        
        try {
            OutputStream output = Files.newOutputStream(configFile);
            config.store(output, "EM installer LAX file");
            output.close();
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to write response file {1}. Exception: {0}",
                configFile.toAbsolutePath());
        }

        return this;
    }

    public InstallerProperties setCollectors(Collection<String> hosts) {
        if (hosts == null || hosts.isEmpty()) {
            return this;
        }

        int i = 0;
        for (String host : hosts) {
            i++;

            if (host == null || host.isEmpty()) {
                continue;
            }

            config.setProperty("emCollectorPort." + i, "5001");
            config.setProperty("emCollectorHost." + i, host);
        }
        config.setProperty("emClusterRole", "Manager");
        return this;
    }

    public InstallerProperties setHeapSize(int heapSize) {
        this.heapSize = heapSize;
        return this;
    }

    public InstallerProperties setLogFolder(Path logFolder) {
        if (logFolder != null) {
            this.gcLogFile = Paths.get(logFolder.toAbsolutePath().toString(), GC_LOG_FILE_NAME);
        }
        return this;
    }

    public InstallerProperties setInstallationDir(Path installDir) {
        config.setProperty("USER_INSTALL_DIR", installDir.toAbsolutePath().toString());
        return this;
    }

    public InstallerProperties setExternalComponentPackage(Path packages) {
        config.setProperty("externalComponentPackage", packages.toAbsolutePath().toString());
        return this;
    }
    
    public InstallerProperties setEmHostPort(String host, Integer port) {
        config.setProperty("wvEmHost", host);
        config.setProperty("wvEmPort", port.toString());
        return this;
    }

    public InstallerProperties setOracleDatabase(String host, int port, String sidName,
        String user, String passwd) {
        config.setProperty("chosenDatabaseIsPostgres", "false");
        config.setProperty("chosenDatabaseIsOracle", "true");
        config.setProperty("useExistingSchemaForOracle", "false");
        config.setProperty("oracleDbHost", host);
        config.setProperty("oracleDbPort", Integer.toString(port));
        config.setProperty("oracleDbSidName", sidName);
        config.setProperty("oracleDbUsername", user);
        config.setProperty("oracleDbPassword", passwd);
        return this;
    }

    public InstallerProperties setPostgreDatabase(String host, String dbUser, String passwd, String serviceUser,
        String servicePasswd) {
        config.setProperty("chosenDatabaseIsPostgres", "true");
        config.setProperty("chosenDatabaseIsOracle", "false");
        config.setProperty("dbHost", host);
        config.setProperty("dbUser", dbUser);
        config.setProperty("dbPassword", passwd);
        config.setProperty("dbAdminUser", serviceUser);
        config.setProperty("dbAdminPassword", servicePasswd);
        return this;
    }

    public Path getResponseFile() {
        return configFile.toAbsolutePath();
    }

    @Override
    public String toString() {
        return config.toString();
    }

    /**
     * @param headless Should we set the -Djava.awt.headless flag
     */
    public void setHeadless(boolean headless) {
        this.headless = headless;
    }

    public void setJvmExtraArgs(String jvmExtraArgs) {
        this.jvmExtraArgs = jvmExtraArgs;
    }
    
    public Path getGcLogFile() {
        return gcLogFile;
    }
    
}
