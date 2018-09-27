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
package com.ca.apm.test.em.transactiontrace.crosscluster;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Graph object for the json graph obtained using the REST API
 */
public class ATCGraph {

    private static final Logger log = LoggerFactory.getLogger(ATCGraph.class.getSimpleName());
    /** Black list of attributes that should NOT be compared for equality */
    private static final List<String>
        ATTRIBUTES_BLACKLIST = Arrays.asList(new String[] {"agent", "hostname", "Source cluster"});

    private static final String APPLICATION_NAME_ATTRIBUTE_KEY = "applicationName";

    private final Set<Vertex> vertices;
    private final List<Edge> edges;

    private HashMap<String, Vertex> vertexLookupByName;
    private HashMap<String, Vertex> vertexLookupById;
    private HashMap<Vertex, List<Edge>> edgeLookupByTailVertex;

    public ATCGraph() {
        vertices = new HashSet<>();
        edges = new ArrayList<>();
        vertexLookupByName = new HashMap<>();
        vertexLookupById = new HashMap<>();
        edgeLookupByTailVertex = new HashMap<>();
    }

    /* --- Public -- */

    /**
     * creates a new vertex from the given data and adds it to the look-ups
     */
    public Vertex addVertex(String ID, String name, Properties attributes) {
        Vertex vertex = new Vertex(ID, name, attributes);
        vertices.add(vertex);
        vertexLookupByName.put(name, vertex);
        vertexLookupById.put(ID, vertex);
        return vertex;
    }

    /**
     * creates a new edge from the given data and adds it to the look-ups
     * @param tailVertex
     * @param headVertex
     * @param businessTxnId
     * @return
     */
    public Edge addEdge(Vertex tailVertex, Vertex headVertex, String businessTxnId) {
        Edge edge = new Edge(tailVertex, headVertex);
        edge.setBusinessTransactionID(businessTxnId);
        edges.add(edge);
        List<Edge> edgeList = edgeLookupByTailVertex.get(tailVertex);
        if(edgeList == null)
        {
            edgeList = new ArrayList<>();
        }
        edgeList.add(edge);
        edgeLookupByTailVertex.put(tailVertex, edgeList);
        return edge;
    }

    public Vertex fetchVertexFromId(String sourceId) {
        return vertexLookupById.get(sourceId);
    }

    public List<Edge> fetchEdgesForTailVertex(Vertex tail) {
        List<Edge> edgesForVertex = edgeLookupByTailVertex.get(tail);
        if (edgesForVertex != null) {
            return edgesForVertex;
        } else {
            return Collections.emptyList();
        }
    }

    public Vertex fetchVertexFromName(String name) {
        return vertexLookupByName.get(name);
    }

