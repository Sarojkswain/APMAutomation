package com.ca.apm.systemtest.fld.plugin.weblogic;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ca.apm.systemtest.fld.common.ACFileUtils;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.plugin.downloadMethod.HttpDownloadMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.ca.apm.systemtest.fld.plugin.AbstractPluginImpl;
import com.ca.apm.systemtest.fld.plugin.annotations.ExposeMethod;
import com.ca.apm.systemtest.fld.plugin.downloader.ArtifactFetchResult;
import com.ca.apm.systemtest.fld.plugin.run.RunPlugin;

public class WeblogicPluginImpl extends AbstractPluginImpl implements WeblogicPlugin {

	private static final String SQL = "SQL ";
	private static final String START_WLST_SCRIPT = "startWlstScript";
	private static final String silentScriptTemplate = "/silent-install.xml.tmpl";
	private static final String createInstanceTemplate = "/create-instance.py.tmpl";
	private static final String uninstallShTemplate = "/uninstall.sh.tmpl";
	private static final String uninstallCmdTemplate = "/uninstall.cmd.tmpl";
	private static final String createDatasourceTemplate = "/create-datasource.py.tmpl";
	private static final String createJmsTemplate = "/create-jms.py.tmpl";
	private static final String deployAppTemplate = "/deploy-app.py.tmpl";

	private static final Pattern LISTEN_PORT_PATTERN = Pattern.compile("ADMIN_URL=\"?t3://[^:]*:([0-9]*)\"?");
	private static final Logger log = LoggerFactory.getLogger(WeblogicPluginImpl.class);

	@Value( "${weblogic.download.url.win:http://isl-dsdc.ca.com/artifactory/simple/apm-third-party-local/com/ca/apm/binaries/weblogic/10.3/weblogic-10.3-windows-x86.exe}")
	String downloadUrlWin;

	@Value( "${weblogic.download.url.linux:http://isl-dsdc.ca.com/artifactory/apm-third-party-local/com/ca/apm/binaries/weblogic/10.3.5/weblogic-10.3.5-linux-x86.bin}")
	String downloadUrlLinux;

	@Value( "${weblogic.prefix:webl10-3_}" )
	String weblogicPrefix;

	@Value( "${weblogic.home.folder:S:\\bea}" )
	String weblogicHome;

	@Value( "${weblogic.install.folder:S:\\Install\\WLS}" )
	String swInstall;

	@Autowired
	RunPlugin runPlugin;


	@Override
	@ExposeMethod(description = "List installed weblogic intances in format 'install, instance, port'")
	public List<String> listInstances() {
		List<String> retVal = new ArrayList<String>();

		File instDir = new File(weblogicHome);
		if (instDir.exists() && instDir.isDirectory()) {
			File[] files = instDir.listFiles();
			for (File f : files) {
				if (f.exists() && f.isDirectory() && f.getName().startsWith(weblogicPrefix)) {
					File instanceDir = new File(f.getAbsolutePath() + File.separator + "user_projects/domains/");
					File[] instanceList = instanceDir.listFiles();
					if (instanceList == null || instanceList.length == 0) {
						retVal.add(f.getName().substring(weblogicPrefix.length()));
					} else {
						for (File i : instanceList) {
							String port = null;
							File stopScript = new File(i.getAbsolutePath() + "/bin/stopWebLogic" + (ACFileUtils.isWindowsOS() ? ".cmd" : ".sh"));
							try (BufferedReader reader = new BufferedReader(new FileReader(stopScript))) {
								String rLine = null;
								while((rLine = reader.readLine()) != null) {
									Matcher matcher = LISTEN_PORT_PATTERN.matcher(rLine);
									if (matcher.find()) {
										port = matcher.group(1);
										break;
									}
								}
							} catch (Exception e) {
								ErrorUtils.logExceptionFmt(log, e, "Can not parse port from script {1}. Exception: {0}",
									stopScript.getAbsolutePath());
							}
							retVal.add(f.getName().substring(weblogicPrefix.length()) + "," + i.getName() + "," + port);
						}
					}
				}
			}
		}
		return retVal;
	}

	@Override
	@ExposeMethod(description = "Start installation of weblogic server, should be monitored via RunPlugin")
	public String startInstallation(String installName) {
		String destDir = weblogicHome + File.separator + weblogicPrefix + installName + File.separator;
		File destDirFile = new File(destDir);
		if (destDirFile.exists()) {
			String err = "ERROR: installation " + installName + " already exist ("+destDirFile.getAbsolutePath()+")";
			log.error(err);
			return err;
		}

		String downloadUrl = ACFileUtils.isWindowsOS() ? downloadUrlWin : downloadUrlLinux;
		File instFile = downloadFile(swInstall, downloadUrl);

		File installScript = new File(swInstall + File.separator + weblogicPrefix + installName + "-install.xml");
		HashMap<String, Object> params = new HashMap<>();
		params.put("WEBLOGIC_HOME", destDir);
		params.put("INSTALL_NAME", installName);
		params.put("INSTALL_DIR", instFile.getParentFile().getAbsolutePath());
		params.put("INSTALL_FILE", instFile.getAbsolutePath());
		params.put("SCRIPT_FILE", installScript.getAbsolutePath());
		processTemplate(params, silentScriptTemplate, installScript, false);
		return runPlugin.runProcess("installWeblogic", params);
	}

