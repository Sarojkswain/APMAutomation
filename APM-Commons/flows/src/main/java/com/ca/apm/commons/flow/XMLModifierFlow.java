package com.ca.apm.commons.flow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ca.apm.commons.common.XMLFileUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.IAutomationFlow;
import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.commons.coda.common.ApmbaseConstants;
import com.ca.apm.commons.coda.common.XMLUtil;

/**
 * Created by nick on 8.10.14.
 */
@Flow
public class XMLModifierFlow implements IAutomationFlow {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(XMLModifierFlow.class);
	private int ret = 1;
	private String str;
	@FlowContext
	private XMLModifierFlowContext flowContext;
	XMLUtil xmlUtil = new XMLUtil();
	XMLFileUtil xmlFileUtil = new XMLFileUtil();

	@Override
	public void run() throws Exception {

		String methodName = flowContext.getMethodName().trim();
		List<String> arguments = flowContext.getArguments();
		LOGGER.debug(methodName);

		switch (methodName) {
		case "addHttpEntryInEMJetty": {
			if (arguments.size() == 1) {

				ret = xmlUtil.addHttpEntryInEMJetty(arguments.get(0));
				LOGGER.debug("Completed with return value " + ret);
			}
		}

			break;

		case "addCustomHttpEntryInEMJetty": {
			if (arguments.size() == 2) {

				ret = xmlUtil.addCustomHttpEntryInEMJetty(arguments.get(0),
						arguments.get(1));
				LOGGER.debug("Completed with return value " + ret);
			}
		}

			break;

		case "addEmptyCollectorEntryInLoadbalanceXML": {
			if (arguments.size() == 4) {
				ret = xmlUtil.addEmptyCollectorEntryInLoadbalanceXML(
						arguments.get(0), arguments.get(1), arguments.get(2),
						arguments.get(3));
			}
		}

			break;

		case "xmlFileUtil.updateXmlFile": {
			if (arguments.size() == 4) {
				ret = xmlFileUtil.updateXmlFile(arguments.get(0), arguments
						.get(1).split(":::")[0], arguments.get(2), arguments
						.get(3));
				ret = xmlFileUtil.updateXmlFile(arguments.get(0), arguments
						.get(1).split(":::")[1], arguments.get(2), arguments
						.get(3));
				ret = xmlFileUtil.updateXmlFile(arguments.get(0), arguments
						.get(1).split(":::")[2], arguments.get(2), arguments
						.get(3));
			}
		}

			break;

		case "xmlFileUtil.updateEMJettyVerifyHostName": {
			if (arguments.size() == 3) {
				ret = xmlFileUtil.updateXmlFile(arguments.get(0),
						ApmbaseConstants.verifyHostnamesExpr, arguments.get(1),
						arguments.get(2));
			}
		}

			break;

		case "xmlFileUtil.updateEMJettyValidateCertificate": {
			if (arguments.size() == 3) {
				ret = xmlFileUtil.updateXmlFile(arguments.get(0),
						ApmbaseConstants.validateCertificatesExpr,
						arguments.get(1), arguments.get(2));
			}
		}

			break;

		case "xmlFileUtil.updateEMJettyNeedClientAuth": {
			if (arguments.size() == 3) {
				ret = xmlFileUtil.updateXmlFile(arguments.get(0),
						ApmbaseConstants.needClientAuthExpr, arguments.get(1),
						arguments.get(2));
			}
		}

			break;

		case "XMLUtil.changeAttributeValue": {
			if (arguments.size() == 5) {

				str = XMLUtil.changeAttributeValue(arguments.get(0),
						arguments.get(1), arguments.get(2), arguments.get(3),
						arguments.get(4));
				if (str.equalsIgnoreCase(XMLUtil.SUCCESS_MESSAGE))
					ret = 1;
				else
					ret = 0;
			}
		}

			break;
		
		case "createCustomDomain":{
           		LOGGER.info("Creating a domain in the specified EM");
   				Map<String, String> userMap = new HashMap<String, String>();
   				userMap.put("admin", "full");    				
   				XMLUtil.createDomain(arguments.get(0), arguments.get(1),"Custom Domain", arguments.get(2), userMap, null);
            }	
			break;
			
		default:
			System.out.println("No matching method in XMLModifierFlow class");
			break;

		}		
	}
}
