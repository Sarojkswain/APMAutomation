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

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static com.ca.apm.automation.action.flow.em.config.ModifyDomainsXmlFlowContext.*;

/**
 * Modifies domains.xml to add/replace domains according to the provided configuration
 */
@Flow
public class ModifyDomainsXmlFlow extends FlowBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModifyDomainsXmlFlow.class);

    private static final String SUPER_DOMAIN = "SuperDomain";
    private static final String AGENT_TAG = "agent";
    private static final String GRANT_TAG = "grant";
    private static final String DOMAIN_TAG = "domain";
    private static final String MAPPING_ATTR = "mapping";
    private static final String USER_ATTR = "user";
    private static final String GROUP_ATTR = "group";
    private static final String PERMISSION_ATTR = "permission";
    private static final String NAME_ATTR = "name";

    @FlowContext
    ModifyDomainsXmlFlowContext flowContext;

    @Override
    public void run() throws Exception {
        String domainsXMLFilePath = flowContext.getDomainsXMLFilePath();
        File domainsXml = FileUtils.getFile(domainsXMLFilePath);
        Map<String, ModifyDomainsXmlFlowContext.Domain> existingDomains = new LinkedHashMap<>();
        Map<String, Node> domainXMLNodeMap = new HashMap<>();
        Map<String, Domain> modifyDomains = flowContext.getDomainMap();

        if (domainsXml.exists()) {
            LOGGER.info("Modifying " + domainsXMLFilePath);

            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(domainsXml);
            doc.getDocumentElement().normalize();

            // Read existing domains
            Node domainsParent = doc.getElementsByTagName("domains").item(0);
            NodeList domainList = domainsParent.getChildNodes();
            for (int i = 0; i < domainList.getLength(); i++) {
                Node eachDomain = domainList.item(i);
                if (eachDomain.getNodeType() == Node.ELEMENT_NODE) {
                    String domainName = eachDomain.getNodeName();
                    if (domainName.equals(DOMAIN_TAG)) {
                        domainName =
                            eachDomain.getAttributes().getNamedItem(NAME_ATTR).getTextContent();
                    }
                    Domain existingDomain = new Domain(domainName);
                    domainXMLNodeMap.put(domainName, eachDomain);
                    NodeList domainChildren = eachDomain.getChildNodes();
                    for (int j = 0; j < domainChildren.getLength(); j++) {
                        Node domainChild = domainChildren.item(j);
                        if (domainChild.getNodeType() == Node.ELEMENT_NODE) {
                            String childName = domainChild.getNodeName();
                            if (AGENT_TAG.equals(childName)) {
                                String specifier =
                                    domainChild.getAttributes().getNamedItem(MAPPING_ATTR)
                                        .getTextContent();
                                existingDomain.addAgentSpecifier(specifier);
                            } else if (GRANT_TAG.equals(childName)) {
                                Grant grant = parseGrant(domainChild);
                                existingDomain.addGrant(grant);
                            }
                        }
                    }
                    existingDomains.put(domainName, existingDomain);
                }
            }

            // Always insert above the existing configuration
            Node insertBefore = domainsParent.getFirstChild();

            // Replace or insert domains
            for (String modifyDomainName : modifyDomains.keySet()) {
                Domain domainToAdd = modifyDomains.get(modifyDomainName);
                Element newDomainNode = createNewDomainElement(doc, domainToAdd);
                // If domain already exists, replace it
                if (existingDomains.containsKey(modifyDomainName)) {
                    Node replaceNode = domainXMLNodeMap.get(modifyDomainName);
                    domainsParent.replaceChild(newDomainNode, replaceNode);
                    domainXMLNodeMap.put(modifyDomainName, newDomainNode);
                    existingDomains.put(modifyDomainName, domainToAdd);
                } else {
                    domainsParent.insertBefore(newDomainNode, insertBefore);
                    insertBefore = newDomainNode;
                }
            }

            // Rewrite the document
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer = tFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(domainsXml);
            transformer.transform(source, result);

        } else {
            LOGGER.error("File " + domainsXml.getCanonicalPath() + " not found. Nothing changed");
        }
    }

    /*
     * Creates a new Node to be added to the document
     */
    private Element createNewDomainElement(Document doc, Domain domainToAdd) {
        String domainName = domainToAdd.getName();
        Set<String> agentSpecifiers = domainToAdd.getAgentSpecifiers();
        Set<Grant> grants = domainToAdd.getGrants();
        Element newDomain;
        if (domainName.equals(SUPER_DOMAIN)) {
            newDomain = doc.createElement(SUPER_DOMAIN);
        } else {
            newDomain = doc.createElement(DOMAIN_TAG);
            newDomain.setAttribute(NAME_ATTR, domainName);
        }
        for (String specifier : agentSpecifiers) {
            Element agentChild = doc.createElement(AGENT_TAG);
            agentChild.setAttribute(MAPPING_ATTR, specifier);
            newDomain.appendChild(agentChild);
        }
        for (Grant grant : grants) {
            Element grantChild = doc.createElement(GRANT_TAG);
            if (grant.getType().equals(Grant.Principal.USER)) {
                grantChild.setAttribute(USER_ATTR, grant.getName());
            } else if (grant.getType().equals(Grant.Principal.GROUP)) {
                grantChild.setAttribute(GROUP_ATTR, grant.getName());
            }
            grantChild.setAttribute(PERMISSION_ATTR, grant.getPermission());
            newDomain.appendChild(grantChild);
        }
        return newDomain;
    }

    private Grant parseGrant(Node domainChild) {
        Grant.Principal type = null;
        String principalName = null;
        Node permittedUser = domainChild.getAttributes().getNamedItem(USER_ATTR);
        Node permittedGroup = domainChild.getAttributes().getNamedItem(GROUP_ATTR);
        if (permittedUser != null) {
            type = Grant.Principal.USER;
            principalName = permittedUser.getNodeValue();
        } else if (permittedGroup != null) {
            type = Grant.Principal.GROUP;
            principalName = permittedGroup.getNodeValue();
        }
        String permission =
            domainChild.getAttributes().getNamedItem(PERMISSION_ATTR).getNodeValue();
        return new Grant(type, principalName, permission);
    }
}
