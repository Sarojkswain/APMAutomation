package com.ca.apm.tests.utils;


import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.persistence.internal.sessions.remote.SequencingFunctionCall.GetNextValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.ca.apm.commons.coda.common.Util;

public class XmlModifications {


    public Document getDocObject(String fileXml) throws ParserConfigurationException, SAXException, IOException{
        File xmlFile = new File(fileXml);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder;

        dBuilder = dbFactory.newDocumentBuilder();
        Document doc  = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        return doc;
    }
    public void addElement(Document doc) {
        NodeList employees = doc.getElementsByTagName("Set");
        Element emp = null;

        //loop for each employee
        for(int i=0; i<employees.getLength();i++){
            emp = (Element) employees.item(i);
            String id = emp.getAttribute("name");
            if(id.equalsIgnoreCase("validateCertificates")){
                Element newChild = doc.createElement("new.value");
                newChild.appendChild(doc.createTextNode("new value"));
                emp.appendChild(newChild);
            }
        }
    }

    public void updateXmlDoc(String xmlFile, Document doc) throws TransformerException{
        doc.getDocumentElement().normalize();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(xmlFile));
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
    }
    public void addAttribute(Document doc) {
        NodeList employees = doc.getElementsByTagName("property");
        Element emp = null;

        //loop for each employee
        for(int i=0; i<employees.getLength();i++){
            emp = (Element) employees.item(i);
            String id = emp.getAttribute("name");
            if(id.equalsIgnoreCase("usersFile")){
                ((Element)emp.getChildNodes()).setAttribute("new.attribute" , "new.attribute.value");
            }
        }
    }
    public void deleteElement(Document doc) {
        NodeList clamps = doc.getElementsByTagName("grant");
        Element emp = null;
        //loop for each employee
        for(int i=0; i<clamps.getLength();i++){
            emp = (Element) clamps.item(i);
            String id = emp.getAttribute("group");
            System.out.println("id : "+id);
            if(id.equalsIgnoreCase("Admin")){
                Node genderNode = emp.getElementsByTagName("grant").item(0);
                emp.removeChild(genderNode);
            }
            /* Node genderNode = emp.getElementsByTagName("gender").item(0);
            emp.removeChild(genderNode);*/
        }

    }
    public void deleteAttribute(Document doc) {
        NodeList clamps = doc.getElementsByTagName("property");
        Element emp = null;
        //loop for each employee
        for(int i=0; i<clamps.getLength();i++){
            emp = (Element) clamps.item(i);
            String id = emp.getAttribute("name");
            if(id.equalsIgnoreCase("usersFile")){
                ((Element)emp.getChildNodes()).removeAttribute("name");
            }
        }
    }
    public void updateElementValue(Document doc) {
        NodeList clamps = doc.getElementsByTagName("Set");
        Element emp = null;
        //loop for each employee
        for(int i=0; i<clamps.getLength();i++){
            emp = (Element) clamps.item(i);
            String id = emp.getAttribute("name");
            System.out.println("id : "+id);
            if(id.equalsIgnoreCase("validateCertificates")){
                emp.setNodeValue("false.new");
            }

        }
    }
    
    public void updateAttributeValue(Document doc) {
        NodeList clamps = doc.getElementsByTagName("CyclicBuffer");
        Element emp = null;
        //loop for each employee
        for(int i=0; i<clamps.getLength();i++){
            emp = (Element) clamps.item(i);
            String id = emp.getAttribute("file");
            if(id.equalsIgnoreCase("dump.log")){
                System.out.println("element found : "+emp.getAttribute("file"));
                ((Element) emp.getChildNodes()).setAttribute("size", "size.new");
            }else{
                System.out.println("no match for id : "+id);

            }
        }
    }

    public void addElement(String fileXml, String primaryElements, String key , String key_value , String element, String value) {
        Document doc;
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList employees = doc.getElementsByTagName(primaryElements);
            Element emp = null;

            //loop for each employee
            for(int i=0; i<employees.getLength();i++)
            {
                emp = (Element) employees.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {   
                Element newChild = doc.createElement(element);
                newChild.appendChild(doc.createTextNode(value));
                emp.appendChild(newChild);
                }
            }
            updateXmlDoc(fileXml, doc);

        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) 
        {        }

    }

    public void addAttribute(String fileXml ,String primaryElements, String key , String key_value, String attribute, String value, int attribute_position) {

        Document doc;
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList employees = doc.getElementsByTagName(primaryElements);
            Element emp = null;

            //loop for each employee
            for(int i=0; i<employees.getLength();i++)
            {
                emp = (Element) employees.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {
                    if(attribute_position!=0)
                        ((Element)emp.getChildNodes().item(attribute_position)).setAttribute(attribute, value);
                    else
                        ((Element)emp.getChildNodes()).setAttribute(attribute, value);
                }

            }

            updateXmlDoc(fileXml, doc);

        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) 
        {        }
    }

    public void deleteElement(String fileXml, String primaryElements,String key , String key_value , String element, int level) {

        Document doc;
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList clamps = doc.getElementsByTagName(primaryElements);
            Element emp = null;
            //loop for each employee
            for(int i=0; i<clamps.getLength();i++)
            {
                emp = (Element) clamps.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {
                    if(level!=0){
                        Node genderNode = emp.getElementsByTagName(element).item(0);
                        emp.getChildNodes().item(level).removeChild(genderNode);
                    }
                    else{
                        Node genderNode = emp.getElementsByTagName(element).item(0);
                        emp.removeChild(genderNode);}
                }

            }
            updateXmlDoc(fileXml, doc);

        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) 
        {        }
    }

    public void deleteAttribute(String fileXml ,String primaryElements, String key , String key_value, String attribute, int attribute_position) {

        Document doc;
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList clamps = doc.getElementsByTagName(primaryElements);
            Element emp = null;
            //loop for each employee
            for(int i=0; i<clamps.getLength();i++)
            {
                emp = (Element) clamps.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {
                    if(attribute_position!=0)
                        ((Element)emp.getChildNodes().item(attribute_position)).removeAttribute(attribute);
                    else
                        ((Element)emp.getChildNodes()).removeAttribute(attribute);
                }

            }
            updateXmlDoc(fileXml, doc);

        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) 
        {        }
    }

    public void updateElementValue(String fileXml, String primaryElements, String key , String key_value , String element, String value) {
        Document doc;
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList clamps = doc.getElementsByTagName(primaryElements);
            Element emp = null;
            //loop for each employee
            for(int i=0; i<clamps.getLength();i++)
            {
                emp = (Element) clamps.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {
                    Node name = emp.getElementsByTagName(element).item(0).getFirstChild();
                    name.setNodeValue(value);
                }
            }
            updateXmlDoc(fileXml, doc);

        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e1) 
        {        }
    }

    public String getElementValue(String fileXml, String primaryElements, String key , String key_value , String element) {
        Document doc;
        String value="";
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList clamps = doc.getElementsByTagName(primaryElements);
            Element emp = null;
            //loop for each employee
            for(int i=0; i<clamps.getLength();i++)
            {
                emp = (Element) clamps.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {
                    Node name = emp.getElementsByTagName(element).item(0).getFirstChild();
                    value = name.getNodeValue();
                }
            }
            return value;
        } catch (SAXException | ParserConfigurationException | IOException e1) 
        {              return null;
      }
    }

    
    /*
     * <xml... >
     *      <primaryElements>
     *          <clamp1 key=key_value> 
     *          <clamp2>           
     *      </primaryElements>
     */
    public void updateAttributeValue(String fileXml, String primaryElements, String key , String key_value, String attribute, String value, int attribute_position) {

        Document doc;
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList clamps = doc.getElementsByTagName(primaryElements);
            Element emp = null;
            //loop for each employee
            for(int i=0; i<clamps.getLength();i++)
            {
                emp = (Element) clamps.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {
                    if(attribute_position!=0)
                        ((Element)emp.getChildNodes().item(attribute_position)).setAttribute(attribute, value);
                    else
                        ((Element)emp.getChildNodes()).setAttribute(attribute, value);

                }else
                {
                    System.out.println("no match found");

                }
            }

            updateXmlDoc(fileXml, doc);

        } catch (SAXException | ParserConfigurationException | IOException | TransformerException e1)
        {        }
    }

    public String getAttributeValue(String fileXml, String primaryElements, String key , String key_value, String attribute, int attribute_position) {

        Document doc;
        String value="";
        try 
        {
            doc =   getDocObject(fileXml);

            NodeList clamps = doc.getElementsByTagName(primaryElements);
            Element emp = null;
            //loop for each employee
            for(int i=0; i<clamps.getLength();i++)
            {
                emp = (Element) clamps.item(i);
                String id = emp.getAttribute(key);
                if(id.equalsIgnoreCase(key_value))
                {
                    if(attribute_position!=0)
                        value = ((Element)emp.getChildNodes().item(attribute_position)).getAttribute(attribute);
                    else
                        value = ((Element)emp.getChildNodes()).getAttribute(attribute);

                }else
                {
                    System.out.println("no match found");

                }
            }
            return value;

        } catch (SAXException | ParserConfigurationException | IOException e1)
        {   return value;     }
    }
    
    public void lineCommentAttributeInXmlFile (String file, String line) throws Exception{
        Util.replaceLine(file, line, "<!--"+line+"-->");    //make changes
    }
}