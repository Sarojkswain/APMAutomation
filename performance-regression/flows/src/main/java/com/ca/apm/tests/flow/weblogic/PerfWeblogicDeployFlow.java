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
package com.ca.apm.tests.flow.weblogic;

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
import java.net.URL;

/**
 * @author Erik Melecky (meler02@ca.com)
 */
@Flow
public class PerfWeblogicDeployFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(PerfWeblogicDeployFlow.class);
    @FlowContext
    private PerfWeblogicDeployFlowContext context;

    protected File createSilentInstallFile(File sourcesLocation, File beaHome, File installDir, File customJvm) {
        File outputFile = FileUtils.getFile(sourcesLocation, "install.xml");
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            // bea-installer element
            Document doc = docBuilder.newDocument();
            Element beaInstallerElement = doc.createElement("bea-installer");
            doc.appendChild(beaInstallerElement);
            // input-fields element
            Element inputFieldsElement = doc.createElement("input-fields");
            beaInstallerElement.appendChild(inputFieldsElement);
            // data-value elements
            Element data1Element = doc.createElement("data-value");
            data1Element.setAttribute("name", "BEAHOME");
            data1Element.setAttribute("value", beaHome.getAbsolutePath());
            inputFieldsElement.appendChild(data1Element);
            Element data2Element = doc.createElement("data-value");
            data2Element.setAttribute("name", "WLS_INSTALL_DIR");
            data2Element.setAttribute("value", installDir.getAbsolutePath());
            inputFieldsElement.appendChild(data2Element);
            Element data3Element = doc.createElement("data-value");
            data3Element.setAttribute("name", "COMPONENT_PATHS");
            data3Element.setAttribute("value", "WebLogic Server/Core Application Server|WebLogic Server/Administration Console|" +
                    "WebLogic Server/Configuration Wizard and Upgrade Framework|WebLogic Server/WebLogic JDBC Drivers|" +
                    "WebLogic Server/Third Party JDBC Drivers|WebLogic Server/Server Examples");
            inputFieldsElement.appendChild(data3Element);
            Element data4Element = doc.createElement("data-value");
            data4Element.setAttribute("name", "INSTALL_NODE_MANAGER_SERVICE");
            data4Element.setAttribute("value", "no");
            inputFieldsElement.appendChild(data4Element);
            Element data5Element = doc.createElement("data-value");
            data5Element.setAttribute("name", "NODEMGR_PORT");
            data5Element.setAttribute("value", "5559");
            inputFieldsElement.appendChild(data5Element);
            Element data6Element = doc.createElement("data-value");
            data6Element.setAttribute("name", "INSTALL_SHORTCUT_IN_ALL_USERS_FOLDER");
            data6Element.setAttribute("value", "no");
            inputFieldsElement.appendChild(data6Element);
            Element data7Element = doc.createElement("data-value");
            data7Element.setAttribute("name", "LOCAL_JVMS");
            data7Element.setAttribute("value", customJvm == null ? "" : customJvm.getAbsolutePath());
            inputFieldsElement.appendChild(data7Element);
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

    public void run() throws Exception {
        File sourcesLocation = FileUtils.getFile(context.getSourcesLocation());
        File beaHome = FileUtils.getFile(context.getBeaHome());
        File installDir = FileUtils.getFile(context.getInstallDir());
        File customJvm = context.getCustomJvm() == null ? null : FileUtils.getFile(context.getCustomJvm());


        URL webLogicInstallUrl = this.context.getInstallerUrl();
        File installerFile = new File(sourcesLocation, this.context.getInstallerFileName());
        if (!installerFile.exists()) {
            LOGGER.info("Downloading WebLogic artefact");
            this.archiveFactory.createArtifact(webLogicInstallUrl).download(installerFile);
        } else {
            LOGGER.info("Weblogic installation files already exist in '" + installerFile.getAbsolutePath() + "'. Skipping download.");
        }
        installerFile.setExecutable(true);
        LOGGER.info("Building WLS response file");
        File installResponseFile = createSilentInstallFile(sourcesLocation, beaHome, installDir, customJvm);
        LOGGER.info("Installing WLS");
        runInstallationProcess(installerFile, installResponseFile);
        LOGGER.info("Flow has finished.");
    }

    protected void runInstallationProcess(File installerFile, File installResponseFile) throws InterruptedException {
        int responseCode = getExecutionBuilder(LOGGER, installerFile.toString())
                .args(new String[]{"-mode=silent", "-silent_xml=" + installResponseFile.getAbsolutePath()})
                .build()
                .go();
        switch (responseCode) {
            case 0:
                LOGGER.info("WLS installation completed SUCCESSFULLY! Congratulations!");
                return;
            default:
                throw new IllegalStateException(String.format("Launching silent installation of WLS failed (%d)", new Object[]{responseCode}));
        }
    }

    protected Execution.Builder getExecutionBuilder(Logger logger, String executable) {
        return new Execution.Builder(executable, logger);
    }
}
