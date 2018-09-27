/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

package com.ca.apm.test.em.appmap;

import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ca.tas.test.TasTestNgTest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlow;
import com.ca.apm.automation.action.flow.utility.FileCreatorFlowContext;
import com.ca.apm.test.em.util.RestUtility;
import com.ca.tas.test.em.appmap.GartnerDemoTestbed;
import com.ca.tas.test.em.appmap.PhantomJSTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class GartnerDemoTest extends TasTestNgTest  {
    
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RestUtility utility = new RestUtility();

    private List<String> addPerspectivesContent = Arrays.asList(
                "#!/bin/sh",
                ". /opt/setup/env.sh",
                "PGUSER=$DB_ADMIN_USER PGPASSWORD=$DB_ADMIN_USER_PWD $DB_HOME/bin/psql -d $DB_NAME -f /opt/gartnerdemo/perspectives.sql",
                "PGUSER=$DB_ADMIN_USER PGPASSWORD=$DB_ADMIN_USER_PWD $DB_HOME/bin/psql -d $DB_NAME -f /opt/gartnerdemo/attribute_rules.sql");

    @BeforeMethod
    public void initTestMethod(Method testMethod) {
    }

    @Tas(testBeds = @TestBed(name = GartnerDemoTestbed.class, executeOn = GartnerDemoTestbed.MACHINE_ID), owner = "korzd01", size = SizeType.SMALL)
    @Test(groups = {"appmap", "smoke"})
    public void configureTestbed() throws Exception {
        loginToApm();

        FileCreatorFlowContext apContext = new FileCreatorFlowContext.Builder()
                .fromData(addPerspectivesContent)
                .destinationPath("/opt/gartnerdemo/temp.sh").build();
        runFlowByMachineId(GartnerDemoTestbed.MACHINE_ID, FileCreatorFlow.class, apContext);

        runCommandFlowByMachineId(GartnerDemoTestbed.MACHINE_ID, new RunCommandFlowContext.Builder("chmod")
                    .args(Arrays.asList("+x", "/opt/gartnerdemo/temp.sh")).build());
        log.info("--- add perspectives and attribute_rules ---");
        runCommandFlowByMachineId(GartnerDemoTestbed.MACHINE_ID,
            new RunCommandFlowContext.Builder("./temp.sh").workDir("/opt/gartnerdemo").build());

        log.info("--- start-replay ---");
        runCommandFlowByMachineId(GartnerDemoTestbed.MACHINE_ID,
            new RunCommandFlowContext.Builder("./3-start-replay.sh").workDir("/opt/gartnerdemo").build());
        log.info("--- send-traces ---");
        runCommandFlowByMachineId(GartnerDemoTestbed.MACHINE_ID,
            new RunCommandFlowContext.Builder("./4-send-traces.sh").workDir("/opt/gartnerdemo").build());
        
        assignAttributes();
    }

    private void assignAttributes() throws Exception {
        final String emHost = envProperties.getMachineHostnameByRoleId(GartnerDemoTestbed.EM_ROLE_ID);
        
        final List<Map<String, String>> attributesList = parseAttributes("/opt/gartnerdemo/attributes/gartner_attributes.csv");
        utility.processVertices(emHost, new RestUtility.VertexCallback() {
            @Override
            public void noticeVertex(String vertexId, JsonObject attributes) {
                for (Map<String, String> csvAttributes : attributesList) {
                    String hostname = csvAttributes.get("hostname");
                    boolean update = false;
                    JsonElement aHostnameE = attributes.get("hostname");
                    if (hostname != null && aHostnameE != null && hostname.equalsIgnoreCase(aHostnameE.getAsString())) {
                        update = true;
                    } else {
                        String tn = csvAttributes.get("transactionName");
                        JsonElement atnE = attributes.get("transactionName");
                        if (tn != null && atnE != null && tn.equalsIgnoreCase(atnE.getAsString())) {
                            update = true;
                        }
                    }
                    if (update) {
                        for (Map.Entry<String, String> entry : csvAttributes.entrySet()) {
                            utility.assignAttributeToVertices(emHost, Arrays.asList(vertexId), entry.getKey(), entry.getValue());
                        }
                    }
                }
            }
        });
    }

    private void loginToApm() throws Exception {
        PhantomJSTest.execute("login.js", envProperties);
        log.info("Login was successfully.");
    }

    private List<Map<String, String>> parseAttributes(String csvFile) {
        BufferedReader br = null;
        String line;
        String cvsSplitBy = ",";
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();

        try {
            br = new BufferedReader(new FileReader(csvFile));
            String[] headers = br.readLine().split(cvsSplitBy);
            while ((line = br.readLine()) != null) {
                String[] elements = line.split(cvsSplitBy);
                Map<String, String> subResult = new HashMap<String, String>();
                
                for (int i = 0; i < Math.min(headers.length, elements.length); ++i) {
                    if (!elements[i].isEmpty()) {
                        subResult.put(headers[i], elements[i]);
                    }
                }
                result.add(subResult);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