    public Set<Vertex> getVertices() {
        return vertices;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    /* --- Public static -- */

    /**
     * Static method to construct a graph from a {@link com.google.gson.JsonObject}
     * @param jsonGraph
     * @return
     */
    public static ATCGraph createGraphFromJSON(JsonObject jsonGraph) {
        ATCGraph graph = new ATCGraph();
        JsonArray jsonVertices = jsonGraph.getAsJsonArray("vertices");
        Iterator<JsonElement> vertexIterator = jsonVertices.iterator();
        while (vertexIterator.hasNext())
        {
            JsonObject vertexObject = vertexIterator.next().getAsJsonObject();
            Properties attributes = getVertexAttributesFromJson(vertexObject);
            graph.addVertex(vertexObject.get("vertexId").getAsString(),attributes.getProperty("name"),attributes);
        }
        JsonArray jsonEdges = jsonGraph.getAsJsonArray("edges");
        Iterator<JsonElement> edgeIterator = jsonEdges.iterator();
        while (edgeIterator.hasNext())
        {
            JsonObject edgeObject = edgeIterator.next().getAsJsonObject();
            String sourceId = edgeObject.get("sourceId").getAsString();
            String targetId = edgeObject.get("targetId").getAsString();
            JsonElement btElement = edgeObject.get("businessTransactionId");
            String businessTxnId = btElement == null ? null : btElement.getAsString();
            Vertex tailVertex = graph.fetchVertexFromId(sourceId);
            Vertex headVertex = graph.fetchVertexFromId(targetId);
            graph.addEdge(tailVertex, headVertex, businessTxnId);
        }
        return graph;
    }

    /**
     * Static method to create graph from a json resource file
     * @param filePath
     * @return
     * @throws IOException
     */
    public static ATCGraph createGraphFromFile(String filePath) throws IOException {
        InputStream inputStream = ATCGraph.class.getResourceAsStream(filePath);
        String jsonString = IOUtils.toString(inputStream);
        inputStream.close();

        JsonObject jsonGraph = new JsonParser().parse(jsonString).getAsJsonObject();
        return createGraphFromJSON(jsonGraph);
    }

    /**
     * Static method to test for equivalence of two graphs
     * This method tests if all the vertices and edges present in the expectedGraph are represented
     * in the actual graph. The test fails if the actual graph does not contain either a vertex or a
     * vertex attribute(except attributes listed in ATTRIBUTES_BLACKLIST) or a an edge that is present
     * in the expected graph. If the actual graph contains something that was not in the expected graph
     * , but also should not be there in the actual graph, this test does not check for that.
     * @param actualGraph
     * @param expectedGraph
     */
    public static void testForEquivalence(ATCGraph actualGraph, ATCGraph expectedGraph) {
        // Map of equivalent vertices in expected vs actual
        HashMap<Vertex, Vertex> vertexMapping = new HashMap<>();
        Vertex actualVertex;
        int missingVertexCount = 0;
        int missingEdgeCount = 0;
        int missingAttributeCount = 0;
        int expectedVertexCount = expectedGraph.getVertices().size();
        int expectedEdgeCount = expectedGraph.getEdges().size();
        // Check if all vertices exist
        for (Vertex expectedVertex : expectedGraph.getVertices()) {
            actualVertex = actualGraph.fetchVertexFromName(expectedVertex.getName());
            if (actualVertex == null) {
                missingVertexCount++;
                log.info(expectedVertex.getName() + " missing in actual graph");
                continue;
            }
            // Check if the vertex has all attributes not in the black list
            missingAttributeCount += checkUnlistedAttributes(actualVertex, expectedVertex, ATTRIBUTES_BLACKLIST);
            vertexMapping.put(expectedVertex, actualVertex);
        }
        // Check if all edges exist
        Vertex equivalentActualTail, equivalentActualHead;
        Edge equivalentEdge = null;
        for (Edge expectedEdge : expectedGraph.getEdges()) {
            Vertex expectedTail = expectedEdge.getTailVertex();
            Vertex expectedHead = expectedEdge.getHeadVertex();
            equivalentActualTail = vertexMapping.get(expectedTail);
            equivalentActualHead = vertexMapping.get(expectedHead);
            if(equivalentActualTail == null || equivalentActualHead == null) {
                missingEdgeCount++;
                log.info(expectedEdge + "  missing in actual graph");
                continue;
            }
            for(Edge actualPotentialEdge: actualGraph.fetchEdgesForTailVertex(equivalentActualTail)) {
                if(equivalentActualHead.equals(actualPotentialEdge.getHeadVertex())) {
                    equivalentEdge = actualPotentialEdge;
                    break;
                }
            }
            if(equivalentEdge == null) {
                missingEdgeCount++;
                log.info(expectedEdge + "  missing in actual graph");
                continue;
            }
        }

        // Fail test if we have missing vertices or attributes or edges
        Assert.assertEquals(missingVertexCount, 0, "Actual graph is missing " + missingVertexCount + " vertices out of " + expectedVertexCount + " expected.");
        Assert.assertEquals(missingAttributeCount, 0, "Missing some attributes.");
        Assert.assertEquals(missingEdgeCount, 0, "Actual graph is missing " + missingEdgeCount + " edges out of " + expectedEdgeCount + " expected.");

        log.info("All vertices and edges matched");
    }

    /**
     * Tests a graph does not contain any vertices from the given applicationName
     * Test fails if the graph contains at least one vertex from the given application
     * @param currentGraph
     * @param applicationName
     */
    public static void testGraphForGoneAwayApplication(ATCGraph currentGraph, String applicationName) {
        for (Vertex currentVertex : currentGraph.getVertices()) {
            if(currentVertex.getAttributes().containsKey(APPLICATION_NAME_ATTRIBUTE_KEY)) {
                String value = currentVertex.getAttributes().getProperty(APPLICATION_NAME_ATTRIBUTE_KEY);
                if(value != null) {
                    Assert.assertFalse(value.equals(applicationName), "Vertices belonging to " + applicationName + " should not be present");
                }
            }
        }
    }

    /* --- Private static helper methods --- */

    /**
     * Returns JsonObject as properties
     * @param vertexObject
     * @return
     */
    private static Properties getVertexAttributesFromJson(JsonObject vertexObject) {
        Properties properties = new Properties();
        JsonObject attributes = vertexObject.getAsJsonObject("attributes");
        for(Map.Entry<String, JsonElement> entry : attributes.entrySet()) {
            if(!entry.getValue().isJsonNull()) {
                properties.put(entry.getKey(), entry.getValue().toString());
            } else {
                properties.put(entry.getKey(), "null");
            }
        }
        return properties;
    }

    /**
     * Compares attributes of expected and actual vertices and reports the missing count
     *
     * @param actualVertex
     * @param expectedVertex
     * @param attributesBlacklist
     * @return
     */
    private static int checkUnlistedAttributes(Vertex actualVertex, Vertex expectedVertex, List<String> attributesBlacklist) {
        int missedAttributeCount = 0;
        Properties expectedAttributes = expectedVertex.getAttributes();
        Properties actualAttributes = actualVertex.getAttributes();
        for(String expectedAttributeName : expectedAttributes.stringPropertyNames()) {
            if(attributesBlacklist.contains(expectedAttributeName)) {
                continue;
            }
            if(!actualAttributes.containsKey(expectedAttributeName)) {
                log.info("Attribute " + expectedAttributeName + " for " + actualVertex + " missing in actual graph");
                missedAttributeCount++;
                continue;
            }
            String expectedAttributeValue = expectedAttributes.getProperty(expectedAttributeName);
            String actualAttributeValue = actualAttributes.getProperty(expectedAttributeName);
            if(expectedAttributeValue!=null) {
              if(!expectedAttributeValue.equals(actualAttributeValue)) {
                  log.info(actualVertex + " : " + expectedAttributeName + " has " + actualAttributeValue + " instead of " + expectedAttributeName);
                  missedAttributeCount++;
              }
            }
        }
        return missedAttributeCount;
    }

    public class Vertex {
        private final String ID;
        private final String name;
        private final Properties attributes;

        Vertex(String ID, String name, Properties attributes) {
            this.ID = ID;
            if(name == null || name.isEmpty())
            {
                throw new IllegalArgumentException("name cannot be null");
            }
            this.name = name;
            if(attributes == null) {
                attributes = new Properties();
            }
            this.attributes = attributes;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Vertex vertex = (Vertex) o;

            if (!name.equals(vertex.name))
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }

        public String getID() {
            return ID;
        }

        public String getName() {
            return name;
        }

        public Properties getAttributes() {
            return attributes;
        }
    }

    public class Edge {
        private final Vertex tailVertex;
        private final Vertex headVertex;
        private String businessTransactionID;

        Edge(Vertex tailVertex, Vertex headVertex) {
            if(tailVertex == null)
            {
                throw new IllegalArgumentException("tail vertex cannot be null");
            }
            this.tailVertex = tailVertex;
            if(headVertex == null)
            {
                throw new IllegalArgumentException("head vertex cannot be null");
            }
            this.headVertex = headVertex;
        }

        @Override
        public String toString() {
            return tailVertex.toString() + " --> " + headVertex.toString();
        }

        public Vertex getTailVertex() {
            return tailVertex;
        }

        public Vertex getHeadVertex() {
            return headVertex;
        }

        public String getBusinessTransactionID() {
            return businessTransactionID;
        }

        public void setBusinessTransactionID(String businessTransactionID) {
            this.businessTransactionID = businessTransactionID;
        }
    }
}
