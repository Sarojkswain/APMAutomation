package com.ca.apm.saas.test.utils;

import java.util.Arrays;
import java.util.List;

public abstract class IABomUtil {
    
    public static final String IA_INSTALL_SCRIPT_UNIX = "apmia-ca-installer.sh";
    public static final String IA_INSTALL_SCRIPT_WIN  = "apmia-ca-installer.bat";
    public static final String IA_PARENT_DIR_SAAS     = "umagent"; //change to 'apmia' once saas is updated
   
    public static final List<String> DOCKER_HOST_FILES = Arrays.asList(
        IA_PARENT_DIR_SAAS + "/" + IA_INSTALL_SCRIPT_UNIX,
        IA_PARENT_DIR_SAAS + "/extensions/deploy/docker-monitor",  //partial file name    
        IA_PARENT_DIR_SAAS + "/extensions/deploy/container-flow",  //partial file name
        IA_PARENT_DIR_SAAS + "/casystemedge",
        IA_PARENT_DIR_SAAS + "/lib/CollectorAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/EPAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/gson.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-server.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-servlet.jar",
        IA_PARENT_DIR_SAAS + "/lib/UnifiedMonitoringAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/xercesImpl.jar",
        IA_PARENT_DIR_SAAS + "/extensions/Extensions.profile",
        IA_PARENT_DIR_SAAS + "/bin/APMIAgent.sh",
        IA_PARENT_DIR_SAAS + "/core/ext/BackendProtocols.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BasicDirectiveLoader.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BizDef.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ExtensionDeployer.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/GCMonitor.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/IntelligentInstrumentation.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ProbeBuilder.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/RegexNormalizerExtension.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ServletHelper.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/SQLAgent.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ThreadDumpGen.jar",
        IA_PARENT_DIR_SAAS + "/jre/bin/java",
        IA_PARENT_DIR_SAAS + "/extensions/",
        IA_PARENT_DIR_SAAS + "/extensions/deploy/",
        IA_PARENT_DIR_SAAS + "/lib/",
        IA_PARENT_DIR_SAAS + "/logs/",
        IA_PARENT_DIR_SAAS + "/core/config/",
        IA_PARENT_DIR_SAAS + "/core/config/hotdeploy/",
        IA_PARENT_DIR_SAAS + "/core/config/dynamic/",
        IA_PARENT_DIR_SAAS + "/extensions/deploy/HostMonitor.tar.gz",
        IA_PARENT_DIR_SAAS + "/installInstructions.md",
        IA_PARENT_DIR_SAAS + "/manifest.txt",
        IA_PARENT_DIR_SAAS + "/core/config/IntroscopeAgent.profile");
    
    public static final List<String> DOCKER_FILES = Arrays.asList(
    	IA_PARENT_DIR_SAAS + "/" + IA_INSTALL_SCRIPT_UNIX,
        IA_PARENT_DIR_SAAS + "/extensions/deploy/docker-monitor",  //partial file name    
        IA_PARENT_DIR_SAAS + "/extensions/deploy/container-flow",  //partial file name
        IA_PARENT_DIR_SAAS + "/lib/CollectorAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/EPAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/gson.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-server.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-servlet.jar",
        IA_PARENT_DIR_SAAS + "/lib/UnifiedMonitoringAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/xercesImpl.jar",
        IA_PARENT_DIR_SAAS + "/extensions/Extensions.profile",
        IA_PARENT_DIR_SAAS + "/bin/APMIAgent.sh",
        IA_PARENT_DIR_SAAS + "/core/ext/BackendProtocols.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BasicDirectiveLoader.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BizDef.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ExtensionDeployer.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/GCMonitor.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/IntelligentInstrumentation.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ProbeBuilder.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/RegexNormalizerExtension.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ServletHelper.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/SQLAgent.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ThreadDumpGen.jar",
        IA_PARENT_DIR_SAAS + "/jre/bin/java",
        IA_PARENT_DIR_SAAS + "/extensions/",
        IA_PARENT_DIR_SAAS + "/extensions/deploy/",
        IA_PARENT_DIR_SAAS + "/lib/",
        IA_PARENT_DIR_SAAS + "/logs/",
        IA_PARENT_DIR_SAAS + "/core/config/",
        IA_PARENT_DIR_SAAS + "/core/config/hotdeploy/",
        IA_PARENT_DIR_SAAS + "/core/config/dynamic/",
        IA_PARENT_DIR_SAAS + "/installInstructions.md",
        IA_PARENT_DIR_SAAS + "/manifest.txt",
        IA_PARENT_DIR_SAAS + "/core/config/IntroscopeAgent.profile");
    
