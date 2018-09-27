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
package com.ca.apm.tests.utils.emutils;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;



public class EnterpriseManagerHibernateTestConfig {

    private String dbConfigPath;

    private Document dbConfig;

    public EnterpriseManagerHibernateTestConfig(EmConfiguration config)
        throws ParserConfigurationException, SAXException, IOException {

        dbConfigPath =
            config.getInstallPath() + File.separator + "config" + File.separator
                + "tess-db-cfg.xml";

        setDBProperties();

    }

    /**
     * Loads DB config from configured path.
     * 
     * @author Artur
     */
    private void setDBProperties() throws ParserConfigurationException, SAXException, IOException {
        File file = new File(dbConfigPath);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setValidating(false);
        dbf.setNamespaceAware(true);
        dbf.setFeature("http://xml.org/sax/features/namespaces", false);
        dbf.setFeature("http://xml.org/sax/features/validation", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
        dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

        DocumentBuilder db = dbf.newDocumentBuilder();
        dbConfig = db.parse(file);
        dbConfig.getDocumentElement().normalize();
    }

    /**
     * Internal method for getting node by one of its values
     * 
     * @author Artur
     */

    private Node getNodeByNamedNamedItem(NodeList nodes, String item, String value) {

        for (int n = 0; n < nodes.getLength(); n++) {
            Node node = nodes.item(n);
            if (node.getAttributes().getNamedItem(item).toString().contains(value)) {
                return node;
            }
        }
        return null;
    }

    /**
     * Method to change one of the nodes, by its name. For Example:
     * config.setDBPropertyValue("em.dbtype", "Postgres")
     * 
     * @author Artur
     */
    public void setDBPropertyValue(String name, String newValue)
        throws TransformerFactoryConfigurationError, TransformerException {
        NodeList configNodes = dbConfig.getElementsByTagName("property");
        Node node = getNodeByNamedNamedItem(configNodes, "name", name);
        if (node == null) {
            throw new RuntimeException("DB property " + name + " is not found");
        }

        node.setTextContent(newValue);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(new File(dbConfigPath));
        Source input = new DOMSource(dbConfig);
        transformer.transform(input, output);
    }

    /**
     * Return value of some named property
     * 
     * @author Artur
     */
    public String getValueOfDBPropertyByName(String name) {

        NodeList configNodes = dbConfig.getElementsByTagName("property");
        Node node = getNodeByNamedNamedItem(configNodes, "name", name);
        if (node != null) {
            return node.getTextContent();
        }
        return "Not Found";
    }


}
