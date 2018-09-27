
package com.ca.apm.systemtest.fld.plugin.em;

import java.io.File;
import java.io.InputStream;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.em.InstallerProperties.InstallerType;

import static org.apache.commons.lang3.StringUtils.defaultIfBlank;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.springframework.util.StringUtils.isEmpty;

/**
 * Plugin for installation and uninstallation of the standalone Postgresql database
 *
 * @author filja01
 */

public class DatabasePluginImpl extends EmPluginImpl implements DatabasePlugin, InitializingBean {

    private static final Logger log = LoggerFactory.getLogger(DatabasePluginImpl.class);

    public DatabasePluginImpl() {
    }

    @ExposeMethod(description = "Install Database into given prefix.")
    @Override
    public String install(final InstallationParameters params) {
        params.installerType = InstallerType.DATABASE;
        params.db = Database.postgre;

        EmPluginConfiguration cfg = configurationManager
            .loadPluginConfiguration(EmPlugin.PLUGIN, EmPluginConfiguration.class);
        params.dbAdminName = defaultIfBlank(params.dbAdminName, cfg.getDefaultDbAdminName());
        params.dbAdminPass = defaultString(params.dbAdminPass, cfg.getDefaultDbAdminPass());
        params.dbUserName = defaultIfBlank(params.dbUserName, cfg.getDefaultDbUserName());
        params.dbUserPass = defaultString(params.dbUserPass, cfg.getDefaultDbUserPass());
        params.dbSid = defaultIfBlank(params.dbSid, cfg.getDefaultDbSchema());
        if (params.dbPort == null || isEmpty(params.dbPort)) {
            params.dbPort = cfg.getDefaultDbPort();
        }

        return super.install(params);
    }

    @Override
    public void start(InstallationParameters config) {
        log.warn("There is no Database start");
    }

    @Override
    public void stop(InstallationParameters config) {
        log.info("There is no Database stop");
    }


    static String databaseForConfigImport(Database db) {
        switch (db) {
            case postgre:
                return "postgres";

            case oracle:
                return "oracle";

            default:
                throw ErrorUtils.logErrorAndReturnException(log,
                    "ConfigImport recognizes only 'oracle' and 'postgres' database types.");
        }
    }


    private static Pattern releasePattern = Pattern.compile("^\\d+\\.\\d+\\.\\d+");

    static String targetReleaseFromCodeName(String codeName) {
        Matcher matcher = releasePattern.matcher(codeName);
        if (!matcher.lookingAt()) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "{0} does not match expected pattern {1}", codeName, matcher.pattern().toString());
        }

        String release = codeName.substring(matcher.start(), matcher.end());
        log.debug("Extracted release {} from {}.", release, codeName);
        return release;
    }


    @Override
    @ExposeMethod(description = "Run domain configuration import script.")
    public void importDomainConfig(String cemDbExportFile, String targetRelease) {
        File f = new File(cemDbExportFile);
        if (!f.exists() || !f.canRead()) {
            info("Domain config import file \"{}\" does not exist or is not readable, skipping domain import.");
            return;
        }
        
        if (! SystemUtils.IS_OS_UNIX) {
            throw ErrorUtils.logErrorAndReturnException(log, "Only *NIX os is supported.");
        }
        EmPluginConfiguration emConfig = configurationManager
            .loadPluginConfiguration(PLUGIN, EmPluginConfiguration.class);
        Path configImportScript = Paths.get(emConfig.getCurrentInstallDir(), "install",
            "database-scripts", "unix", "configimport.sh");
        InetAddress addr = null;
        
        try {
            addr = InetAddress.getLocalHost();
        } catch (Exception e) {
            ErrorUtils.logExceptionAndWrap(log, e, "Exception getting hostname");
        }

        String hostname = addr.getHostName();
        ProcessExecutor pe = ProcessUtils2.newProcessExecutor()
            .command(configImportScript.toString(),
                "-dbhost", hostname,
                "-dbname", emConfig.getDefaultDbSchema(),
                "-dbport", Integer.toString(emConfig.getDefaultDbPort()),
                "-databasetype", "postgres",
                "-dbuser", emConfig.getDefaultDbUserName(),
                "-dbpassword", emConfig.getDefaultDbUserPass(),
                "-dbscriptsdir",
                Paths.get(emConfig.getCurrentInstallDir(), "install", "database-scripts")
                    .toString(),
                "-importfile", "/automation/domainconfig/current",
                "-targetrelease", targetRelease,
                "-dbserviceuser", emConfig.getDefaultDbAdminName(),
                "-dbservicepwd", emConfig.getDefaultDbAdminPass(),
                "-postgresinstalldir",
                Paths.get(emConfig.getCurrentInstallDir(), "database").toString(),
                "-is64Bit", "true",
                "-promptbeforeimport", "false")
            .environment("JAVA_HOME", emConfig.getCurrentInstallDir() + "/jre")
            .directory(configImportScript.getParent().toFile());
        // The ConfigImport utility requires user input even when -promptbeforeimport true is
        // supplied, hence this standard input redirection.
        InputStream standardInputSource = IOUtils.toInputStream("n\n", StandardCharsets.US_ASCII);
        pe.redirectInput(standardInputSource);
        StartedProcess sp = ProcessUtils2.startProcess(pe);
        int exitCode = ProcessUtils.waitForProcess(sp.getProcess(), 5, TimeUnit.MINUTES, true);
        if (exitCode != 0) {
            throw ErrorUtils.logErrorAndReturnException(log,
                "ConfigImport ended with exit code {0}", exitCode);
        }
    }
}
