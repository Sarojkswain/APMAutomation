package com.ca.apm.systemtest.fld.plugin.appdetect;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPlugin;

@Component()
public class WeblogicDetector extends AppServerDetector {
    
    private static final Logger log = LoggerFactory.getLogger(WeblogicDetector.class);
    
    @Autowired
    @Qualifier(WlsPlugin.WLS_PLUGIN)
    private WlsPlugin appServerPlugin;

    @Override
    protected void scanInstallation(Path installation) {

        try {
            Path domainsFile = Paths.get(installation.toString(), "common", "nodemanager",
                                         "nodemanager.domains");
            if (Files.exists(domainsFile)) {
                Properties domains = new Properties();
                domains.load(Files.newInputStream(domainsFile));

                for (Entry<Object, Object> entry : domains.entrySet()) {
                    HashMap<String, Object> prop = new HashMap<String, Object>();

                    String port = parseConfig(Paths.get(entry.getValue().toString(), "config",
                                                        "config.xml"),
                                              "/domain/server/listen-port/text()",
                                              "/domain/server/ssl/listen-port/text()");
                    if (port != null && !port.isEmpty()) {
                        prop.put(PORT, port);
                    }
                    prop.put(NAME, installation.getFileName().toString() + " - "
                                           + entry.getKey().toString());
//                    prop.put(VERSION, "Weblogic");
                    prop.put(SERVER_NAME, installation.getFileName().toString());
                    prop.put(BASE_DIR, installation.toString());
                    foundProperties.add(prop);
                }
            }
        } catch (IOException e) {
            log.error("Error reading directory structure", e);
        }
    }

    @Override
    public void testFile(String fileString) {

        if (fileString.endsWith("setWLSEnv.cmd")) {
            Path foundFile = Paths.get(fileString);
            Path bin = foundFile.getParent();

            if (bin.endsWith("server/bin")) {
                installations.add(bin.getParent().getParent());
            }
        }
    }

    @Override
    protected AppServerPlugin getAppServerPlugin() {
        return appServerPlugin;
    }
}