	@Override
	@ExposeMethod(description = "Start uninstallation of weblogic server, should be monitored via RunPlugin")
	public String startUninstallation(String installName) {
		String destDir = weblogicHome + File.separator + weblogicPrefix + installName + File.separator;
		HashMap<String, Object> vars = new HashMap<>();
		vars.put("WEBLOGIC_HOME", destDir);
		vars.put("INSTALL_NAME", installName);
		File outputScript = new File(destDir + "fullUninstall" + (ACFileUtils.isWindowsOS() ? ".cmd" : ".sh"));
		String cmdTemplate = ACFileUtils.isWindowsOS() ? uninstallCmdTemplate : uninstallShTemplate;
		processTemplate(vars, cmdTemplate, outputScript, true);
		outputScript.setExecutable(true, true);

		return runPlugin.runProcess("uninstallWeblogic", vars);
	}

	public String createInstance(String installName, String instanceName, int listenPort) {
		String destDir = weblogicHome + File.separator + weblogicPrefix + installName + File.separator;
		String instanceDir = destDir + "user_projects/domains/" + instanceName;
		File instanceDirFile = new File(instanceDir);
		if (instanceDirFile.exists()) {
			String err = "ERROR: instance '" + instanceName + "' in installation '" + installName + "' already exist ("+instanceDirFile.getAbsolutePath()+")";
			log.error(err);
			return err;
		}

		File createInstanceScript = new File(swInstall + File.separator + weblogicPrefix + installName + "-" + instanceName +"-CREATEDOM.py");
		HashMap<String, Object> vars = new HashMap<>();
		vars.put("WEBLOGIC_HOME", destDir);
		vars.put("INSTALL_NAME", installName);
		vars.put("INSTANCE_NAME", instanceName);
		vars.put("LISTEN_PORT", Integer.toString(listenPort));
		vars.put("SCRIPT_FILE", createInstanceScript.getAbsolutePath());

		processTemplate(vars, createInstanceTemplate, createInstanceScript, true);
		return runPlugin.runProcess(START_WLST_SCRIPT, vars);
	}

	public String startInstance(String installName, String instanceName) {
		return runCommand("startWeblogicInstance", installName, instanceName);
	}

	public String stopInstance(String installName, String instanceName) {
		return runCommand("stopWeblogicInstance", installName, instanceName);
	}

	private String runCommand(String cmd, String installName, String instanceName) {
		String destDir = weblogicHome + File.separator + weblogicPrefix + installName + File.separator;
		HashMap<String, Object> vars = new HashMap<>();
		vars.put("WEBLOGIC_HOME", destDir);
		vars.put("INSTALL_NAME", installName);
		vars.put("INSTANCE_NAME", instanceName);
		return runPlugin.runProcess(cmd, vars);
	}

    @Autowired
    HttpDownloadMethod dm;

	private File downloadFile(String installFolder, String downloadUrl) {
		File instFolder = new File(installFolder);
		if (!instFolder.exists()) {
			instFolder.mkdirs();
		}

		File retVal;
		try {
			URL url = new URL(downloadUrl);
			File f = new File(url.getFile());
			File dFile = new File(installFolder + File.separator + f.getName());
			if (dFile.exists()) {
				retVal = dFile;
			} else {
				ArtifactFetchResult result = dm.fetch(downloadUrl, instFolder, true);
				retVal = result.getFile();
				retVal.setExecutable(true, true);
			}
		} catch (Exception e) {
			log.error("Error downloading installation file", e);
			retVal = null;
		}
		return retVal;
	}

	private void processTemplate(HashMap<String, Object> vars, String templateFile, File outputFile, boolean replaceBackslash) {
		if (outputFile.exists()) {
			outputFile.delete();
		}
		try (
				InputStream inputTemplate = this.getClass().getResourceAsStream(templateFile);
				OutputStream outputScriptStream = new FileOutputStream(outputFile);
			) {
			processTemplate(vars, inputTemplate, outputScriptStream, replaceBackslash);
		} catch (Exception ex) {
			ErrorUtils.logExceptionFmt(log, ex, "Can NOT process template {1}. Exception: {0}", ex);
		}
	}

