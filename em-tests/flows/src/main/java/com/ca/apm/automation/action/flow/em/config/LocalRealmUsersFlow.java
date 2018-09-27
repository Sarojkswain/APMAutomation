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
package com.ca.apm.automation.action.flow.em.config;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Modifies users.xml according to the specified configuration
 */
@Flow
public class LocalRealmUsersFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalRealmUsersFlow.class);

    private static final String USER_NAME_ATTRIBUTE = "name";
    private static final String USER_PASSWORD_ATTRIBUTE = "password";
    private static final String GROUP_NAME_ATTRIBUTE = "name";
    private static final String PLAIN_TEXT_PASSWORDS_ATTR = "plainTextPasswords";
    private static final String PRINCIPALS_TAG = "principals";
    private static final String USERS_TAG = "users";
    private static final String GROUPS_TAG = "groups";

    @FlowContext
    LocalRealmUsersFlowContext flowContext;

    private final Map<String, Node> userXMLNodes = new HashMap<>();
    private final Map<String, Node> groupXMLNodes = new HashMap<>();
    private final Map<String, String> existingUsers = new HashMap<>();
    private final Map<String, HashSet<String>> existingGroups = new HashMap<>();

    private Map<String, String> modifyUsers;
    private Map<String, Set<String>> modifyGroups;
    private boolean plainTextPasswords;

    @Override
    public void run() throws Exception {
        String usersXMLFilePath = flowContext.getUsersXMLFilePath();
        modifyUsers = flowContext.getUserPasswordMap();
        modifyGroups = flowContext.getGroupUserMap();
        plainTextPasswords = flowContext.isPlainTextPasswords();
        File usersFile = FileUtils.getFile(usersXMLFilePath);
        if (usersFile.exists()) {
            LOGGER.info("Modifying users.xml file at " + usersXMLFilePath);
            // load xml file into DOM
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(usersFile);
            doc.getDocumentElement().normalize();

            Node usersParent, groupsParent;
            Node principals = doc.getElementsByTagName(PRINCIPALS_TAG).item(0);
            // Set to true only if there is some change in passwords
            if (plainTextPasswords) {
                principals.getAttributes().getNamedItem(PLAIN_TEXT_PASSWORDS_ATTR)
                    .setTextContent(String.valueOf(Boolean.TRUE));
            }
            // read existing users
            NodeList localUsers = doc.getElementsByTagName(USERS_TAG);
            usersParent = localUsers.item(0);
            NodeList userList = usersParent.getChildNodes();
            for (int i = 0; i < userList.getLength(); i++) {
                Node user = userList.item(i);
                if (user.getNodeType() == Node.ELEMENT_NODE) {
                    String userName =
                        user.getAttributes().getNamedItem(USER_NAME_ATTRIBUTE).getNodeValue();
                    String password =
                        user.getAttributes().getNamedItem(USER_PASSWORD_ATTRIBUTE).getNodeValue();
                    existingUsers.put(userName, password);
                    userXMLNodes.put(userName, user);
                }
            }
            // read existing groups
            NodeList localGroups = doc.getElementsByTagName(GROUPS_TAG);
            groupsParent = localGroups.item(0);
            NodeList groupList = groupsParent.getChildNodes();
            for (int i = 0; i < groupList.getLength(); i++) {
                Node group = groupList.item(i);
                if (group.getNodeType() == Node.ELEMENT_NODE) {
                    String groupName =
                        group.getAttributes().getNamedItem(GROUP_NAME_ATTRIBUTE).getNodeValue();
                    groupXMLNodes.put(groupName, group);
                    HashSet<String> groupUserSet = new HashSet<>();
                    NodeList groupUsers = group.getChildNodes();
                    for (int j = 0; j < groupUsers.getLength(); j++) {
                        Node groupUser = groupUsers.item(j);
                        if (groupUser.getNodeType() == Node.ELEMENT_NODE) {
                            String groupUserName =
                                groupUser.getAttributes().getNamedItem(USER_NAME_ATTRIBUTE)
                                    .getNodeValue();
                            groupUserSet.add(groupUserName);
                        }
                    }
                    if (groupUserSet.isEmpty()) {
                        groupUserSet.add(LocalRealmUsersFlowContext.DUMMY_USER);
                    }
                    existingGroups.put(groupName, groupUserSet);
                    groupXMLNodes.put(groupName, group);
                }
            }
            // Modify the document with users and groups
            for (String modifyUserName : modifyUsers.keySet()) {
                String modifyPassword = modifyUsers.get(modifyUserName);
                // If the user already exists, but with a different password, update the password
                if (existingUsers.containsKey(modifyUserName)) {
                    if (!existingUsers.get(modifyUserName).equals(modifyPassword)) {
                        // Find the node and modify it
                        Node userNode = userXMLNodes.get(modifyUserName);
                        userNode.getAttributes().getNamedItem(USER_PASSWORD_ATTRIBUTE)
                            .setTextContent(modifyUsers.get(modifyUserName));
                        existingUsers.put(modifyUserName, modifyPassword);
                    }
                } else { // Create a node and add to parent
                    Element newUser = doc.createElement("user");
                    newUser.setAttribute("password", modifyPassword);
                    newUser.setAttribute("name", modifyUserName);
                    usersParent.appendChild(newUser);
                    existingUsers.put(modifyUserName, modifyPassword);
                }
            }

            for (String modifyGroupName : modifyGroups.keySet()) {
                Node groupNode = groupXMLNodes.get(modifyGroupName);
                HashSet<String> groupUserSet = existingGroups.get(modifyGroupName);
                if (groupNode == null) {
                    Element newGroup = doc.createElement("group");
                    newGroup.setAttribute("name", modifyGroupName);
                    groupsParent.appendChild(newGroup);
                    groupNode = newGroup;
                    groupUserSet = new HashSet<>();
                    groupUserSet.add(LocalRealmUsersFlowContext.DUMMY_USER);
                    groupXMLNodes.put(modifyGroupName, newGroup);
                    existingGroups.put(modifyGroupName, groupUserSet);
                }
                for (String modifyUser : modifyGroups.get(modifyGroupName)) {
                    // Add user only if group doesn't already include the user
                    if (!groupUserSet.contains(modifyUser)) {
                        Element newUser = doc.createElement("user");
                        newUser.setAttribute("name", modifyUser);
                        groupNode.appendChild(newUser);
                        groupUserSet.add(modifyUser);
                    }
                }
            }

            // Rewrite the document
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(usersFile);
            transformer.transform(source, result);

            LOGGER.info("Successfully updated " + usersXMLFilePath);
        } else {
            LOGGER.error("{} does not exist", usersFile.getCanonicalPath());
        }
    }
}
