package com.ca.apm.systemtest.fld.plugin.appdetect;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.jboss.JBossPlugin;
import com.ca.apm.systemtest.fld.plugin.jboss.JBossPluginConfiguration.JBossVersion;

@Component()
public class Jboss7Detector
    extends AppServerDetector {

    @Autowired
    private JBossPlugin appServerPlugin;

    @Override
    protected void scanInstallation(Path installation) {
        HashMap<String, Object> prop = new HashMap<>();

        String port = parseConfig(Paths.get(installation.toString(), "standalone", "configuration",
                                            "standalone.xml"),
                                  "/server/socket-binding-group/socket-binding[@name='http']/@port");
        if (port != null && !port.isEmpty()) {
            prop.put(PORT, port);
        }
        prop.put(NAME, installation.getFileName().toString());
        prop.put(BASE_DIR, installation.toString());
        prop.put(VERSION, JBossVersion.JBossAS7_1);
        foundProperties.add(prop);
    }

    @Override
    protected AppServerPlugin getAppServerPlugin() {
        return appServerPlugin;
    }

    @Override
    public void testFile(String fileString) {
        if (fileString.endsWith("jboss-cli.bat")) {
            Path foundFile = Paths.get(fileString);
            Path bin = foundFile.getParent();

            if (bin.endsWith("bin")) {
                installations.add(bin.getParent());
            }
        }
    }
}
