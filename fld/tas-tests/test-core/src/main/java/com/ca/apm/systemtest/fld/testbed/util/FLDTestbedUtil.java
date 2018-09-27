package com.ca.apm.systemtest.fld.testbed.util;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;

import com.ca.apm.systemtest.fld.artifact.thirdparty.JMeterVersion;
import com.ca.apm.systemtest.fld.artifact.thirdparty.TasTestsCoreVersion;
import com.ca.apm.systemtest.fld.flow.DeployLogMonitorFlowContext;
import com.ca.apm.systemtest.fld.testbed.regional.FLDConfiguration;
import com.ca.apm.systemtest.fld.testbed.regional.LogMonitorConfigurationSource;
import com.ca.tas.artifact.thirdParty.JBossVersion;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.artifact.thirdParty.TomcatVersion;
import com.ca.tas.role.IRole;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.Testbed;

public class FLDTestbedUtil {

    public static final String BASE_DIR = "C:/SW/";
    public static final String BASE_DIR_LINUX = "/home/sw/";

    public static final String XSLT_TEMPLATE_START =
        "<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>"
            + "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">"
            + " <xsl:output method=\"xml\" indent=\"yes\" encoding=\"utf-8\" />"
            + " <xsl:template match=\"comment()\">"
            + "     <xsl:copy />"
            + " </xsl:template>"
            + " <xsl:template match=\"*|@*\">"
            + "     <xsl:copy>"
            + "         <xsl:copy-of select=\"@*\" />"
            + "         <xsl:apply-templates />"
            + "     </xsl:copy>"
            + " </xsl:template>";
    public static final String XSLT_TEMPLATE_END = "</xsl:stylesheet>";


    private FLDTestbedUtil() {}

    public static String getJavaDir(JavaBinary javaBinary) {
        return getAbsolutePath(BASE_DIR, "java", javaBinary.getJavaRuntime().name().toLowerCase()
            + javaBinary.getArtifact().getVersion());
    }

    public static String getJBossDir(JBossVersion jbossVersion) {
        return getAbsolutePath(BASE_DIR, jbossVersion.getArtifact().getArtifactId() + "-"
            + jbossVersion.getVersion());
    }

    public static String getTomcatDir(TomcatVersion tomcatVersion) {
        return getAbsolutePath(BASE_DIR, "tomcat", "apache-tomcat-" + tomcatVersion.getVersion());
    }

    public static String getJMeterDir(JMeterVersion jmeterVersion) {
        return getAbsolutePath(BASE_DIR, "jmeter", "apache-jmeter-" + jmeterVersion.getVersion());
    }

    public static String getAgentDynamicInstrumentationDir() {
        return getAbsolutePath(BASE_DIR, "DI");
    }

    public static String getCLWLibDir() {
        return getAbsolutePath(getAgentDynamicInstrumentationDir(), "lib");
    }

    public static String getCLWJar() {
        return "CLWorkstation.jar";
    }

    public static String getWebViewLoadWorkDir() {
        return getAbsolutePath(BASE_DIR, "webview-load");
    }

    public static String getWebViewLoadStartScript() {
        return getAbsolutePath(getWebViewLoadWorkDir(), "run-webview-load.bat");
    }

    public static String getSeleniumRunDir(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(BASE_DIR, tasTestsCoreVersion.getArtifact().getArtifactId());
    }

    public static String getSeleniumRunJar(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(getSeleniumRunDir(tasTestsCoreVersion),
            tasTestsCoreVersion.getFilename());
    }

    public static String getMemoryMonitorWorkDir() {
        return getAbsolutePath(BASE_DIR, "memory-monitor");
    }

    public static String getLinuxMemoryMonitorWorkDir() {
        return getLinuxAbsolutePath(BASE_DIR_LINUX, "memory-monitor");
    }

    public static String getMemoryMonitorStartScript() {
        return getAbsolutePath(getMemoryMonitorWorkDir(), "run-memory-monitor.bat");
    }

    public static String getLinuxMemoryMonitorStartScript() {
        return getLinuxAbsolutePath(getLinuxMemoryMonitorWorkDir(), "run-memory-monitor.sh");
    }

    public static String getLinuxMemoryMonitorStopScript() {
        return getLinuxAbsolutePath(getLinuxMemoryMonitorWorkDir(), "stop-memory-monitor.sh");
    }

    public static String getMemoryMonitorRunDir(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(BASE_DIR, tasTestsCoreVersion.getArtifact().getArtifactId());
    }

    public static String getLinuxMemoryMonitorRunDir(TasTestsCoreVersion tasTestsCoreVersion) {
        return getLinuxAbsolutePath(BASE_DIR_LINUX, tasTestsCoreVersion.getArtifact()
            .getArtifactId());
    }

    public static String getMemoryMonitorRunJar(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(getMemoryMonitorRunDir(tasTestsCoreVersion),
            tasTestsCoreVersion.getFilename());
    }

    public static String getLinuxMemoryMonitorRunJar(TasTestsCoreVersion tasTestsCoreVersion) {
        return getLinuxAbsolutePath(getLinuxMemoryMonitorRunDir(tasTestsCoreVersion),
            tasTestsCoreVersion.getFilename());
    }

    public static String getTimeSynchronizationWorkDir() {
        return getAbsolutePath(BASE_DIR, "time-synchronization");
    }

    public static String getLinuxTimeSynchronizationWorkDir() {
        return getLinuxAbsolutePath(BASE_DIR_LINUX, "time-synchronization");
    }

    public static String getTimeSynchronizationRunDir(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(BASE_DIR, tasTestsCoreVersion.getArtifact().getArtifactId());
    }