	private void processTemplate(HashMap<String, Object> vars, InputStream scriptTemplate, OutputStream scriptOutput, boolean replaceBackslash) {
		StringBuilder fileContent = new StringBuilder();

		// Read
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(scriptTemplate))) {
			String line;
			while ((line = reader.readLine()) != null) {
				fileContent.append(line).append("\n");
			}
		} catch (IOException e) {
			log.error("Error reading script template", e);
		}

		// Convert
		Pattern p = Pattern.compile("\\$\\{([^}]+)\\}");
		Matcher m = p.matcher(fileContent);
		StringBuffer output = new StringBuffer();
		String replWith = replaceBackslash ? "/" : "\\\\\\\\";
		while (m.find()) {
			Object value = vars.get(m.group(1));
			if (value != null) {
				String replStr = value.toString().replaceAll("\\\\", replWith);
				m.appendReplacement(output, replStr);
			}
		}
		m.appendTail(output);

		// Write
		try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(scriptOutput))) {
			writer.write(output.toString());
		} catch (IOException e) {
			log.error("Error writing silent script template", e);
		}
	}

	@Override
	public String deployApp(String installName, String instanceName, int listenPort, String appName, String warDownloadUrl) {
		String destDir = weblogicHome + File.separator + weblogicPrefix + installName + File.separator;

		File instFile = downloadFile(swInstall, warDownloadUrl);

		File deplAppScript = new File(swInstall + File.separator + weblogicPrefix + installName + "-" + instanceName + "-DEPLAPP.py");
		HashMap<String, Object> vars = new HashMap<>();
		vars.put("WEBLOGIC_HOME", destDir);
		vars.put("INSTALL_NAME", installName);
		vars.put("INSTANCE_NAME", instanceName);
		vars.put("LISTEN_PORT", Integer.toString(listenPort));
		vars.put("SCRIPT_FILE", deplAppScript.getAbsolutePath());
		vars.put("DEPLOY_APP_NAME", appName);
		vars.put("DEPLOY_WAR_FILE", instFile.getAbsolutePath());

		processTemplate(vars, deployAppTemplate, deplAppScript, true);
		return runPlugin.runProcess(START_WLST_SCRIPT, vars);
	}

	@Override
	public String createJms(String installName, String instanceName, int listenPort, String connFactoryName, String queueNames, String topicNames) {
		String destDir = weblogicHome + File.separator + weblogicPrefix + installName + File.separator;
		File createJmsScript = new File(swInstall + File.separator + weblogicPrefix + installName + "-"
			+ instanceName + "-CREATEJMS.py");
		HashMap<String, Object> vars = new HashMap<>();
		vars.put("WEBLOGIC_HOME", destDir);
		vars.put("INSTALL_NAME", installName);
		vars.put("INSTANCE_NAME", instanceName);
		vars.put("LISTEN_PORT", Integer.toString(listenPort));
		vars.put("SCRIPT_FILE", createJmsScript.getAbsolutePath());
		vars.put("JMS_CONFACTORY_NAME", connFactoryName);
		vars.put("JMS_QUEUE_NAMES", queueNames == null ? "" : queueNames);
		vars.put("JMS_TOPIC_NAMES", topicNames == null ? "" : topicNames);

		processTemplate(vars, createJmsTemplate, createJmsScript, true);
		return runPlugin.runProcess(START_WLST_SCRIPT, vars);
	}

	@Override
	public String createDatasource(String installName, String instanceName, int listenPort, String dsName, String dsUrl
			, String dsDriverName , String dsUsername, String dsPassword, String dsTestQuery) {
		String destDir = weblogicHome + File.separator + weblogicPrefix + installName + File.separator;
		File createDatasourceScript = new File(swInstall + File.separator + weblogicPrefix
			+ installName + "-" + instanceName +"-CREATEDS.py");
		HashMap<String, Object> vars = new HashMap<>();
		vars.put("WEBLOGIC_HOME", destDir);
		vars.put("INSTALL_NAME", installName);
		vars.put("INSTANCE_NAME", instanceName);
		vars.put("LISTEN_PORT", Integer.toString(listenPort));
		vars.put("SCRIPT_FILE", createDatasourceScript.getAbsolutePath());
		vars.put("DS_NAME", dsName);
		vars.put("DS_URL", dsUrl);
		vars.put("DS_DRIVER_NAME", dsDriverName);
		vars.put("DS_USERNAME", dsUsername);
		vars.put("DS_PASSWORD", dsPassword);
		vars.put("DS_TEST_QUERY", dsTestQuery.startsWith(SQL) ? dsTestQuery : SQL + dsTestQuery);

		processTemplate(vars, createDatasourceTemplate, createDatasourceScript, true);
		return runPlugin.runProcess(START_WLST_SCRIPT, vars);
	}

}
