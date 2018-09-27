package com.ca.apm.saas.test.utils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JavaAgentBomUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaAgentBomUtil.class);
   
    public static final List<String> COMMON_FILES = Arrays.asList(
        "core/ext/introscopeWindowsIntelAmd32Stats.dll",
        "core/ext/introscopeWindowsIntelAmd32Stats.jar",
        "core/ext/introscopeWindowsIntelAmd64Stats.dll",
        "core/ext/introscopeWindowsIntelAmd64Stats.jar",
        "core/ext/AccRegistration.jar",
        "core/ext/AgentMgr.jar",
        "core/ext/AppMap.jar",
        "core/ext/BackendProtocols.jar",
        "core/ext/BasicDirectiveLoader.jar",
        "core/ext/BizDef.jar",
        "core/ext/BizTrxHttp.jar",
        "core/ext/BoundaryOnlyTrace.jar",
        "core/ext/DynInstrBootstrap.jar",
        "core/ext/ExtensionDeployer.jar",
        "core/ext/GCMonitor.jar",
        "core/ext/Inheritance.jar",
        "core/ext/IntelligentInstrumentation.jar",        
        "core/ext/ProbeBuilder.jar",
        "core/ext/RegexNormalizerExtension.jar",
        "core/ext/ServletHeaderDecorator.jar",
        "core/ext/ServletHelper.jar",
        "core/ext/SQLAgent.jar",        
        "core/ext/Supportability-Agent.jar",
        "core/ext/ThreadDumpGen.jar",        
        "core/ext/WebServicesAgent.jar",        
        "core/ext/Java15DynamicInstrumentation.jar",
        "core/ext/DynInstrSupport15.jar");
    
    public static final List<String> SPRING_PACKAGE_EXTENSIONS = Arrays.asList(       
        "browser-agent-ext",
        "SpringBean",
        "spring-webservices",
        "struts");
    
    public static final List<String> JAVA_PACKAGE_EXTENSIONS = Arrays.asList(       
        "browser-agent-ext",       
        "struts");
    
    public static final String WEBAPPSUPPORT_CORE_EXT = "core/ext/WebAppSupport.jar";
    
    public static final String AGENT_JAR = "Agent.jar";
    
    public static final String NOREDEF_NORETRANS_JAR = "AgentNoRedefNoRetrans.jar";
    
    public static boolean isDynamicExtExist(String dir,
                                            String extPrefix) throws Exception {
        
        for (File file : new File(dir + "/extensions/deploy").listFiles()) {
            if(file.getName().endsWith("tar.gz") && file.getName().contains(extPrefix)) {    
                logger.info("Found dynamic extension with prefix " + extPrefix);
                return true;
            }
        }
        
        logger.info("Unable to find dynamic extension with prefix " + extPrefix);
        return false;
    }   
}