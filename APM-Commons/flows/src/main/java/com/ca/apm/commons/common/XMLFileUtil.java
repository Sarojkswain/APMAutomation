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
 * 
 * Author : JAMSA07/ SANTOSH JAMMI
 * Date : 12/20/2015
 */

package com.ca.apm.commons.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XMLFileUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(XMLFileUtil.class);

    /**
     * Find specified path in the xml file
     *
     * @param xmlFile
     * @param expression
     * @return
     * @throws Exception
     */
    public ArrayList<String> parseXmlFile(String xmlFile, String expression) throws Exception {

        
        LOGGER.info ("parsing xml file: " + xmlFile + " with xpath expression: " + expression);
        FileInputStream file = new FileInputStream(new File(xmlFile));

        ArrayList<String> data = new ArrayList<String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(file);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(expression);

        System.out.println(expression);
        NodeList nodeList =
            (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.println(nodeList.item(i).getFirstChild().getNodeValue());
        }

        return data;
    }

    /**
     *
     * Update xml file by providing required xpath
     * @param xmlFile
     * @param expression
     * @param oldValue
     * @param newValue
     * @return
     * @throws Exception
     */
    public int updateXmlFile(String xmlFile, String expression, String oldValue, String newValue) throws Exception {
        LOGGER.info ("parsing xml file: " + xmlFile + " with xpath expression: " + expression);
        FileInputStream file = new FileInputStream(new File(xmlFile));

        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(file);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(expression);

        System.out.println(expression);
        NodeList nodeList =
            (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.println(nodeList.item(i).getFirstChild().getNodeValue().equalsIgnoreCase(oldValue));
            if(nodeList.item(i).getFirstChild().getNodeValue().equalsIgnoreCase(oldValue))
            {
                nodeList.item(i).getFirstChild().setNodeValue(newValue);
            }
        }

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StreamResult result = new StreamResult(xmlFile);
        transformer.transform(new DOMSource(doc), result);

        return 1;
    }
    
    
    /**
    *
    * Update xml file by providing required xpath
    * @param xmlFile
    * @param expression
    * @param oldValue
    * @param newValue
    * @return
    * @throws Exception
    */
   public int updateAttributeInXmlFile(String xmlFile, String expression, String oldValue, String newValue) throws Exception {
       LOGGER.info ("Updating xml file: " + xmlFile + " with xpath expression: " + expression);
       FileInputStream file = new FileInputStream(new File(xmlFile));

       DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
       domFactory.setNamespaceAware(true);
       DocumentBuilder builder = domFactory.newDocumentBuilder();
       Document doc = builder.parse(file);

//       XPathFactory factory = XPathFactory.newInstance();
//       XPath xpath = factory.newXPath();
//       XPathExpression expr = xpath.compile(expression);
//
//       System.out.println(expression);
//       NodeList nodeList =
//           (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
//       for (int i = 0; i < nodeList.getLength(); i++) {
//           System.out.println(nodeList.item(i).getFirstChild().getNodeValue().equalsIgnoreCase(oldValue));
//           if(nodeList.item(i).getFirstChild().getNodeValue().equalsIgnoreCase(oldValue))
//           {
//               nodeList.item(i).getFirstChild().setNodeValue(newValue);
//           }
//       }
       
       XPath xpath = XPathFactory.newInstance().newXPath();
       NodeList nodes = (NodeList)xpath.evaluate(expression,
                                                 doc, XPathConstants.NODESET);

       // 3- Make the change on the selected nodes
       for (int idx = 0; idx < nodes.getLength(); idx++) {
           Node value = nodes.item(idx).getAttributes().getNamedItem("value");
           String val = value.getNodeValue();
           value.setNodeValue(val.replaceAll(oldValue, newValue));
       }

       Transformer transformer = TransformerFactory.newInstance().newTransformer();
       transformer.setOutputProperty(OutputKeys.INDENT, "yes");
       StreamResult result = new StreamResult(xmlFile);
       transformer.transform(new DOMSource(doc), result);
       
       return 1;
   }


    public int getCountForTag(String xmlFile, String expression) throws Exception {

        
        LOGGER.info ("parsing xml file: " + xmlFile + " with xpath expression: " + expression);
        FileInputStream file = new FileInputStream(new File("test.xml"));

        ArrayList<String> data = new ArrayList<String>();
        DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
        domFactory.setNamespaceAware(true);
        DocumentBuilder builder = domFactory.newDocumentBuilder();
        Document doc = builder.parse(file);

        XPathFactory factory = XPathFactory.newInstance();
        XPath xpath = factory.newXPath();
        XPathExpression expr = xpath.compile(expression);

        System.out.println(expression);
        NodeList nodeList =
            (NodeList) xpath.compile(expression).evaluate(doc, XPathConstants.NODESET);
        

        return nodeList.getLength();
    }
    
}
