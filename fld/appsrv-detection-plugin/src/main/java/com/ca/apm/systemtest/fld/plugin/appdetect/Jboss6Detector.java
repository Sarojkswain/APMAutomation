package com.ca.apm.systemtest.fld.plugin.appdetect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.jboss.JBossPlugin;
import com.ca.apm.systemtest.fld.plugin.jboss.JBossPluginConfiguration.JBossVersion;

@Component()
public class Jboss6Detector extends AppServerDetector {

    private static final Logger log = LoggerFactory.getLogger(Jboss6Detector.class);
    
    @Autowired
    private JBossPlugin appServerPlugin;
    
    @Override
    protected void scanInstallation(Path installation) {

        Path servers = Paths.get(installation.toString(), "server");
        if (Files.exists(servers)) {
            try {
                for (Path server : Files.newDirectoryStream(servers)) {
                    HashMap<String, Object> prop = new HashMap<String, Object>();
                    String port = parseConfig(Paths.get(server.toString(), "conf",
                                                        "bindingservice.beans", "META-INF",
                                                        "bindings-jboss-beans.xml"),
                                              "//bean[property[@name='bindingName'][text()='HttpConnector']]/property[@name='port']/text()");
                    if (port != null && !port.isEmpty()) {
                        prop.put(PORT, port);
                    }
                    prop.put(NAME, installation.getFileName().toString() + " - " + server.getFileName().toString());
                    prop.put(BASE_DIR, installation.toString());
                    prop.put(VERSION, JBossVersion.JBossAS6_1);

                    foundProperties.add(prop);
                }
            } catch (IOException e) {
                log.error("Exception during JBoss directories reading", e);
            }
        }
    }

    @Override
    protected AppServerPlugin getAppServerPlugin() {
        return appServerPlugin;
    }
    
    @Override
    public void testFile(String fileString) {
        if (fileString.endsWith("jboss-common-core.jar")) {
            Path foundFile = Paths.get(fileString);
            Path bin = foundFile.getParent();

            if (bin.endsWith("lib")) {
                installations.add(bin.getParent());
            }
        }
    }
}
