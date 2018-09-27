package com.ca.apm.tests.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.classes.from.appmap.plugin.AnalystHelperServices;
import com.ca.apm.classes.from.appmap.plugin.Attribute;
import com.ca.apm.classes.from.appmap.plugin.AttributesRetrievalOutput;
import com.ca.apm.classes.from.appmap.plugin.ExternalId;
import com.ca.apm.classes.from.appmap.plugin.Vertex;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FetchAttributeInfo {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private RestClient restClient = new RestClient();

    public final String FRONTEND;
    public final String SERVICE_ID;
    public final String APPLICATION_SERVICE = AnalystHelperServices.APPLICATION_SERVICE;

    public FetchAttributeInfo() {
        FRONTEND = "frontend";
        SERVICE_ID = Vertex.MainAttribute.ATTRIBUTE_SERVICE_ID.getName();
    }

    /*
     * Calls AppMap Rest API for a particular vertex ID and fetches its information.
     * If vertex ID is not found returns null.
     * Returns a map of vertexInfo.
     */
    public Map<String, String> fetchVertexInfo(String suspectId, String timestamp, String agcHost, boolean edgeVertexId)
        throws Exception {
 
        String vertexId;
        if(edgeVertexId == true){
            vertexId =  suspectId;
        }else{
            vertexId = Integer.toString(getVertexIDfromExternalID(agcHost, ExternalId.fromString(suspectId).getJustExternalId()));
        }
         
        if("0".equals(vertexId)){
            log.info("ERROR : VertexId not found in appmap_id_mappings table for ExternalId : " + suspectId);
        }
        
        Map<String, String> attributeInfo = new HashMap<String, String>();

        // Check for story in REST API Response
        final String urlPart = "http://" + agcHost + ":8081/apm/appmap/private/attributes/retrieve";

        // Payload for getting vertex information form Rest
        String payload =
            "{\"vertexIds\": [\"" + vertexId + "\"],\"timestamp\" : \"" + timestamp + "\"}";

        if (timestamp == null) {
            payload = "{\"vertexIds\": [\"" + vertexId + "\"]}";
        }
        log.info("Printing AppMap Vertex Retrieve Payload--");
        log.info(payload);

        // Calls AppMap Rest API to get that vertex information
        EmRestRequest request = new EmRestRequest(urlPart, payload);
        IRestResponse<String> response = restClient.process(request);
        String jsonResponse = response.getContent();

        ObjectMapper mapper = new ObjectMapper();
        AttributesRetrievalOutput attRetrieve =
            mapper.readValue(jsonResponse, AttributesRetrievalOutput.class);

        Collection<Attribute> attributes = attRetrieve.getAttributes().get(vertexId);
        Iterator<Attribute> itAttribute = attributes.iterator();

        // If vertex infomation not found return null, else loop attributes to find vertex name and
        // return vertex name
        if (attributes.isEmpty()) {
            return null;
        } else {
            
            //attributeInfo.put("vertexId", vertexId);
            while (itAttribute.hasNext()) {
                Attribute currAttribute = itAttribute.next();
                String attributeName = currAttribute.getName();

                attributeInfo.put(attributeName, currAttribute.getValue());

                // Temporary fix to handle PipeOrgan Application node
                if (SERVICE_ID.equalsIgnoreCase(attributeName)) {
                    if (currAttribute.getValue().equalsIgnoreCase(APPLICATION_SERVICE))
                        attributeInfo.put(FRONTEND, FRONTEND);
                }
            }
        }
        return attributeInfo;
    }
    
    public int getVertexIDfromExternalID(String dbName, String externalId)
            throws ClassNotFoundException, SQLException {

            Connection c = null;
            Statement stmt = null;
            Class.forName("org.postgresql.Driver");
            c =
                DriverManager.getConnection("jdbc:postgresql://" + dbName + ":5432/cemdb", "postgres",
                    "Lister@123");
            String dbQuery =
                "select vertex_id from appmap_id_mappings where external_id="
                    + "'"
                    + externalId
                    + "'";

            log.info("DB Query : " + dbQuery);
            stmt = c.createStatement();
            ResultSet rsQuery = stmt.executeQuery(dbQuery);
            
            int vertexId =0;
            while (rsQuery.next()) {
                vertexId = rsQuery.getInt(1);
            }
            
            return vertexId;
        }
    
    
    public int getEdgesFromVertexID(String dbName, String vertexId)
        throws ClassNotFoundException, SQLException {

        Connection c = null;
        Statement stmt = null;
        Class.forName("org.postgresql.Driver");
        c =
            DriverManager.getConnection("jdbc:postgresql://" + dbName + ":5432/cemdb", "postgres",
                "Lister@123");
        String dbQuery =
            "select distinct target_id from appmap_edges where backend_id="
                + "'"
                + vertexId
                + "'";

        log.info("DB Query : " + dbQuery);
        stmt = c.createStatement();
        ResultSet rsQuery = stmt.executeQuery(dbQuery);
        
        int targetId = 0;
        while (rsQuery.next()) {
            targetId = rsQuery.getInt(1);
        }
        
        return targetId;
    }
}