    public static String getLinuxTimeSynchronizationRunDir(TasTestsCoreVersion tasTestsCoreVersion) {
        return getLinuxAbsolutePath(BASE_DIR_LINUX, tasTestsCoreVersion.getArtifact()
            .getArtifactId());
    }

    public static String getTimeSynchronizationRunJar(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(getTimeSynchronizationRunDir(tasTestsCoreVersion),
            tasTestsCoreVersion.getFilename());
    }

    public static String getLinuxTimeSynchronizationRunJar(TasTestsCoreVersion tasTestsCoreVersion) {
        return getLinuxAbsolutePath(getLinuxTimeSynchronizationRunDir(tasTestsCoreVersion),
            tasTestsCoreVersion.getFilename());
    }

    public static String getNetworkTrafficMonitorWorkDir() {
        return getAbsolutePath(BASE_DIR, "network-traffic-monitor");
    }

    public static String getLinuxNetworkTrafficMonitorWorkDir() {
        return getLinuxAbsolutePath(BASE_DIR_LINUX, "network-traffic-monitor");
    }

    public static String getNetworkTrafficMonitorStartScript() {
        return getAbsolutePath(getNetworkTrafficMonitorWorkDir(),
            "run-network-traffic-chart-generator.bat");
    }

    public static String getLinuxNetworkTrafficMonitorStartScript() {
        return getLinuxAbsolutePath(getLinuxNetworkTrafficMonitorWorkDir(),
            "run-network-traffic-chart-generator.sh");
    }

    public static String getLinuxNetworkTrafficMonitorStopScript() {
        return getLinuxAbsolutePath(getLinuxNetworkTrafficMonitorWorkDir(),
            "stop-network-traffic-chart-generator.sh");
    }

    public static String getNetworkTrafficMonitorRunDir(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(BASE_DIR, tasTestsCoreVersion.getArtifact().getArtifactId());
    }

    public static String getLinuxNetworkTrafficMonitorRunDir(TasTestsCoreVersion tasTestsCoreVersion) {
        return getLinuxAbsolutePath(BASE_DIR_LINUX, tasTestsCoreVersion.getArtifact()
            .getArtifactId());
    }

    public static String getNetworkTrafficMonitorRunJar(TasTestsCoreVersion tasTestsCoreVersion) {
        return getAbsolutePath(getNetworkTrafficMonitorRunDir(tasTestsCoreVersion),
            tasTestsCoreVersion.getFilename());
    }

    public static String getLinuxNetworkTrafficMonitorRunJar(TasTestsCoreVersion tasTestsCoreVersion) {
        return getLinuxAbsolutePath(getLinuxNetworkTrafficMonitorRunDir(tasTestsCoreVersion),
            tasTestsCoreVersion.getFilename());
    }

    /**
     * <p>
     * getAbsolutePath.
     * </p>
     *
     * Return absolute path for Windows OS.
     *
     * @return a {@link java.lang.String} object.
     */
    public static String getAbsolutePath(String... pathElements) {
        StringBuilder path = new StringBuilder(pathElements.length * 15);
        for (String pathEl : pathElements) {
            if (path.lastIndexOf("\\") != path.length() - 1) {
                path.append("\\");
            }
            path.append(pathEl.replaceAll("/", "\\\\"));
        }
        return path.toString();
    }

    public static String getLinuxAbsolutePath(String... pathElements) {
        // TODO
        // return Paths.get("", pathElements).toAbsolutePath().toString();
        StringBuilder path = new StringBuilder(pathElements.length * 15);
        for (String pathEl : pathElements) {
            if (path.lastIndexOf("/") != path.length() - 1) {
                path.append("/");
            }
            path.append(pathEl.replaceAll("\\\\", "/"));
        }
        return path.toString();
    }

    public static ITestbedMachine getMachineById(Testbed testbed, String machineId) {
        try {
            return testbed.getMachineById(machineId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static IRole getRoleById(ITestbedMachine machine, String roleId) {
        try {
            return machine.getRoleById(roleId);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static boolean hasRole(ITestbedMachine machine, String roleId) {
        return getRoleById(machine, roleId) != null;
    }

    public static String getXslTemplate(String xsltTemplateBody) {
        return XSLT_TEMPLATE_START + xsltTemplateBody + XSLT_TEMPLATE_END;
    }

    public static void xslt(File xmlFile, String xsl) throws IOException, TransformerException {
        String xml = FileUtils.readFileToString(xmlFile, "UTF-8");
        xmlFile.delete();
        xslt(xml, xmlFile.getAbsolutePath(), xsl);
    }

    public static void xslt(File srcXmlFile, File dstXmlFile, String xsl)
        throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(new StringReader(xsl));
        Transformer transformer = factory.newTransformer(xslt);
        Source text = new StreamSource(srcXmlFile);
        transformer.transform(text, new StreamResult(dstXmlFile));
    }

    public static void xslt(String srcXml, String dstXmlFile, String xsl)
        throws TransformerException {
        TransformerFactory factory = TransformerFactory.newInstance();
        Source xslt = new StreamSource(new StringReader(xsl));
        Transformer transformer = factory.newTransformer(xslt);
        Source text = new StreamSource(new StringReader(srcXml));
        transformer.transform(text, new StreamResult(dstXmlFile));
    }

    public static LogMonitorConfigurationSource getDefaultMomLogMonitorConfiguration() {
        return new LogMonitorConfigurationSourceImpl(
            DeployLogMonitorFlowContext.LogMonitorConfigSource.ResourceFile,
            FLDConfiguration.LOG_MONITOR_CONFIG_JSON, "emLogStream");
    }
}
