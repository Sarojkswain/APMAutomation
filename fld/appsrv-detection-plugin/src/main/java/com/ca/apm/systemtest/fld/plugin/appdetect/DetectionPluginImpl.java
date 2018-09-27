package com.ca.apm.systemtest.fld.plugin.appdetect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;

@PluginAnnotationComponent(pluginType = DetectionPlugin.PLUGIN)
public class DetectionPluginImpl
    extends AbstractPluginImpl
    implements DetectionPlugin {

    @Autowired
    private TomcatDetector    tomcatDetector;
    @Autowired
    private Jboss6Detector    jboss6Detector;
    @Autowired
    private Jboss7Detector    jboss7Detector;
    @Autowired
    private WeblogicDetector  weblogicDetector;
    @Autowired
    private WebsphereDetector websphereDetector;

    private static final Logger log = LoggerFactory.getLogger(DetectionPluginImpl.class);
    
    @Override
    @ExposeMethod(description = "Start application server detection process.")
    public void runDetection() {

        AppServerDetector[] detectors = { tomcatDetector, jboss6Detector, jboss7Detector,
                weblogicDetector, websphereDetector };

        FileWalker fw = new FileWalker();

        for (AppServerDetector d : detectors) {
            fw.registerDetector(d);
        }

        log.info("Starting filesystem scan.");
        fw.startSearch();

        log.info("Harvesting information from installed application servers.");
        for (AppServerDetector d : detectors) {
            d.runDetection();
            d.saveResults();
        }
    }
}