    public static final List<String> HOST_FILES = Arrays.asList(     
        IA_PARENT_DIR_SAAS + "/" + IA_INSTALL_SCRIPT_UNIX,
    	IA_PARENT_DIR_SAAS + "/casystemedge",
        IA_PARENT_DIR_SAAS + "/lib/CollectorAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/EPAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/gson.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-server.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-servlet.jar",
        IA_PARENT_DIR_SAAS + "/lib/UnifiedMonitoringAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/xercesImpl.jar",
        IA_PARENT_DIR_SAAS + "/extensions/Extensions.profile",
        IA_PARENT_DIR_SAAS + "/bin/APMIAgent.sh",
        IA_PARENT_DIR_SAAS + "/core/ext/BackendProtocols.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BasicDirectiveLoader.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BizDef.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ExtensionDeployer.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/GCMonitor.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/IntelligentInstrumentation.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ProbeBuilder.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/RegexNormalizerExtension.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ServletHelper.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/SQLAgent.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ThreadDumpGen.jar",
        IA_PARENT_DIR_SAAS + "/jre/bin/java",
        IA_PARENT_DIR_SAAS + "/extensions/",
        IA_PARENT_DIR_SAAS + "/extensions/deploy/",
        IA_PARENT_DIR_SAAS + "/lib/",
        IA_PARENT_DIR_SAAS + "/logs/",
        IA_PARENT_DIR_SAAS + "/core/config/",
        IA_PARENT_DIR_SAAS + "/core/config/hotdeploy/",
        IA_PARENT_DIR_SAAS + "/core/config/dynamic/",
        IA_PARENT_DIR_SAAS + "/extensions/deploy/HostMonitor.tar.gz",
        IA_PARENT_DIR_SAAS + "/installInstructions.md",
        IA_PARENT_DIR_SAAS + "/manifest.txt",
        IA_PARENT_DIR_SAAS + "/core/config/IntroscopeAgent.profile");
    
    public static final List<String> OPEN_SHIFT_FILES = Arrays.asList(  
        IA_PARENT_DIR_SAAS + "/extensions/deploy/openshift-monitor",
        IA_PARENT_DIR_SAAS + "/lib/CollectorAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/EPAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/gson.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-server.jar",
        IA_PARENT_DIR_SAAS + "/lib/jetty-servlet.jar",
        IA_PARENT_DIR_SAAS + "/lib/UnifiedMonitoringAgent.jar",
        IA_PARENT_DIR_SAAS + "/lib/xercesImpl.jar",
        IA_PARENT_DIR_SAAS + "/extensions/Extensions.profile",
        IA_PARENT_DIR_SAAS + "/core/ext/BackendProtocols.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BasicDirectiveLoader.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/BizDef.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ExtensionDeployer.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/GCMonitor.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/IntelligentInstrumentation.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ProbeBuilder.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/RegexNormalizerExtension.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ServletHelper.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/SQLAgent.jar",
        IA_PARENT_DIR_SAAS + "/core/ext/ThreadDumpGen.jar",
        IA_PARENT_DIR_SAAS + "/extensions/",
        IA_PARENT_DIR_SAAS + "/extensions/deploy/",
        IA_PARENT_DIR_SAAS + "/lib/",
        IA_PARENT_DIR_SAAS + "/logs/",
        IA_PARENT_DIR_SAAS + "/core/config/",
        IA_PARENT_DIR_SAAS + "/core/config/hotdeploy/",
        IA_PARENT_DIR_SAAS + "/core/config/dynamic/",
        IA_PARENT_DIR_SAAS + "/bin/APMIAgent.sh",
        IA_PARENT_DIR_SAAS + "/" + IA_INSTALL_SCRIPT_UNIX,
        IA_PARENT_DIR_SAAS + "/jre/bin/java",
        IA_PARENT_DIR_SAAS + "/installInstructions.md",
        IA_PARENT_DIR_SAAS + "/manifest.txt",
        IA_PARENT_DIR_SAAS + "/core/config/IntroscopeAgent.profile");
    
    public static List<String> getFileList(String type) {
    
        if(type.equals("docker-host-monitoring")) {
            return DOCKER_HOST_FILES;
        }
        else if(type.equals("docker-monitoring")) {
            return DOCKER_FILES;
        }
        else if(type.equals("host-monitoring")) {
            return HOST_FILES;
        } 
        else if(type.equals("openshift-monitoring")) {
            return OPEN_SHIFT_FILES;
        }
        else {
            return null;
        }
    }
}