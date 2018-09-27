package com.ca.apm.systemtest.fld.plugin.weblogic;

import java.util.List;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;

@PluginAnnotationComponent(pluginType=WeblogicPlugin.PLUGIN)
public interface WeblogicPlugin extends Plugin {
    String PLUGIN = "weblogicPlugin";

    
    List<String> listInstances();

	String startInstallation(String installName);
	String startUninstallation(String installName);

	String createInstance(String installName, String instanceName, int listenPort);
	String startInstance(String installName, String instanceName);
	String stopInstance(String installName, String instanceName);

	String deployApp(String installName, String instanceName, int listenPort, String appName,
        String warFile);
	String createJms(String installName, String instanceName, int listenPort,
        String connFactoryName, String queueNames, String topicNames);
	String createDatasource(String installName, String instanceName, int listnePort, String dsName,
        String dsUrl
        , String dsDriverName, String dsUsername, String dsPassword, String dsTestQuery);
}
