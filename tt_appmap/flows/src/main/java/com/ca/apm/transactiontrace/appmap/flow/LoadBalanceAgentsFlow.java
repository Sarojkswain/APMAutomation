/*
 * Copyright (c) 2015 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.transactiontrace.appmap.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;

/**
 * LoadBalanceAgentsFlow
 *
 * Distribute agents using loadbalancing.xml
 *
 * Should be run on the same machine as MOM
 *
 * @author ...
 */
@Flow
public class LoadBalanceAgentsFlow extends FlowBase {

    @SuppressWarnings("unused")
    private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalanceAgentsFlow.class);

    @FlowContext
    private LoadBalanceAgentsFlowContext context;

    /*
     *  Modifies loadbalancing XML file to distribute agents to different collectors using
     *  loadbalancing.xml file
     */
    @Override
    public void run() throws Exception {
        String[] agents = context.getAgentNames();
        String[] collectors = context.getCollectorInfo();

        String specifierPrefix = context.getAgentSpecifierPrefix();

        File lbXMLFile = new File(context.getLoadbalancingXMLPath());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbFactory.newDocumentBuilder();
        Document doc = docBuilder.parse(lbXMLFile);

        doc.getDocumentElement().normalize();

        Node loadbalancing = doc.getElementsByTagName("loadbalancing").item(0);

        for(int i=0;i<agents.length;i++) {
            String agent = agents[i];
            String collectorHost = collectors[i];
            String collectorPort = "5001";
            Element agentCollector = doc.createElement("agent-collector");
            Element agentSpecifier = doc.createElement("agent-specifier");
            Element include = doc.createElement("include");
            Element collector = doc.createElement("collector");

            agentCollector.setAttribute("name", agent);
            agentSpecifier.appendChild(doc.createTextNode(specifierPrefix + agent));
            collector.setAttribute("host",collectorHost);
            collector.setAttribute("port", collectorPort);
            include.appendChild(collector);

            agentCollector.appendChild(agentSpecifier);
            agentCollector.appendChild(include);

            loadbalancing.appendChild(agentCollector);
            LOGGER.info("Added child " + agentCollector);
        }

        // Write contents to xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(context.getLoadbalancingXMLPath()));
        transformer.transform(source,result);

        LOGGER.info("Successfully modified loadbalancing XML file");
    }
}
