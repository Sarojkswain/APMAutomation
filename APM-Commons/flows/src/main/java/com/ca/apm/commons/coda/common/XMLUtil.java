package com.ca.apm.commons.coda.common;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Administrator
 * 
 */
public class XMLUtil {

    public static final String SUCCESS_MESSAGE = "Successful";
    private static Logger LOGGER = Logger.getLogger(XMLUtil.class);
    int value_changed = 0;
    int done_parsing = 0;

    public static Document getDocument(String xmlFilePath) throws Exception {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(xmlFilePath);
        return document;
    }

    public static void writeToXMLFile(Document document, String xmlFilePath) throws Exception {
        DOMSource source = new DOMSource(document);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");

            StreamResult result = new StreamResult(xmlFilePath);
            transformer.transform(source, result);
        } catch (Exception e) {
            LOGGER.error("Failed to write into xml file '" + xmlFilePath + "': ", e);
            throw e;
        }

    }

    /**
     * Checks if the element contains the attribute name and corresponding value
     */

    public static boolean containsElements(String xmlFilePath, String element, String attrName,
        String attrValue) {
        LOGGER
            .info("Entering com.apm.automation.XMLUtil.containsElements(String xmlFilePath, String element, String attrName, String attrValue)");
        LOGGER.info("xmlFilePath: " + xmlFilePath);
        LOGGER.info("element: " + element);
        LOGGER.info("attrName: " + attrName);
        LOGGER.info("attrValue: " + attrValue);

        try {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node abc = attrMap.getNamedItem(attrName);
                if (abc != null && abc.getNodeValue().equals(attrValue)) {
                    return true;
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to traverse the DOM tree: ", e);
        } finally {
            LOGGER
                .info("Leaving com.apm.automation.XMLUtil.containsElements(String xmlFilePath, String element, String attrName, String attrValue)");
        }
        return false;
    }

    /**
     * Changes the attribute value of the corresponding element
     */
    public static String changeAttributeValue(String xmlFilePath, String element, String attrName,
        String attrOldValue, String attrNewValue) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node abc = attrMap.getNamedItem(attrName);
                if (abc != null && abc.getNodeValue().equals(attrOldValue)) {

                    abc.setNodeValue(attrNewValue);

                    // Normalize the DOM tree to combine all adjacent nodes
                    document.normalize();

                    break;
                }
            }

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Changes the attribute value of the element under the corresponding parent node
     */
    public static String changeAttributeValueWithparentNode(String xmlFilePath, String element,
        String parentNode, String attrName, String attrOldValue, String attrNewValue) {
        String message = null;
        Node abc = null;
        try {
            Document document = XMLUtil.getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                if (node.getParentNode().getNodeName().equals(parentNode)) {
                    NamedNodeMap attrMap = node.getAttributes();

                    if (attrName != null && attrName != "") {
                        abc = attrMap.getNamedItem(attrName);
                    } else {
                        abc = node.getFirstChild();
                    }
                    if (abc != null && abc.getNodeValue().equals(attrOldValue)) {

                        abc.setNodeValue(attrNewValue);

                        // Normalize the DOM tree to combine all adjacent nodes
                        document.normalize();

                        break;
                    }
                }
            }
            XMLUtil.writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Changes the attribute value of the user under the corresponding element
     */

    public static String changeAttributeValueforUser(String xmlFilePath, String element,
        String userName, String userValue, String attrName, String attrOldValue, String attrNewValue) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node user = attrMap.getNamedItem(userName);
                if (user != null && user.getNodeValue().equals(userValue)) {
                    Node abc = attrMap.getNamedItem(attrName);
                    if (abc != null && abc.getNodeValue().equals(attrOldValue)) {
                        abc.setNodeValue(attrNewValue);
                        // Normalize the DOM tree to combine all adjacent nodes
                        document.normalize();
                        break;
                    }
                }
            }

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * This method creates a new element with the given name and attributes and
     * adds it to the parentNode.
     * 
     * @param xmlFilePath
     *        - Location of the xml file
     * @param elementName
     *        - Name of the new element
     * @param parentNode
     *        - Name of the parent element
     * @param parentattr
     *        - The attribute name & value of parent
     * @param attributeMap
     *        - A Map containing the attribute names and values for the new
     *        element
     * @return message - A status message (Success/Failure)
     */
    public static String createElement(String xmlFilePath, String elementName, String elementText,
        String parentNode, String parentattrName, String parentAttrValue,
        Map<String, String> attributeMap) {
        String message = null;

        try {
            Document document = getDocument(xmlFilePath);

            /**
             * Finding the parent Node with the given the given attribute name &
             * value
             */
            Node parentElement = null;
            NodeList nodeList = document.getElementsByTagName(parentNode);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node abc = attrMap.getNamedItem(parentattrName);
                if (abc != null && abc.getNodeValue().equals(parentAttrValue)) {
                    parentElement = node;
                    // Normalize the DOM tree to combine all adjacent nodes
                    document.normalize();
                    break;
                }
            }

            /** Creating a new element with the given name and attribute values */
            Element newElement = document.createElement(elementName);

            if (attributeMap != null && attributeMap.size() > 0) {
                Set<String> keySet = attributeMap.keySet();
                Iterator<String> itr = keySet.iterator();
                while (itr.hasNext()) {
                    String attrName = (String) itr.next();
                    String attrValue = null;
                    if (attrName != null && attrName != "") {
                        attrValue = attributeMap.get(attrName);
                        newElement.setAttribute(attrName, attrValue);
                    } else {
                        attrValue = attributeMap.get(attrName);
                        // newElement.setNodeValue(attrValue);
                        newElement.setTextContent(attrValue);
                    }
                }
            }
            if (elementText != null && elementText != "") {
                newElement.setTextContent(elementText);
            }

            /** Adding the new element to the parent element */
            parentElement.appendChild(newElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * This method creates a new element with the given name and attributes and
     * adds it to the parentNode only if it has no childNodes
     */
    public static String createElementWhennoChild(String xmlFilePath, String elementName,
        String elementText, String parentNode, String parentattrName, String parentAttrValue,
        Map<String, String> attributeMap) {
        String message = null;

        try {
            Document document = getDocument(xmlFilePath);

            /**
             * Finding the parent Node with the given the given attribute name &
             * value
             */
            Node parentElement = null;
            NodeList nodeList = document.getElementsByTagName(parentNode);

            for (int i = 0; i < nodeList.getLength(); i++) {
                Node node = nodeList.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node abc = attrMap.getNamedItem(parentattrName);
                if (abc != null && abc.getNodeValue().equals(parentAttrValue)) {
                    if (!node.hasChildNodes()) {
                        parentElement = node;
                        document.normalize();
                        break;
                    }
                }
            }

            /** Creating a new element with the given name and attribute values */
            Element newElement = document.createElement(elementName);

            if (attributeMap != null && attributeMap.size() > 0) {
                Set<String> keySet = attributeMap.keySet();
                Iterator<String> itr = keySet.iterator();
                while (itr.hasNext()) {
                    String attrName = (String) itr.next();
                    String attrValue = null;
                    if (attrName != null && attrName != "") {
                        attrValue = attributeMap.get(attrName);
                        newElement.setAttribute(attrName, attrValue);
                    } else {
                        attrValue = attributeMap.get(attrName);
                        // newElement.setNodeValue(attrValue);
                        newElement.setTextContent(attrValue);
                    }
                }
            }
            if (elementText != null && elementText != "") {
                newElement.setTextContent(elementText);
            }

            /** Adding the new element to the parent element */
            parentElement.appendChild(newElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Deletes the element with the corresponding attribute name and value in the first occurrence
     */
    public static String deleteElement(String xmlFilePath, String element, String attrName,
        String attrValue) {
        LOGGER
            .info("Entering com.apm.automation.XMLUtil.deleteElement(String xmlFilePath, String element, String attrName, String attrValue)");
        LOGGER.info("xmlFilePath: " + xmlFilePath);
        LOGGER.info("element: " + element);
        LOGGER.info("attrName: " + attrName);
        LOGGER.info("attrValue: " + attrValue);

        String message = null;
        boolean isNodeDeleted = false;
        try {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            LOGGER.info("node list::::::"+nodeLst.toString());
            LOGGER.info("node list:::getLength:::"+nodeLst.getLength());
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node abc = attrMap.getNamedItem(attrName);
                if (abc != null && abc.getNodeValue().equals(attrValue)) {

                    node.getParentNode().removeChild(node);

                    // Normalize the DOM tree to combine all adjacent nodes
                    document.normalize();
                    isNodeDeleted = true;
                    break;
                }
            }

            writeToXMLFile(document, xmlFilePath);
            if (isNodeDeleted)
                message = SUCCESS_MESSAGE;
            else
                message = "Failed to delete the node";
        } catch (Exception e) {
            message = e.getMessage();
        } finally {
            LOGGER
                .info("Leaving com.apm.automation.XMLUtil.deleteElement(String xmlFilePath, String element, String attrName, String attrValue)");
        }
        return message;
    }

    /**
     * Deletes the element with the corresponding attribute name and value in all the occurrences
     */
    public static String deleteElementMultiple(String xmlFilePath, String element, String attrName,
        String attrValue) {
        String message = null;
        boolean isNodeDeleted = false;
        try {
            Document document = getDocument(xmlFilePath);
            NodeList nodeLst = document.getElementsByTagName(element);
            for (int i = 0; i < nodeLst.getLength(); i++) {
                Node node = nodeLst.item(i);
                NamedNodeMap attrMap = node.getAttributes();
                Node abc = attrMap.getNamedItem(attrName);
                if (abc != null && abc.getNodeValue().equals(attrValue)) {

                    node.getParentNode().removeChild(node);

                    // Normalize the DOM tree to combine all adjacent nodes
                    document.normalize();
                    isNodeDeleted = true;

                }
            }

            writeToXMLFile(document, xmlFilePath);
            if (isNodeDeleted)
                message = SUCCESS_MESSAGE;
            else
                message = "Unsucessful deleting a node";
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * This method creates an agent-cluster element in agentClusters.xml
     * 
     * @param xmlFilePath
     *        - Location of agentClusters.xml
     * @param agentClusterName
     * @param domain
     * @param agentSpecifierValue
     * @param metricSpecifiers
     */
    public static String createAgentCluster(String xmlFilePath, String agentClusterName,
        String domain, String agentSpecifierValue, String[] metricSpecifiers) {
        String message = "";
        try {

            Document document = getDocument(xmlFilePath);
            Node agentClusters = document.getElementsByTagName("agent-clusters").item(0);

            Element agentCluster = document.createElement("agent-cluster");

            if (agentClusterName != null && !agentClusterName.trim().equals("")) {
                agentCluster.setAttribute("name", agentClusterName);
                agentCluster.setAttribute("domain", domain);
            }
            if (domain != null && !domain.trim().equals("")) {
                agentCluster.setAttribute("name", agentClusterName);
                agentCluster.setAttribute("domain", domain);
            }
            if (agentSpecifierValue != null && !agentSpecifierValue.trim().equals("")) {

                Element agentSpecifier = document.createElement("agent-specifier");
                agentSpecifier.setTextContent(agentSpecifierValue);
                agentCluster.appendChild(agentSpecifier);
            }
            for (String metricSpecVal : metricSpecifiers) {
                Element metricSpecifier = document.createElement("metric-specifier");
                metricSpecifier.setTextContent(metricSpecVal);
                agentCluster.appendChild(metricSpecifier);
            }

            agentClusters.appendChild(agentCluster);

            XMLUtil.writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates a new domain in domains.xml
     * 
     * @param xmlFilePath
     *        - Location of domains.xml
     * @param domainName
     *        - Name of the domain
     * @param description
     *        - Description of domain
     * @param agentMapping
     *        - Value of agent mapping tag
     * @param usersMap
     *        - Map containing users and permissions
     * @param groupsMap
     *        - Map containing groups and permissions
     */
    public static void createDomain(String xmlFilePath, String domainName, String description,
        String agentMapping, Map<String, String> usersMap, Map<String, String> groupsMap) {
        try {
            Document document = XMLUtil.getDocument(xmlFilePath);
            Node domains = document.getElementsByTagName("domains").item(0);
            Node superDomain = document.getElementsByTagName("SuperDomain").item(0);

            Element domain = document.createElement("domain");
            domain.setAttribute("name", domainName);
            domain.setAttribute("description", description);

            Element agent = document.createElement("agent");
            agent.setAttribute("mapping", agentMapping);
            domain.appendChild(agent);

            if (groupsMap != null && groupsMap.size() > 0) {
                Set<String> keySet = groupsMap.keySet();
                for (String group : keySet) {
                    Element grantUser = document.createElement("grant");
                    grantUser.setAttribute("group", group);
                    grantUser.setAttribute("permission", groupsMap.get(group));
                    domain.appendChild(grantUser);
                }
            }

            if (usersMap != null && usersMap.size() > 0) {
                Set<String> keySet = usersMap.keySet();
                for (String user : keySet) {
                    Element grantUser = document.createElement("grant");
                    grantUser.setAttribute("user", user);
                    grantUser.setAttribute("permission", usersMap.get(user));
                    domain.appendChild(grantUser);
                }
            }

            /** Inserts the new domain node before SuperDomain */
            domains.insertBefore(domain, superDomain);

            XMLUtil.writeToXMLFile(document, xmlFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a new domain in domains.xml and returns message with success or
     * failure
     * 
     * @param xmlFilePath
     *        - Location of domains.xml
     * @param domainName
     *        - Name of the domain
     * @param description
     *        - Description of domain
     * @param agentMapping
     *        - Value of agent mapping tag
     * @param usersMap
     *        - Map containing users and permissions
     * @param groupsMap
     *        - Map containing groups and permissions
     */
    public static String createDomainWithReturnMessage(String xmlFilePath, String domainName,
        String description, String agentMapping, Map<String, String> usersMap,
        Map<String, String> groupsMap) {
        String message = "";
        try {
            Document document = XMLUtil.getDocument(xmlFilePath);
            Node domains = document.getElementsByTagName("domains").item(0);
            Node superDomain = document.getElementsByTagName("SuperDomain").item(0);

            Element domain = document.createElement("domain");
            domain.setAttribute("name", domainName);
            domain.setAttribute("description", description);

            Element agent = document.createElement("agent");
            agent.setAttribute("mapping", agentMapping);
            domain.appendChild(agent);

            if (groupsMap != null && groupsMap.size() > 0) {
                Set<String> keySet = groupsMap.keySet();
                for (String group : keySet) {
                    Element grantUser = document.createElement("grant");
                    grantUser.setAttribute("group", group);
                    grantUser.setAttribute("permission", groupsMap.get(group));
                    domain.appendChild(grantUser);
                }
            }

            if (usersMap != null && usersMap.size() > 0) {
                Set<String> keySet = usersMap.keySet();
                for (String user : keySet) {
                    Element grantUser = document.createElement("grant");
                    grantUser.setAttribute("user", user);
                    grantUser.setAttribute("permission", usersMap.get(user));
                    domain.appendChild(grantUser);
                }
            }

            /** Inserts the new domain node before SuperDomain */
            domains.insertBefore(domain, superDomain);

            XMLUtil.writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            e.printStackTrace();
            message = e.getMessage();
        }
        return message;
    }
    
    /**
     * Creates a new domain after superdomain tag in domains.xml 
     * 
     * @param xmlFilePath
     *        - Location of domains.xml
     * @param domainName
     *        - Name of the domain
     * @param description
     *        - Description of domain
     * @param agentMapping
     *        - Value of agent mapping tag
     * @param usersMap
     *        - Map containing users and permissions
     * @param groupsMap
     *        - Map containing groups and permissions
     */
    public static void createDomainAfterSuperDomain(String xmlFilePath, String domainName, String description,
        String agentMapping, Map<String, String> usersMap, Map<String, String> groupsMap) {
        try {
            Document document = XMLUtil.getDocument(xmlFilePath);
            Node domains = document.getElementsByTagName("domains").item(0);            
            Node domainsend = document.getElementsByTagName("/domains").item(0);

            Element domain = document.createElement("domain");
            domain.setAttribute("name", domainName);
            domain.setAttribute("description", description);

            Element agent = document.createElement("agent");
            agent.setAttribute("mapping", agentMapping);
            domain.appendChild(agent);

            if (groupsMap != null && groupsMap.size() > 0) {
                Set<String> keySet = groupsMap.keySet();
                for (String group : keySet) {
                    Element grantUser = document.createElement("grant");
                    grantUser.setAttribute("group", group);
                    grantUser.setAttribute("permission", groupsMap.get(group));
                    domain.appendChild(grantUser);
                }
            }

            if (usersMap != null && usersMap.size() > 0) {
                Set<String> keySet = usersMap.keySet();
                for (String user : keySet) {
                    Element grantUser = document.createElement("grant");
                    grantUser.setAttribute("user", user);
                    grantUser.setAttribute("permission", usersMap.get(user));
                    domain.appendChild(grantUser);
                }
            }

            /** Inserts the new domain node after SuperDomain */
            domains.insertBefore(domain, domainsend);           
            XMLUtil.writeToXMLFile(document, xmlFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    } 


    /**
     * Creates the group grant element with the given group name and permission level
     * in domains.xml for SuperDomain
     */
    public static String createGroupGrantForSuperDomain(String xmlFilePath, String groupName,
        String permissionLevel) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);

            /** getting root element 'SuperDomain' */
            Node superDomainElement = document.getElementsByTagName("SuperDomain").item(0);

            /** creating a new user element */
            Element grantElement = document.createElement("grant");
            grantElement.setAttribute("group", groupName);
            grantElement.setAttribute("permission", permissionLevel);
            superDomainElement.appendChild(grantElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates the group grant element with the given group name and permission level
     * for the corresponding element
     */
    public static String createGroupGrantForElement(String xmlFilePath, String elementName,
        String groupName, String permissionLevel) {
        String message = null;
        try {
            Document document = XMLUtil.getDocument(xmlFilePath);

            /** getting root element 'SuperDomain' */
            Node superDomainElement = document.getElementsByTagName(elementName).item(0);

            /** creating a new user element */
            Element grantElement = document.createElement("grant");
            grantElement.setAttribute("group", groupName);
            grantElement.setAttribute("permission", permissionLevel);
            superDomainElement.appendChild(grantElement);

            XMLUtil.writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates the group grant element with the given group name and permission level
     * for the SuperDomain
     */
    public static String createUserGrantElement(String xmlFilePath, String username,
        String permissionLevel) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);

            /** getting root element 'SuperDomain' */
            Node superDomainElement = document.getElementsByTagName("SuperDomain").item(0);

            /** creating a new user element */
            Element grantElement = document.createElement("grant");
            grantElement.setAttribute("user", username);
            grantElement.setAttribute("permission", permissionLevel);
            superDomainElement.appendChild(grantElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates the group grant element with the given group name and permission level
     * for the CustomDomain
     */
    public static String createUserGrantElementForCustomDomain(String xmlFilePath,
        String customDomainName, String username, String permissionLevel) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);

            /** getting root element 'SuperDomain' */
            Node superDomainElement = document.getElementsByTagName(customDomainName).item(0);

            /** creating a new user element */
            Element grantElement = document.createElement("grant");
            grantElement.setAttribute("user", username);
            grantElement.setAttribute("permission", permissionLevel);
            superDomainElement.appendChild(grantElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates a user with the given username and password under <users> tag in users.xml
     * 
     */
    public static String createUserInUsersXML(String xmlFilePath, String userName, String password) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);
            /** getting users element/node */
            Node usersNode = document.getElementsByTagName("users").item(0);

            /** creating a new user element */
            Element userElement = document.createElement("user");
            userElement.setAttribute("password", password.trim());
            userElement.setAttribute("name", userName.trim());
            usersNode.appendChild(userElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates a user with the given username for the Admin group in users.xml
     * 
     */
    public static String addUserToAdminGroup(String xmlFilePath, String username) {
        String message = null;
        try {

            Document document = getDocument(xmlFilePath);
            /** getting group element/node */
            Node groupNode = document.getElementsByTagName("group").item(2);

            /** creating a new user element */
            Element userElement = document.createElement("user");
            userElement.setAttribute("name", username);
            groupNode.appendChild(userElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates a user with the given username for the CEM System Administrator group in users.xml
     * 
     */
    public static String addUserToCEMAdminGroup(String xmlFilePath, String username) {
        String message = null;
        try {

            Document document = getDocument(xmlFilePath);
            /** getting group element/node */
            Node groupNode = document.getElementsByTagName("group").item(1);

            /** creating a new user element */
            Element userElement = document.createElement("user");
            userElement.setAttribute("name", username);
            groupNode.appendChild(userElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates a group with the given group name and description
     * and adds single user to that group in users.xml
     * 
     */
    public static String createGroupAddSingleUserInUsersXML(String xmlFilePath, String description,
        String userGroup, String user1) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);
            /** getting users element/node */
            Node usersNode = document.getElementsByTagName("groups").item(0);
            System.out.println(usersNode.getNodeName());

            /** creating a new user element */
            Element userElement = document.createElement("group");
            userElement.setAttribute("description", description.trim());
            userElement.setAttribute("name", userGroup.trim());

            Element groupUser1 = document.createElement("user");
            groupUser1.setAttribute("name", user1);
            userElement.appendChild(groupUser1);

            usersNode.appendChild(userElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }
    
    /**
     * Creates a Property with the given name and Add Value
     * to that group in relams.xml
     * 
     */
    public static String createPropertyAddValueInrelamsXML(String xmlFilePath, String propertyname, String value) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);
            /** getting users element/node */
            Node relamNode = document.getElementsByTagName("realm").item(0);
            System.out.println(relamNode.getNodeName());

            /** creating a new user element */
            Element propertyElement = document.createElement("property");
            propertyElement.setAttribute("name", propertyname.trim());

            Element groupUser1 = document.createElement("value");
            groupUser1.setTextContent(value);
            propertyElement.appendChild(groupUser1);

            relamNode.appendChild(propertyElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    
    
    /**
     * Creates a group with the given group name and description
     * and adds two users to that group in users.xml
     * 
     */
    public static String createGroupAddTwoUsersInUsersXML(String xmlFilePath, String description,
        String userGroup, String user1, String user2) {
        String message = null;
        try {
            Document document = getDocument(xmlFilePath);
            /** getting users element/node */
            Node usersNode = document.getElementsByTagName("groups").item(0);
            System.out.println(usersNode.getNodeName());

            /** creating a new user element */
            Element userElement = document.createElement("group");
            userElement.setAttribute("description", description.trim());
            userElement.setAttribute("name", userGroup.trim());

            Element groupUser1 = document.createElement("user");
            groupUser1.setAttribute("name", user1);
            userElement.appendChild(groupUser1);

            Element groupUser2 = document.createElement("user");
            groupUser2.setAttribute("name", user2);
            userElement.appendChild(groupUser2);

            usersNode.appendChild(userElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /**
     * Creates a group with the given group name and description
     * and adds multiple users to that group in users.xml
     * 
     */
    public static String createGroupAddMultipleUsersInUsersXML(String xmlFilePath,
        String description, String userGroup, String user) {
        String message = null;
        try {

            String[] users = user.split(",");
            Document document = getDocument(xmlFilePath);
            /** getting users element/node */
            Node usersNode = document.getElementsByTagName("groups").item(0);
            System.out.println(usersNode.getNodeName());

            /** creating a new group element */
            Element userElement = document.createElement("group");
            userElement.setAttribute("description", description.trim());
            userElement.setAttribute("name", userGroup.trim());

            /** adding users to the created group element */
            for (int i = 0; i < users.length; i++) {
                Element groupUser = document.createElement("user");
                groupUser.setAttribute("name", users[i]);
                userElement.appendChild(groupUser);
            }
            usersNode.appendChild(userElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;
        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    public static String deleteUserFromAdminGroup(String xmlFilePath, String username) {
        String message = null;
        try {

            Document document = getDocument(xmlFilePath);
            /** getting group element/node */
            Node groupNode = document.getElementsByTagName("group").item(2);

            /** creating a new user element */
            Element userElement = document.createElement("user");
            userElement.setAttribute("name", username);
            groupNode.appendChild(userElement);

            writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }

    /*
     * Creates an agent-collector entry in the loadbalancing.xml with the latched value set
     * file_name - give loadbalancing.xml including its path
     * agent_collector - name of the agent-collector
     * agent_specifier - agent expression for the agents to point to the collectors
     * collector_1 - pass both col1host and port like col1host+":"+c1Port
     * collector_2 - pass both col1host and port like col2host+":"+c2Port
     * affinity - tells to which collector you want to set the latched property , pass value like
     * col1host+":"+"true"
     */
    public int addlatchedEntryInLoadBalXML(String file_name, String agent_collector,
        String agent_specifier, String collector_1, String collector_2, String affinity)
        throws Exception {
        int done_adding = 0;
        File f = new File(file_name);
        Document doc = getDocument(file_name);
        doc.normalize();
        NodeList l = doc.getElementsByTagName("loadbalancing");
        if (l.getLength() == 1) {
            Element ac = doc.createElement("agent-collector");
            ac.setAttribute("name", agent_collector);
            Element as = doc.createElement("agent-specifier");
            as.setTextContent(agent_specifier);
            Element include = doc.createElement("include");
            Element collector1 = doc.createElement("collector");
            String host_port_1[] = collector_1.split(":");
            collector1.setAttribute("host", host_port_1[0]);
            collector1.setAttribute("port", host_port_1[1]);
            Element collector2 = doc.createElement("collector");
            String host_port_2[] = collector_2.split(":");
            collector2.setAttribute("host", host_port_2[0]);
            collector2.setAttribute("port", host_port_2[1]);
            String attach_to[] = affinity.split(":");
            if (attach_to[0].contains("1")) {
                collector1.setAttribute("latched", attach_to[1]);
            } else if (attach_to[0].contains("2")) {
                collector2.setAttribute("latched", attach_to[1]);
            }
            l.item(0).appendChild(ac);
            ac.appendChild(as);
            ac.appendChild(include);
            include.appendChild(collector1);
            include.appendChild(collector2);
            done_adding = 1;
        }
        int ret = saveFile(f, doc);
        return ret & done_adding;
    }


    /*
     * Changes the affinity from one collector to another by chaging the latched property
     */
    public Boolean changelatchedEntryInLoadBalXML(String strng, String name_value_as,
        String to_value, String rem_value) throws Exception {
        boolean ret_value = Boolean.FALSE;
        File f = new File(strng);
        Document doc = getDocument(strng);
        doc.normalize();
        NodeList l = doc.getElementsByTagName("loadbalancing");
        for (int i = 0; i < l.getLength(); i++) {
            NodeList ll = l.item(i).getChildNodes();
            for (int j = 0; j < ll.getLength(); j++) {
                if (!(ll.item(j).getNodeName().equals("#text"))) {
                    String name_value = ll.item(j).getNodeName();
                    if (name_value.equals("agent-collector")) {
                        Element e = (Element) ll.item(j);
                        String abc = e.getAttribute("name");
                        if (abc.equalsIgnoreCase(name_value_as)) {
                            System.out.println(abc);
                            Node parent = e;
                            ret_value = parse(f, parent, "include", to_value, doc);
                        }
                    }
                }
            }
        }
        return ret_value;
    }


     /**
     * Creates an agent-collector entry in the loadbalancing.xml with the latched value set
     * file_name - give loadbalancing.xml including its path
     * agent_collector - name of the agent-collector
     * agent_specifier - agent expression for the agents to point to the collectors
     * collector_1 - pass both col1host and port like col1host+":"+c1Port
     * type - values can be include/exclude
     * 
     * Author: JAMSA07
     * 
     */
    public int addCollectorEntryInLoadbalanceXML(String file_name, String agent_collector,
        String agent_specifier, String collector_1, String type) throws Exception {
        int done_adding = 0;
        File f = new File(file_name);
        Document doc = getDocument(file_name);
        doc.normalize();
        NodeList l = doc.getElementsByTagName("loadbalancing");
        if (l.getLength() == 1) {
            Element ac = doc.createElement("agent-collector");
            ac.setAttribute("name", agent_collector);
            Element as = doc.createElement("agent-specifier");
            as.setTextContent(agent_specifier);
            Element include = doc.createElement(type);
            Element collector1 = doc.createElement("collector");
            String host_port_1[] = collector_1.split(":");
            collector1.setAttribute("host", host_port_1[0]);
            collector1.setAttribute("port", host_port_1[1]);
            l.item(0).appendChild(ac);
            ac.appendChild(as);
            ac.appendChild(include);
            include.appendChild(collector1);
            done_adding = 1;
        }
        int ret = saveFile(f, doc);
        return ret & done_adding;
    }


    public int addCollectorsEntryInLoadbalanceXML(String file_name, String agent_collector,
                                                  String agent_specifier, String collector_1, String collector_2, String type) throws Exception {
        int done_adding = 0;
        File f = new File(file_name);
        Document doc = getDocument(file_name);
        doc.normalize();
        NodeList l = doc.getElementsByTagName("loadbalancing");
        if (l.getLength() == 1) {
            Element ac = doc.createElement("agent-collector");
            ac.setAttribute("name", agent_collector);
            Element as = doc.createElement("agent-specifier");
            as.setTextContent(agent_specifier);
            Element include = doc.createElement(type);
            Element collector1 = doc.createElement("collector");
            String host_port_1[] = collector_1.split(":");
            collector1.setAttribute("host", host_port_1[0]);
            collector1.setAttribute("port", host_port_1[1]);
            Element collector2 = doc.createElement("collector");
            String host_port_2[] = collector_2.split(":");
            collector2.setAttribute("host", host_port_2[0]);
            collector2.setAttribute("port", host_port_2[1]);
            l.item(0).appendChild(ac);
            ac.appendChild(as);
            ac.appendChild(include);
            include.appendChild(collector1);
            include.appendChild(collector2);
            done_adding = 1;
        }
        int ret = saveFile(f, doc);
        return ret & done_adding;
    }

    /**
     * Creates an agent-collector entry in the loadbalancing.xml with the latched value set
     * file_name - give loadbalancing.xml including its path
     * agent_collector - name of the agent-collector
     * agent_specifier - agent expression for the agents to point to the collectors
     * type - values can be include/exclude
     * 
     * Author: JAMSA07
     * 
     */
    public int addEmptyCollectorEntryInLoadbalanceXML(String file_name, String agent_collector,
        String agent_specifier, String type) throws Exception {
        int done_adding = 0;
        File f = new File(file_name);
        Document doc = getDocument(file_name);
        doc.normalize();
        NodeList l = doc.getElementsByTagName("loadbalancing");
        if (l.getLength() == 1) {
            Element ac = doc.createElement("agent-collector");
            ac.setAttribute("name", agent_collector);
            Element as = doc.createElement("agent-specifier");
            as.setTextContent(agent_specifier);
            Element include = doc.createElement(type);
            l.item(0).appendChild(ac);
            ac.appendChild(as);
            ac.appendChild(include);
            done_adding = 1;
        }
        int ret = saveFile(f, doc);
        return ret & done_adding;
    }


    /**
     * Creates a new entry of the HTTP Connection so that EM can be accessed VIA
     * HTTP by agent and instead of uncommenting it, we add a new entry.
     * 
     * Author: JAMSA07
     * 
     */
    public int addHttpEntryInEMJetty(String file_name) throws Exception {
        int done_adding = 0;
        File f = new File(file_name);
        Document doc = getDocument(file_name);
        doc.normalize();
        NodeList l = doc.getElementsByTagName("Configure");
        if (l.getLength() == 1) {
            Element call = doc.createElement("Call");
            call.setAttribute("name", "addConnector");
            Element arg = doc.createElement("Arg");
            Element newClassForPort = doc.createElement("New");
            newClassForPort.setAttribute("class","com.wily.webserver.NoNPESocketConnector");

            Element setPort = doc.createElement("Set");
            setPort.setAttribute("name","port");
            setPort.appendChild(doc.createTextNode("8081"));

            Element setHeaderBufferSize = doc.createElement("Set");
            setHeaderBufferSize.setAttribute("name","HeaderBufferSize");
            setHeaderBufferSize.appendChild(doc.createTextNode("8192"));

            Element setRequestBufferSize = doc.createElement("Set");
            setRequestBufferSize.setAttribute("name","RequestBufferSize");
            setRequestBufferSize.appendChild(doc.createTextNode("16384"));


            Element setThreadPool = doc.createElement("Set");
            setThreadPool.setAttribute("name","ThreadPool");

            Element newOrgMortbayThreadBoundedThreadPool = doc.createElement("New");
            newOrgMortbayThreadBoundedThreadPool.setAttribute("class","org.mortbay.thread.BoundedThreadPool");

            Element setMinThreads = doc.createElement("Set");
            setMinThreads.setAttribute("name","minThreads");
            setMinThreads.appendChild(doc.createTextNode("10"));

            Element setMaxThreads = doc.createElement("Set");
            setMaxThreads.setAttribute("name","maxThreads");
            setMaxThreads.appendChild(doc.createTextNode("100"));

            Element setMaxIdleTimeMs = doc.createElement("Set");
            setMaxIdleTimeMs.setAttribute("name","maxIdleTimeMs");
            setMaxIdleTimeMs.appendChild(doc.createTextNode("60000"));


            l.item(0).appendChild(call);
            call.appendChild(arg);
            arg.appendChild(newClassForPort);
            newClassForPort.appendChild(setPort);
            newClassForPort.appendChild(setHeaderBufferSize);
            newClassForPort.appendChild(setRequestBufferSize);
            newClassForPort.appendChild(setThreadPool);
            setThreadPool.appendChild(newOrgMortbayThreadBoundedThreadPool);
            newOrgMortbayThreadBoundedThreadPool.appendChild(setMinThreads);
            newOrgMortbayThreadBoundedThreadPool.appendChild(setMaxThreads);
            newOrgMortbayThreadBoundedThreadPool.appendChild(setMaxIdleTimeMs);

            done_adding = 1;
        }
        int ret = saveFile(f, doc);
        return ret & done_adding;
    }
    
    /**
     * Creates a new entry of the HTTP Connection so that EM can be accessed VIA
     * HTTP by agent and instead of uncommenting it, we add a new entry.
     * 
     * Author: KETSW01
     * 
     */
    public int addCustomHttpEntryInEMJetty(String file_name, String emWebPort) throws Exception {
        int done_adding = 0;
        File f = new File(file_name);
        Document doc = getDocument(file_name);
        doc.normalize();
        NodeList l = doc.getElementsByTagName("Configure");
        if (l.getLength() == 1) {
            Element call = doc.createElement("Call");
            call.setAttribute("name", "addConnector");
            Element arg = doc.createElement("Arg");
            Element newClassForPort = doc.createElement("New");
            newClassForPort.setAttribute("class","com.wily.webserver.NoNPESocketConnector");

            Element setPort = doc.createElement("Set");
            setPort.setAttribute("name","port");
            setPort.appendChild(doc.createTextNode(emWebPort));

            Element setHeaderBufferSize = doc.createElement("Set");
            setHeaderBufferSize.setAttribute("name","HeaderBufferSize");
            setHeaderBufferSize.appendChild(doc.createTextNode("8192"));

            Element setRequestBufferSize = doc.createElement("Set");
            setRequestBufferSize.setAttribute("name","RequestBufferSize");
            setRequestBufferSize.appendChild(doc.createTextNode("16384"));


            Element setThreadPool = doc.createElement("Set");
            setThreadPool.setAttribute("name","ThreadPool");

            Element newOrgMortbayThreadBoundedThreadPool = doc.createElement("New");
            newOrgMortbayThreadBoundedThreadPool.setAttribute("class","org.mortbay.thread.BoundedThreadPool");

            Element setMinThreads = doc.createElement("Set");
            setMinThreads.setAttribute("name","minThreads");
            setMinThreads.appendChild(doc.createTextNode("10"));

            Element setMaxThreads = doc.createElement("Set");
            setMaxThreads.setAttribute("name","maxThreads");
            setMaxThreads.appendChild(doc.createTextNode("100"));

            Element setMaxIdleTimeMs = doc.createElement("Set");
            setMaxIdleTimeMs.setAttribute("name","maxIdleTimeMs");
            setMaxIdleTimeMs.appendChild(doc.createTextNode("60000"));


            l.item(0).appendChild(call);
            call.appendChild(arg);
            arg.appendChild(newClassForPort);
            newClassForPort.appendChild(setPort);
            newClassForPort.appendChild(setHeaderBufferSize);
            newClassForPort.appendChild(setRequestBufferSize);
            newClassForPort.appendChild(setThreadPool);
            setThreadPool.appendChild(newOrgMortbayThreadBoundedThreadPool);
            newOrgMortbayThreadBoundedThreadPool.appendChild(setMinThreads);
            newOrgMortbayThreadBoundedThreadPool.appendChild(setMaxThreads);
            newOrgMortbayThreadBoundedThreadPool.appendChild(setMaxIdleTimeMs);

            done_adding = 1;
        }
        int ret = saveFile(f, doc);
        return ret & done_adding;
    }

    public void parseValue(File f, Node parent, String node, String to_value, Document doc)
        throws Exception {
        String collector_data[] = to_value.split(":");
        String node_name = node;
        NodeList pl = parent.getChildNodes();
        for (int k = 0; k < pl.getLength(); k++) {
            if (!(pl.item(k).getNodeName().equals("#text")) && (value_changed == 0)) {
                String abc = pl.item(k).getNodeName();
                Element e = (Element) pl.item(k);
                if (abc.equalsIgnoreCase(node)) {
                    String host = e.getAttribute("host");
                    String port = e.getAttribute("port");
                    if ((host.equalsIgnoreCase(collector_data[0]))
                        && (port.equalsIgnoreCase(collector_data[1]))) {
                        System.out.println(host);
                        System.out.println(port);
                        System.out.println("found it");
                        e.setAttribute("latched", "true");
                        System.out.println("true");
                        value_changed = 1;
                    } else {
                        e.removeAttribute("latched");
                    }
                }
            } else {
                parseValue(f, pl.item(k), node_name, to_value, doc);
            }
        }

        saveFile(f, doc);

    }


    public boolean parse(File f, Node parent, String node, String to_value, Document doc)
        throws Exception {
        Boolean done = Boolean.FALSE;
        String node_name = node;
        NodeList pl = parent.getChildNodes();
        for (int k = 0; k < pl.getLength(); k++) {
            if (!(pl.item(k).getNodeName().equals("#text")) && (done_parsing == 0)) {
                String abc = pl.item(k).getNodeName();
                Element e = (Element) pl.item(k);
                if (abc.equalsIgnoreCase(node)) {
                    Node parent1 = e;
                    System.out.println(parent1.getNodeName());
                    parseValue(f, parent1, "collector", to_value, doc);
                    done_parsing = 1;
                } else {
                    parse(f, pl.item(k), node_name, to_value, doc);
                }
            }
        }
        if (done_parsing == 1) {
            done = Boolean.TRUE;
        }
        return done;

    }
    
    /**
     * JAMSA07 - Fixed for indentation
     * 
     * @param f
     * @param doc
     * @return
     * @throws Exception
     */
    
    public int saveFile(File f, Document doc) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        Result output = new StreamResult(f);
        Source input = new DOMSource(doc);
        transformer.transform(input, output);
        return 1;
    }
    

    /**
     * delete User from specified file path
     * 
     * @param filepath
     * @param userName
     */
    public static String deleteUser(String filepath, String userName) {
        return XMLUtil.deleteElement(filepath, "user", "name", userName);
    }

    /**
     * delete Group from from specified file path
     * 
     * @param filepath
     * @param groupName
     */
    public static String deleteGroup(String filepath, String groupName) {
        return XMLUtil.deleteElement(filepath, "group", "name", groupName);
    }


    /**
     * Create User from specified file path
     * 
     * @param filepath
     * @param userName
     * @param userPassword
     */
    public static String addUser(String filepath, String userName, String userPassword) {
        return XMLUtil.createUserInUsersXML(filepath, userName, userPassword);
    }

    /**
     * Create Group from from specified file path
     * 
     * @param filepath
     * @param description
     * @param groupName
     * @param userName
     * 
     */
    public static String addGroup(String filepath, String desc, String groupName, String userName) {
        return XMLUtil.createGroupAddSingleUserInUsersXML(filepath, desc, groupName, userName);
    }

    /**
     * Add user permission from specified file path in domain.xml
     * 
     * @param filepath
     * @param userName
     * @param permission
     */
    public static String grantPermissionUserDomainXml(String filepath, String userName, String permission) {
        return XMLUtil.createUserGrantElement(filepath, userName, permission);
    }

    /**
     * Add Group permission from specified file path in domain.xml
     * 
     * @param filepath
     * @param groupName
     * @param permission
     */
    public static String grantPermissionGroupDomainXml(String filepath, String groupName, String permission) {
        return XMLUtil.createGroupGrantForElement(filepath, "SuperDomain", groupName, permission);
    }

    /**
     * Add Group permission from specified file path in server.xml
     * 
     * @param filepath
     * @param groupName
     * @param permission
     */
    public static String grantPermissionGroupServerXml(String filepath, String groupName, String permission) {
        return XMLUtil.createGroupGrantForElement(filepath, "server", groupName, permission);
    }


    /**
     * update Attribute the specified string with the given string in EM relam.xml file
     * 
     * @param filepath
     * @param userAttrOldValue
     * @param userAttrNewValue
     */
    public static String modifyRelamXmlPropertyAttribute(String filepath, String userAttrOldValue,
        String userAttrNewValue) {
        String userElementName = "property";
        String userAttrName = "name";
        return XMLUtil
            .changeAttributeValue(filepath, userElementName, userAttrName, userAttrOldValue, userAttrNewValue);
    }

    /**
     * update relam Attribute the specified string with the given string in EM relam.xml file
     * 
     * @param filepath
     * @param userAttrName
     * @param userAttrNameValue
     * @param userAttrNamenewValue
     */
    public static String modifyRelamXmlrelamAttribute(String filepath, String userAttrName, String userAttrNameValue,
        String userAttrNamenewValue) {
        String userElementName = "realm";
        return XMLUtil.changeAttributeValue(filepath, userElementName, userAttrName, userAttrNameValue,
            userAttrNamenewValue);
    }

    /**
     * delete property Attribute in EM relam.xml file
     * 
     * @param filepath
     * @param propertyName
     */
    public static String deleteRelamXmlProperty(String filepath, String propertyName) {
        return XMLUtil.deleteElement(filepath, "property", "name", propertyName);
    }

    /**
     * delete Group Attribute in EM domain.xml file
     * 
     * @param filepath
     * @param propertyName
     */
    public static String deleteGroupInDomainXml(String filepath, String propertyName) {
        return XMLUtil.deleteElement(filepath, "grant", "group", propertyName);
    }
    
    /**
     * delete user Attribute in EM domain.xml file
     * 
     * @param filepath
     * @param propertyName
     */
    public static String deleteUserInDomainXml(String filepath, String propertyName) {
        return XMLUtil.deleteElement(filepath, "grant", "user", propertyName);
    }

    /**
     * create user Attribute and user value in EM relam.xml file
     * 
     * @param filepath
     * @param userAttrName
     * @param userValue
     */
    public static String addRelamXmlPropertyAttribute(String filepath, String userAttrName, String userValue) {

        return XMLUtil.createPropertyAddValueInrelamsXML(filepath, userAttrName, userValue);
    }

    /**
     * Creates the user grant element and adds permission level in the server.xml file
     */
    public static String createUserGrantElementInServerXml(String xmlFilePath,
        String userName, String permissionLevel) {
        String message = null;
        try {
            Document document = XMLUtil.getDocument(xmlFilePath);

            /** getting root element 'SuperDomain' */
            Node superDomainElement = document.getElementsByTagName("server").item(0);

            /** creating a new user element */
            Element grantElement = document.createElement("grant");
            grantElement.setAttribute("user", userName);
            grantElement.setAttribute("permission", permissionLevel);
            superDomainElement.appendChild(grantElement);

            XMLUtil.writeToXMLFile(document, xmlFilePath);
            message = SUCCESS_MESSAGE;

        } catch (Exception e) {
            message = e.getMessage();
        }
        return message;
    }


}
