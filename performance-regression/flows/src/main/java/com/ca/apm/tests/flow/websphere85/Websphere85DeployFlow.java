/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.tests.flow.websphere85;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;

/**
 * DeployMsSqlDbFlow
 *
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class Websphere85DeployFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(Websphere85DeployFlow.class);
    @FlowContext
    private Websphere85DeployFlowContext context;

    public Websphere85DeployFlow() {
    }

    protected static File createManagerSilentInstallXml(File managerSourcesLocation, File managerInstallLocation) {
        File outputFile = FileUtils.getFile(managerSourcesLocation, "install.xml");
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // agent-input element
            Document doc = docBuilder.newDocument();
            Element agentInputElement = doc.createElement("agent-input");
            agentInputElement.setAttribute("clean", "true");
            agentInputElement.setAttribute("temporary", "true");
            doc.appendChild(agentInputElement);
            // profile element
            Element profileElement = doc.createElement("profile");
            profileElement.setAttribute("kind", "self");
            profileElement.setAttribute("installLocation", managerInstallLocation.getAbsolutePath());
            profileElement.setAttribute("id", "IBM Installation Manager");
            agentInputElement.appendChild(profileElement);
            // data element
            Element dataElement = doc.createElement("data");
            dataElement.setAttribute("key", "eclipseLocation");
            dataElement.setAttribute("value", managerInstallLocation.getAbsolutePath());
            profileElement.appendChild(dataElement);
            // server element
            Element serverElement = doc.createElement("server");
            agentInputElement.appendChild(serverElement);
            // repository element
            Element repositoryElement = doc.createElement("repository");
            repositoryElement.setAttribute("location", ".");
            serverElement.appendChild(repositoryElement);
            // install element
            Element installElement = doc.createElement("install");
            agentInputElement.appendChild(installElement);
            // offering element
            Element offeringElement = doc.createElement("offering");
            offeringElement.setAttribute("features", "agent_core,agent_jre");
            offeringElement.setAttribute("id", "com.ibm.cic.agent");
            offeringElement.setAttribute("version", "1.5.2000.20120223_0907");
            installElement.appendChild(offeringElement);
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outputFile);
            // Output to console for testing
//            StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
        return outputFile;
    }

    protected static File createWasSilentInstallXml(File wasSourcesLocation, File javaSourcesLocation,
                                                    File wasInstallLocation, File imSharedInstallLocation) {
        File outputFile = FileUtils.getFile(wasSourcesLocation, "install.xml");
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // agent-input element
            Document doc = docBuilder.newDocument();
            Element agentInputElement = doc.createElement("agent-input");
            agentInputElement.setAttribute("acceptLicense", "true");
            doc.appendChild(agentInputElement);
            // profile element
            Element profileElement = doc.createElement("profile");
            profileElement.setAttribute("installLocation", wasInstallLocation.getAbsolutePath());
            profileElement.setAttribute("id", "IBM WebSphere Application Server V8.5");
            agentInputElement.appendChild(profileElement);
            // data elements
            Element data1Element = doc.createElement("data");
            data1Element.setAttribute("key", "eclipseLocation");
            data1Element.setAttribute("value", wasInstallLocation.getAbsolutePath());
            profileElement.appendChild(data1Element);
            Element data2Element = doc.createElement("data");
            data2Element.setAttribute("key", "user.import.profile");
            data2Element.setAttribute("value", "false");
            profileElement.appendChild(data2Element);
            Element data3Element = doc.createElement("data");
            data3Element.setAttribute("key", "cic.selector.os");
            data3Element.setAttribute("value", "win32");
            profileElement.appendChild(data3Element);
            Element data4Element = doc.createElement("data");
            data4Element.setAttribute("key", "cic.selector.ws");
            data4Element.setAttribute("value", "win32");
            profileElement.appendChild(data4Element);
            Element data5Element = doc.createElement("data");
            data5Element.setAttribute("key", "cic.selector.arch");
            data5Element.setAttribute("value", "x86");
            profileElement.appendChild(data5Element);
            Element data6Element = doc.createElement("data");
            data6Element.setAttribute("key", "cic.selector.nl");
            data6Element.setAttribute("value", "en");
            profileElement.appendChild(data6Element);
            // server element
            Element serverElement = doc.createElement("server");
            agentInputElement.appendChild(serverElement);
            // repository elements
            Element repository1Element = doc.createElement("repository");
            repository1Element.setAttribute("location", wasSourcesLocation.getAbsolutePath());
            serverElement.appendChild(repository1Element);
            Element repository2Element = doc.createElement("repository");
            repository2Element.setAttribute("location", javaSourcesLocation.getAbsolutePath());
            serverElement.appendChild(repository2Element);
            // install element
            Element installElement = doc.createElement("install");
            installElement.setAttribute("modify", "false");
            agentInputElement.appendChild(installElement);
            // offering elements
            Element offering1Element = doc.createElement("offering");
            offering1Element.setAttribute("features", "core.feature,ejbdeploy,thinclient,embeddablecontainer,com.ibm.sdk.6_64bit");
            offering1Element.setAttribute("id", "com.ibm.websphere.BASE.v85");
            offering1Element.setAttribute("version", "8.5.0.20120501_1108");
            offering1Element.setAttribute("profile", "IBM WebSphere Application Server V8.5");
            offering1Element.setAttribute("installFixes", "none");
            installElement.appendChild(offering1Element);
            Element offering2Element = doc.createElement("offering");
            offering2Element.setAttribute("features", "com.ibm.sdk.7");
            offering2Element.setAttribute("id", "com.ibm.websphere.IBMJAVA.v70");
            offering2Element.setAttribute("version", "7.0.1000.20120424_1539");
            offering2Element.setAttribute("profile", "IBM WebSphere Application Server V8.5");
            offering2Element.setAttribute("installFixes", "none");
            installElement.appendChild(offering2Element);
            // preference elements
            Element preference1Element = doc.createElement("preference");
            preference1Element.setAttribute("name", "com.ibm.cic.common.core.preferences.eclipseCache");
            preference1Element.setAttribute("value", imSharedInstallLocation.getAbsolutePath());
            agentInputElement.appendChild(preference1Element);
            Element preference2Element = doc.createElement("preference");
            preference2Element.setAttribute("name", "com.ibm.cic.common.core.preferences.connectTimeout");
            preference2Element.setAttribute("value", "30");
            agentInputElement.appendChild(preference2Element);
            Element preference3Element = doc.createElement("preference");
            preference3Element.setAttribute("name", "com.ibm.cic.common.core.preferences.readTimeout");
            preference3Element.setAttribute("value", "45");
            agentInputElement.appendChild(preference3Element);
            Element preference4Element = doc.createElement("preference");
            preference4Element.setAttribute("name", "com.ibm.cic.common.core.preferences.downloadAutoRetryCount");
            preference4Element.setAttribute("value", "0");
            agentInputElement.appendChild(preference4Element);
            Element preference5Element = doc.createElement("preference");
            preference5Element.setAttribute("name", "offering.service.repositories.areUsed");
            preference5Element.setAttribute("value", "true");
            agentInputElement.appendChild(preference5Element);
            Element preference6Element = doc.createElement("preference");
            preference6Element.setAttribute("name", "com.ibm.cic.common.core.preferences.ssl.nonsecureMode");
            preference6Element.setAttribute("value", "false");
            agentInputElement.appendChild(preference6Element);
            Element preference7Element = doc.createElement("preference");
            preference7Element.setAttribute("name", "com.ibm.cic.common.core.preferences.http.disablePreemptiveAuthentication");
            preference7Element.setAttribute("value", "false");
            agentInputElement.appendChild(preference7Element);
            Element preference8Element = doc.createElement("preference");
            preference8Element.setAttribute("name", "http.ntlm.auth.kind");
            preference8Element.setAttribute("value", "NTLM");
            agentInputElement.appendChild(preference8Element);
            Element preference9Element = doc.createElement("preference");
            preference9Element.setAttribute("name", "http.ntlm.auth.enableIntegrated.win32");
            preference9Element.setAttribute("value", "true");
            agentInputElement.appendChild(preference9Element);
            Element preference10Element = doc.createElement("preference");
            preference10Element.setAttribute("name", "com.ibm.cic.common.core.preferences.preserveDownloadedArtifacts");
            preference10Element.setAttribute("value", "true");
            agentInputElement.appendChild(preference10Element);
            Element preference11Element = doc.createElement("preference");
            preference11Element.setAttribute("name", "com.ibm.cic.common.core.preferences.keepFetchedFiles");
            preference11Element.setAttribute("value", "false");
            agentInputElement.appendChild(preference11Element);
            Element preference12Element = doc.createElement("preference");
            preference12Element.setAttribute("name", "PassportAdvantageIsEnabled");
            preference12Element.setAttribute("value", "false");
            agentInputElement.appendChild(preference12Element);
            Element preference13Element = doc.createElement("preference");
            preference13Element.setAttribute("name", "com.ibm.cic.common.core.preferences.searchForUpdates");
            preference13Element.setAttribute("value", "false");
            agentInputElement.appendChild(preference13Element);
            Element preference14Element = doc.createElement("preference");
            preference14Element.setAttribute("name", "com.ibm.cic.agent.ui.displayInternalVersion");
            preference14Element.setAttribute("value", "false");
            agentInputElement.appendChild(preference14Element);
            Element preference15Element = doc.createElement("preference");
            preference15Element.setAttribute("name", "com.ibm.cic.common.sharedUI.showErrorLog");
            preference15Element.setAttribute("value", "true");
            agentInputElement.appendChild(preference15Element);
            Element preference16Element = doc.createElement("preference");
            preference16Element.setAttribute("name", "com.ibm.cic.common.sharedUI.showWarningLog");
            preference16Element.setAttribute("value", "true");
            agentInputElement.appendChild(preference16Element);
            Element preference17Element = doc.createElement("preference");
            preference17Element.setAttribute("name", "com.ibm.cic.common.sharedUI.showNoteLog");
            preference17Element.setAttribute("value", "true");
            agentInputElement.appendChild(preference17Element);
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(outputFile);
            // Output to console for testing
//            StreamResult result = new StreamResult(System.out);
            transformer.transform(source, result);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (TransformerException tfe) {
            tfe.printStackTrace();
        }
        return outputFile;
    }

    public void run() throws IOException {
        File managerZipSourcesDir = FileUtils.getFile(context.getManagerZipSourcesLocation());
        File was85ZipSourcesDir = FileUtils.getFile(context.getWas85ZipSourcesLocation());
        File java7ZipSourcesDir = FileUtils.getFile(context.getJava7ZipSourcesLocation());

        File was85Zip1SourcesDiskDir = FileUtils.getFile(context.getWas85ZipSourcesLocation(), "disk1");
        File was85Zip2SourcesDiskDir = FileUtils.getFile(context.getWas85ZipSourcesLocation(), "disk2");
        File was85Zip3SourcesDiskDir = FileUtils.getFile(context.getWas85ZipSourcesLocation(), "disk3");
        File java7Zip1SourcesDiskDir = FileUtils.getFile(context.getJava7ZipSourcesLocation(), "disk1");
        File java7Zip2SourcesDiskDir = FileUtils.getFile(context.getJava7ZipSourcesLocation(), "disk2");
        File java7Zip3SourcesDiskDir = FileUtils.getFile(context.getJava7ZipSourcesLocation(), "disk3");

        if (!managerZipSourcesDir.exists()) {
            archiveFactory.createArchive(context.getManagerZipPackageUrl()).unpack(new File(context.getManagerZipSourcesLocation()));
        } else {
            LOGGER.info("WAS Manager installation files already exist in '" + context.getManagerZipSourcesLocation() + "'. Skipping download.");
        }
        if (!was85Zip1SourcesDiskDir.exists()) {
            archiveFactory.createArchive(context.getWas85Zip1PackageUrl()).unpack(new File(context.getWas85ZipSourcesLocation()));
        } else {
            LOGGER.info("WAS 1/3 installation files already exist in '" + context.getWas85ZipSourcesLocation() + "'. Skipping download.");
        }
        if (!was85Zip2SourcesDiskDir.exists()) {
            archiveFactory.createArchive(context.getWas85Zip2PackageUrl()).unpack(new File(context.getWas85ZipSourcesLocation()));
        } else {
            LOGGER.info("WAS 2/3 installation files already exist in '" + context.getWas85ZipSourcesLocation() + "'. Skipping download.");
        }
        if (!was85Zip3SourcesDiskDir.exists()) {
            archiveFactory.createArchive(context.getWas85Zip3PackageUrl()).unpack(new File(context.getWas85ZipSourcesLocation()));
        } else {
            LOGGER.info("WAS 3/3 installation files already exist in '" + context.getWas85ZipSourcesLocation() + "'. Skipping download.");
        }
        if (!java7Zip1SourcesDiskDir.exists()) {
            archiveFactory.createArchive(context.getJava7Zip1PackageUrl()).unpack(new File(context.getJava7ZipSourcesLocation()));
        } else {
            LOGGER.info("IBM Java 7 1/3 installation files already exist in '" + context.getJava7ZipSourcesLocation() + "'. Skipping download.");
        }
        if (!java7Zip2SourcesDiskDir.exists()) {
            archiveFactory.createArchive(context.getJava7Zip2PackageUrl()).unpack(new File(context.getJava7ZipSourcesLocation()));
        } else {
            LOGGER.info("IBM Java 7 2/3  installation files already exist in '" + context.getJava7ZipSourcesLocation() + "'. Skipping download.");
        }
        if (!java7Zip3SourcesDiskDir.exists()) {
            archiveFactory.createArchive(context.getJava7Zip3PackageUrl()).unpack(new File(context.getJava7ZipSourcesLocation()));
        } else {
            LOGGER.info("IBM Java 7 3/3  installation files already exist in '" + context.getJava7ZipSourcesLocation() + "'. Skipping download.");
        }
        File managerInstallResponseFile = createManagerSilentInstallXml(managerZipSourcesDir,
                FileUtils.getFile(context.getManagerInstallLocation(), "eclipse"));
        File wasInstallResponseFile = createWasSilentInstallXml(was85ZipSourcesDir, java7ZipSourcesDir,
                FileUtils.getFile(context.getInstallWasLocation()), FileUtils.getFile(context.getImSharedLocation()));

        try {
            runManagerInstallationProcess();
            runWasInstallationProcess(wasInstallResponseFile);
            setupJava();
            createProfile();
        } catch (InterruptedException var3) {
            throw new IllegalStateException(var3);
        }
        LOGGER.info("Flow has finished.");
    }

    protected void runManagerInstallationProcess() throws InterruptedException {
        File installLocation = FileUtils.getFile(context.getManagerInstallLocation());
        File installExecutable = FileUtils.getFile(context.getManagerZipSourcesLocation(), "installc.exe");
        int responseCode = getExecutionBuilder(LOGGER, installExecutable.toString())
                .args(new String[]{installLocation.exists() ? "-reinstallIM" : "", "-acceptLicense"})
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("WAS Manager installation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of WAS Manager failed (%d)", new Object[]{responseCode}));
        }
    }

    protected void runWasInstallationProcess(File wasInstallResponseFile) throws InterruptedException {
        File installExecutable = FileUtils.getFile(context.getManagerInstallLocation(), "eclipse/tools/imcl.exe");
        int responseCode = getExecutionBuilder(LOGGER, installExecutable.toString())
                .args(new String[]{"input", wasInstallResponseFile.getAbsolutePath(), "-acceptLicense"})
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("WAS Manager installation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of WAS Manager failed (%d)", new Object[]{responseCode}));
        }
    }

//    public static void main(String[] args) {
//        File was1SourcesLocation = FileUtils.getFile("c:\\tmp\\aa");
//        File was2SourcesLocation = FileUtils.getFile("c:\\tmp\\bb");
//        File was3SourcesLocation = FileUtils.getFile("c:\\tmp\\cc");
//        File was4SourcesLocation = FileUtils.getFile("c:\\tmp\\dd");
//        File was5SourcesLocation = FileUtils.getFile("c:\\tmp\\ee");
//        File was6SourcesLocation = FileUtils.getFile("c:\\tmp\\ff");
//        File was7SourcesLocation = FileUtils.getFile("c:\\tmp\\gg");
//        File was8SourcesLocation = FileUtils.getFile("c:\\tmp\\hh");
//        createWasSilentInstallXml(was1SourcesLocation, was2SourcesLocation, was3SourcesLocation,was4SourcesLocation,
//                was5SourcesLocation, was6SourcesLocation, was7SourcesLocation, was8SourcesLocation);
//    }

    protected void setupJava() throws InterruptedException {
        File installExecutable = FileUtils.getFile(context.getInstallWasLocation(), "bin/managesdk.bat");
        int responseCode1 = getExecutionBuilder(LOGGER, installExecutable.toString())
                .args(new String[]{"-setCommandDefault", "-sdkname", context.getSdkName()})
                .build()
                .go();
        int responseCode2 = getExecutionBuilder(LOGGER, installExecutable.toString())
                .args(new String[]{"-setNewProfileDefault", "-sdkname", context.getSdkName()})
                .build()
                .go();
        if (responseCode1 == 0 && responseCode2 == 0) {
            LOGGER.info("IBM Java installation completed SUCCESSFULLY! Congratulations!");
        } else {
            throw new IllegalStateException(String.format("IBM Java installation failed (%d)", new Object[]{responseCode1}));
        }
    }

    protected void createProfile() throws InterruptedException {
        File installExecutable = FileUtils.getFile(context.getInstallWasLocation(), "bin/manageprofiles.bat");
        int responseCode = getExecutionBuilder(LOGGER, installExecutable.toString())
                .args(new String[]{"-create",
                        "-profileName",
                        context.getProfileName(),
                        "-profilePath",
                        context.getProfilePath(),
                        "-templatePath",
                        context.getTemplatePath(),
                        "-cellName",
                        context.getCellName(),
                        "-hostName",
                        context.getHostName(),
                        "-nodeName",
                        context.getNodeName(),
                        Boolean.TRUE.equals(context.getDef()) ? "-isDefault" : "",
                        "-enableAdminSecurity",
                        Boolean.TRUE.equals(context.getEnableAdminSecurity()) ? "true" : "false",
                        "-adminUserName",
                        context.getAdminUserName(),
                        "-adminPassword",
                        context.getAdminPassword(),
                        "-winserviceCheck",
                        Boolean.TRUE.equals(context.getWinserviceCheck()) ? "true" : "false"})
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("WAS Manager installation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of WAS Manager failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
