package com.ca.apm.tests.utils;

import static org.testng.Assert.fail;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ValidateVertexInfo {
    private final Logger log = LoggerFactory.getLogger(getClass());

    // Takes input as story data i.e. Culprit Id, Culprit Name and vertex Info and verifies culprit
    // information
    @SuppressWarnings("null")
    public void verifyCulpritInfo(String culpritId, String culpritName, String culpritAppName,
        String culpritType, Map<String, String> vertexInfo) {

        // Check to see if culpritName is null, if true - fail
        if (culpritId.isEmpty()) {
            fail(" No Culprit ID found for evidence");
        } else {
            // Handles Culprit info verification
            String vertexName = vertexInfo.get("name");
            String vertexAppName = vertexInfo.get("applicationName");
            String vertexType = vertexInfo.get("type");


            // If culprit name in AT Story Rest Response is null, fail
            if (culpritName == null || culpritName.isEmpty()) {
                fail(" Culprit Name not found for Culprit ID: " + culpritId + " in AT Story");
            }

            // If culprit app name in AT Story Rest Response is null, fail
            if (culpritAppName == null || culpritAppName.isEmpty()) {
                fail(" Culprit App Name not found for Culprit ID: " + culpritId
                    + " and Culprit Name: " + culpritName + " in AT Story");
            }

            // If culprit type name in AT Story Rest Response is null, fail
            if (culpritType == null || culpritType.isEmpty()) {
                fail(" Culprit Type not found for Culprit ID: " + culpritId + " and Culprit Name: "
                    + culpritName + " in AT Story");
            }

            // If vertex name in AppMap Rest Response is null, fail
            if (vertexName == null) {
                fail(" Vertex Name not found for Culprit ID: " + culpritId + " and Culprit Name: "
                    + culpritName);
            }

            // If vertex type in AppMap Rest Response is null, fail
            if (vertexType == null) {
                fail(" Vertex type not found for Culprit ID: " + culpritId + " and Culprit Type: "
                    + culpritType);
            }

            if (vertexAppName == null || vertexAppName.isEmpty()) {
                log.info(" Vertex App Name not found for Culprit ID: " + culpritId
                    + " and Culprit Name: " + culpritName + ". Setting AppName to 'Unknown'.");
                vertexAppName = "Unknown";
            }

            // If vertex name matches, then log and continue else fail.
            if (culpritName.equals(vertexName) && culpritAppName.equalsIgnoreCase(vertexAppName)
                && culpritType.equals(vertexType)) {
                log.info("Test Pass: Vertex Information found for Vertex ID: " + culpritId
                    + " ; Vertex Name: " + vertexName + " ; Vertex App Name: " + vertexAppName
                    + " ; Vertex Type: " + vertexType);
            } else {
                fail("Failed to match Culprit Information for Culprit ID: " + culpritId
                    + " ; Culprit Name: " + culpritName + " and Vertex Name: " + vertexName
                    + " ; Culprit App Name: " + culpritAppName + " and Vertex App Name: "
                    + vertexAppName + " ; Culprit Type: " + culpritType + " and Vertex Type: "
                    + vertexType);
            }
        }
    }

}
