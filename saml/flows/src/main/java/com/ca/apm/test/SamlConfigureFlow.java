/*
 * Copyright (c) 2014 CA. All rights reserved.
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
package com.ca.apm.test;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;

@Flow
public class SamlConfigureFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlConfigureFlow.class);

    @FlowContext
    private SamlConfigureFlowContext context;

    @Override
    public void run() throws Exception {
        String apmRootDir = context.getApmRootDir();
        
        modifyIntroscopeConfig(new File(apmRootDir+"/config/IntroscopeEnterpriseManager.properties"));
        modifyUsers(new File(apmRootDir + "/config/users.xml"));
    }

    private void modifyIntroscopeConfig(final File configFile) throws IOException {
        if (configFile.exists())
        {
            String encoding = System.getProperty("file.encoding");
            FileUtils.write(configFile, FileUtils.readFileToString(configFile, encoding).
                replaceAll("introscope.saml.enable=false", "introscope.saml.enable=true").
                replaceAll("introscope.saml.internalIdp.enable=false", 
                    "introscope.saml.internalIdp.enable=true"), encoding);
            }
        else
        {
            LOGGER.error("{} does not exist", configFile.getCanonicalPath());
        }
    }


    private void modifyUsers(final File usersFile) throws IOException,
        ParserConfigurationException, SAXException, TransformerException {
        if (usersFile.exists()) {
            // load xml file into DOM
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder();
            Document doc = dBuilder.parse(usersFile);

            NodeList lusers = doc.getElementsByTagName("users");
            Node users = lusers.item(0);

            // add new user with no Group to simulate authentication error
            Element newUser = doc.createElement("user");
            newUser.setAttribute("password", "");
            newUser.setAttribute("name", "user");
            users.appendChild(newUser);

            // write back to file
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(usersFile);
            transformer.transform(source, result);
        } else {
            LOGGER.error("{} does not exist", usersFile.getCanonicalPath());
        }

    }
}
