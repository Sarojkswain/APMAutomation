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
import com.ca.apm.systemtest.fld.plugin.websphere.WebspherePlugin;

@Component()
public class WebsphereDetector extends AppServerDetector {
    private static final Logger log = LoggerFactory.getLogger(WebsphereDetector.class);
    
    @Autowired
    private WebspherePlugin appServerPlugin;

    @Override
    protected void scanInstallation(Path installation) {
        Path profiles = Paths.get(installation.toString(), "AppServer", "profiles");
        if (Files.exists(profiles)) {
            try {
                for (Path profile : Files.newDirectoryStream(profiles)) {
                    Path cells = Paths.get(profile.toString(), "config", "cells");
                    if (Files.exists(cells)) {
                        for (Path cell : Files.newDirectoryStream(cells)) {
                            Path nodes = Paths.get(cell.toString(), "nodes");
                            if (Files.exists(nodes)) {
                                for (Path node : Files.newDirectoryStream(nodes)) {
                                    HashMap<String, Object> prop = new HashMap<String, Object>();

                                    String port = parseConfig(Paths.get(node.toString(),
                                        "serverindex.xml"),
                                        "//specialEndpoints[@endPointName='WC_defaulthost']/endPoint/@port");
                                    if (port != null && !port.isEmpty()) {
                                        prop.put(PORT, port);
                                    }
                                    prop.put(SERVER_NAME, cell.getFileName().toString());
                                    prop.put(NAME,
                                             installation.getFileName().toString() + " - "
                                                     + profile.getFileName().toString() + " - "
                                                     + cell.getFileName().toString() + " - "
                                                     + node.getFileName().toString());
                                    prop.put(BASE_DIR, installation.toString());
//                                    prop.put(TYPE, "WebSphere");

                                    foundProperties.add(prop);
                                }
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Error reading directory structure", e);
            }
        }
    }
    @Override
    protected AppServerPlugin getAppServerPlugin() {
        return appServerPlugin;
    }

    @Override
    public void testFile(String fileString) {
        if (fileString.endsWith("was_public.jar")) {
            Path foundFile = Paths.get(fileString);
            Path bin = foundFile.getParent();

            if (bin.endsWith("AppServer/dev")) {
                installations.add(bin.getParent().getParent());
            }
        }
    }
}
