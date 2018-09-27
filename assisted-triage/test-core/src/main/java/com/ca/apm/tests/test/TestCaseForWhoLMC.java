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

package com.ca.apm.tests.test;

import static org.testng.Assert.fail;



import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;



import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.classes.from.appmap.plugin.AtStory;
import com.ca.apm.classes.from.appmap.plugin.AtStoryList;
import com.ca.apm.classes.from.appmap.plugin.Specifier.SpecifierType;
import com.ca.apm.classes.from.appmap.plugin.VertexType;
import com.ca.apm.tests.testbed.AssistedTriageTestbed;
import com.ca.apm.tests.testbed.DevStandAloneTestBed;
import com.ca.apm.tests.utils.Common;
import com.ca.apm.tests.utils.FetchATStories;
import com.ca.apm.tests.utils.FetchAttributeInfo;
import com.ca.apm.tests.utils.FetchMetricsUtils;
import com.ca.apm.tests.utils.LocalStorage;
import com.ca.apm.tests.utils.ValidateVertexInfo;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;


public class TestCaseForWhoLMC extends TasTestNgTest {

	private final Logger log = LoggerFactory.getLogger(getClass());
	private Common common = new Common();

	private RunCommandFlowContext runCommandFlowContext;


	private FetchATStories atStories = new FetchATStories();

	private static Timestamp start_Time;
	private String restTimestamp = null;;

	private String agcHost = envProperties
			.getMachineHostnameByRoleId(AssistedTriageTestbed.SA_MASTER_EM_ROLE);

	private String batFile = "run.bat";
	private String batLocation = AssistedTriageTestbed.TOMCAT_INSTALL_DIR
			+ "\\webapps\\pipeorgan\\WEB-INF\\lib\\";
	private String scenarioFolderName = "scenarios";



	private static String expectedImpactedComponentPreFix = "ATC:PipeOrgan Application:GENERICFRONTEND:Apps|PipeOrgan Application|URLs|/pipeorgan/ExecutorServlet_17:";
	private static String seperator = "####";
	private static String expectedImpactedComponentSuffix = "Tomcat Agent:Tomcat";


	@BeforeMethod
	public void initTestMethod(Method testMethod) {
		@SuppressWarnings("unused")
		LocalStorage localStorage = new LocalStorage(testMethod);
	}

	@Tas(testBeds = @TestBed(name = AssistedTriageTestbed.class, executeOn = AssistedTriageTestbed.SA_MASTER), size = SizeType.MEDIUM, owner = "katan09")
	@Test(groups = { "pipeorgan_who_feature" })
	private void testCase_NoBTSall_with_LMC_as_GF() throws Exception {
		launchScenarios(20);
		testCase_NoBTStall(VertexType.Type.GENERICFRONTEND);
	}

	private void testCase_NoBTStall(VertexType.Type lmcVertexType)
			throws Exception {
		// Initialize query time for REST
		start_Time = common.getCurrentTimeinISO8601Format(0);
		Timestamp end_time = common.getCurrentTimeinISO8601Format(2);

		log.info("Generating Problem - Sleeping for 6 mins");
		Thread.sleep(360000);

		// Check for story in REST API Response
		final String urlPart = "http://" + agcHost
				+ ":8081/apm/appmap/private/triage/stories";

		// Fetch detailed story information
		AtStoryList stories = atStories.fetchStories(urlPart, start_Time,
				end_time, false);
		restTimestamp = common.timestamp2String(common
				.getCurrentTimeinISO8601Format(0));

		// make sure stories non-empty
		boolean foundExcpeted = false;
		if (stories == null || stories.getStories() == null
				|| stories.getStories().isEmpty()) {
			fail("No suitable response for stories REST. Empty or null");
		} else {
			Iterator<AtStory> itStory = stories.getStories().iterator();
			while (itStory.hasNext()) {
				AtStory storyLocation = itStory.next();
				foundExcpeted = findExpectedImpactedComponent(storyLocation);
				if(foundExcpeted)
					break;
			}
		}
		if (!foundExcpeted) {
			fail("Expected impacted component:"
					+ expectedImpactedComponentPreFix + seperator
					+ expectedImpactedComponentSuffix + " not found");
		} else {
			log.info("Expected impacted component:"
					+ expectedImpactedComponentPreFix + seperator
					+ expectedImpactedComponentSuffix + " found");
		}
	}

	private boolean findExpectedImpactedComponent(AtStory currStory)
			throws Exception {
		log.info("Checking impacted component on story with ids:{}", currStory.getStoryIds());
		Collection<String> impacts = currStory.getImpactVertexIds();
		if (impacts == null) {
			log.info("No Impacts found for story with ids:{}", currStory.getStoryIds());
			return false;
		}
		Iterator<String> itImpact = impacts.iterator();
		while (itImpact.hasNext()) {
			String currImpact = itImpact.next();
			log.info("Current impact:{}",currImpact);
			if (currImpact.indexOf(expectedImpactedComponentPreFix) >= 0) {
				return true;
			}
			log.info("Current impact:{} not matched",currImpact);
		}
		return false;
	}

	private void launchScenarios(int numberOflaunches) {
		for (int i = 0; i < numberOflaunches; i++) {
			runCommandFlowContext = common.runPipeOrganScenario(batLocation,
					batFile, scenarioFolderName, "Problem-NoBTStall.xml");
			runCommandFlowByMachineId(AssistedTriageTestbed.COL_TO_MOM_PROVIDER,
					runCommandFlowContext);
		}
	}

}
