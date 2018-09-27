
package com.ca.apm.systemtest.fld.plugin.em;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.NetworkUtils;
import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.common.ProcessUtils;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.em.InstallerProperties.InstallerType;
import com.ca.apm.systemtest.fld.plugin.util.SystemUtil.OperatingSystemFamily;

/**
 * Plugin for installation and uninstallation of WebView
 * 
 * @author filja01
 *
 */

@PluginAnnotationComponent(pluginType = "wvPlugin")
public class WebViewPluginImpl extends EmPluginImpl implements EmPlugin {

    private static final Logger log = LoggerFactory.getLogger(WebViewPluginImpl.class);

    public WebViewPluginImpl() {}

    @ExposeMethod(description = "Install WebView into given prefix.")
    @Override
    public String install(final InstallationParameters config) {
        //stop(config);
        config.installerType = InstallerType.WEBVIEW;
        return super.install(config);
    }
    
    @ExposeMethod(description = "Uninstall WebView.")
    @Override
    public void uninstall(final InstallationParameters config) {
        stop(config);
        super.uninstall(config);
    }
    
    @Override
    public void start(InstallationParameters config) {
        EmPluginConfiguration cfg = getConfiguration();
    
        log.info("Starting WebView");
        Path instDir = Paths.get(cfg.getCurrentInstallDir());
        Path logDir = Paths.get(cfg.getCurrentLogDir());
//        Path instDir = Paths.get(config.installDir);
//        Path logDir = Paths.get(config.logs);
        
        if (!NetworkUtils.isServerListening("localhost", 8080)) {
            File exeFile = findFile(instDir, "Introscope_WebView");
            if (!exeFile.exists()) {
                exeFile = findFile(instDir, "Introscope_WebView.exe");
            }
            ProcessBuilder em =
                ProcessUtils
                    .newProcessBuilder(true)
                    .command(exeFile.getAbsolutePath())
                    .directory(instDir.toFile())
                    .redirectErrorStream(true)
                    .redirectOutput(
                        Redirect.appendTo(new File(logDir.toFile(), "webview.txt")));
            ProcessUtils.startProcess(em);

            log.debug("Waiting for WebView");

            boolean isListening = false;
            for (int i = 0; i < 60; i++) {
                if (isListening = NetworkUtils.isServerListening("localhost", 8080)) {
                    break;
                }

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    // Panic!
                }
            }

            if (!isListening) {
                ErrorUtils.logErrorAndThrowException(log, "WebView is not running");
            }
        } else {
            log.info("WebView was already running.");
        }
    }

    @Override
    public void stop(InstallationParameters config) {
        log.info("Stopping WebView");

        if (NetworkUtils.isServerListening("localhost", 8080)) {
            if (config.platform == OperatingSystemFamily.Windows) {
                ProcessBuilder clw =
                    ProcessUtils
                        .newProcessBuilder()
                        .command("taskkill", "/f", "/im", "Introscope_WebView*", "/t");
                ProcessUtils.waitForProcess(ProcessUtils.startProcess(clw), 1, TimeUnit.MINUTES, true);
            }
            else {
                ProcessBuilder clw =
                    ProcessUtils
                        .newProcessBuilder()
                        .command("/bin/sh", "-c", "kill $(ps -ef | grep javaagent | awk 'NR==1{print $2}')");
                ProcessUtils.waitForProcess(ProcessUtils.startProcess(clw), 1, TimeUnit.MINUTES, true);
            }
            boolean isListening = true;
            for (int i = 0; i < 60; i++) {
                if (!(isListening = NetworkUtils.isServerListening("localhost", 8080))) {
                    break;
                }

                try {
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    // Panic!
                }
            }

            if (isListening) {
                ErrorUtils.logErrorAndThrowException(log, "WebView is running after shutdown.");
            }
        } else {
            log.info("WebView was not running.");
        }
    }

    /**
     * Main.
     * 
     * @param args
     */
    public static void main(String[] args) {

        InstallationParameters config = new InstallationParameters();
        config.installDir = args[0];

        config.wvEmHost = "sqw64xeoserv30";
        config.wvEmPort = 5001;

        config.logs = "C:\\";

        WebViewPluginImpl wv = new WebViewPluginImpl();
        // wv.install(config);
        System.out.println("stop EM");
        wv.stop(config);

        System.out.println("start EM");
        wv.start(config);
        // wv.uninstall(config);
    }
}
