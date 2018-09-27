package com.ca.apm.systemtest.fld.plugin.appdetect;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ca.apm.systemtest.fld.plugin.AppServerPlugin;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPlugin;
import com.ca.apm.systemtest.fld.plugin.tomcat.TomcatPluginConfiguration.TomcatVersion;

@Component()
public class TomcatDetector
    extends AppServerDetector {

    @Autowired
    private TomcatPlugin appServerPlugin;

    @Override
    protected void scanInstallation(Path installation) {
        HashMap<String, Object> prop = new HashMap<String, Object>();

        String port = parseConfig(Paths.get(installation.toString(), "conf", "server.xml"),
                                  "/Server/Service/Connector[@protocol='HTTP/1.1']/@port");

        if (port != null && !port.isEmpty()) {
            prop.put(PORT, port);
        }
        prop.put(NAME, installation.getFileName().toString());
        String baseDir = installation.toString();
        prop.put(BASE_DIR, baseDir);
        
        if (Files.exists(Paths.get(baseDir, "bin", "Tomcat6.exe"))) {
            prop.put(VERSION, TomcatVersion.Tomcat6);    
        }
        else if (Files.exists(Paths.get(baseDir, "bin", "Tomcat7.exe"))) {
            prop.put(VERSION, TomcatVersion.Tomcat7);    
        }
        else if (Files.exists(Paths.get(baseDir, "bin", "Tomcat8.exe"))) {
            prop.put(VERSION, TomcatVersion.Tomcat8);    
        }
        
        foundProperties.add(prop);
    }

    @Override
    public void testFile(String fileString) {
        if (fileString.endsWith("catalina.bat")) {
            Path foundFile = Paths.get(fileString);
            Path bin = foundFile.getParent();

            if (bin.endsWith("bin")) {
                installations.add(bin.getParent());
            }
        }
    }

    @Override
    protected AppServerPlugin getAppServerPlugin() {
        return appServerPlugin;
    }
}
