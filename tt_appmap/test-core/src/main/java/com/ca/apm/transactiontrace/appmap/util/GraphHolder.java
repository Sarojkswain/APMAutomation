/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.transactiontrace.appmap.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Class for very simple graph matcher which works based only on Vertex name
 */
public class GraphHolder {

    private List<String> vertices = new ArrayList<>();
    private Multimap<String, String> edges = TreeMultimap.create();

    private static final Logger logger = LoggerFactory.getLogger(GraphHolder.class);

    /**
     * Loads the expected json graph into a simple graph for comparison
     */
    public static GraphHolder initGraph(String jsonGraph) {
        GraphHolder ret = new GraphHolder();
        JsonElement jsonElement = new JsonParser().parse(jsonGraph);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray jsonVertices = jsonObject.getAsJsonArray("vertices");
        JsonArray jsonEdges = jsonObject.getAsJsonArray("edges");

        // Add all vertices
        final HashMap<String, String> idToVertex = new HashMap<>();
        for (JsonElement vertex : jsonVertices) {
            String vertexId = vertex.getAsJsonObject().get("vertexId").getAsString();
            final String name =
                vertex.getAsJsonObject().get("attributes").getAsJsonObject().get("name")
                    .getAsString();
            ret.addVertex(name);
            idToVertex.put(vertexId, name);
        }

        // Add all edges
        for (JsonElement edge : jsonEdges) {
            final String srcId = edge.getAsJsonObject().get("sourceId").getAsString();
            final String srcName = idToVertex.get(srcId);
            final String tgtId = edge.getAsJsonObject().get("targetId").getAsString();
            final String tgtName = idToVertex.get(tgtId);
            ret.addEdge(srcName, tgtName);
        }
        return ret;
    }

    /**
     * Creates new vertex with a given name
     */
    public void addVertex(String name) {
        if (!vertices.contains(name)) {
            vertices.add(name);
        }
    }

    /**
     * Creates new edge. Both Vertices must exist for it to succeed.
     */
    public void addEdge(String head, String tail) {

        Assert.assertTrue(vertices.contains(head), "Vertex '" + head + "' does not exist");
        Assert.assertTrue(vertices.contains(tail), "Vertex '" + tail + "' does not exist");

        edges.put(head, tail);
    }

    /**
     * Returns all graph vertices
     */
    public List<String> getVertices() {
        return vertices;
    }

    /**
     * Returns all graph edges
     */
    public Multimap<String, String> getEdges() {
        return edges;
    }

    /**
     * Checks this graph vs. expected one
     */
    public void assertEqualsTo(final GraphHolder expected) {

        logger.info("Checking Graph == " + this);

        // check vertices
        final List<String> copyVertices = new ArrayList<>(vertices);
        for (String name : expected.getVertices()) {
            Assert.assertTrue(vertices.contains(name), "Vertex '" + name + "' must exist by now");
            copyVertices.remove(name);
        }
        Assert.assertTrue(copyVertices.isEmpty(), "Found vertices which shouldn't be there : "
            + copyVertices);

        // check edges
        final Multimap<String, String> missingEdges = findDifference(expected.getEdges(), edges);
        if (missingEdges.size() > 0) {
            logger.error("Expected : " + expected.getEdges().size() + " edges but got : "
                + edges.size() + ". Missing edges :");
            for (Map.Entry<String, String> entry : missingEdges.entries()) {
                logger.error("    " + entry.getKey() + " --> " + entry.getValue());
            }
            throw new RuntimeException("Found missing edges : " + missingEdges);
        }

        final Multimap<String, String> extraEdges = findDifference(edges, expected.getEdges());
        if (extraEdges.size() > 0) {
            logger.error("Expected : " + expected.getEdges().size() + " edges but got : "
                + edges.size() + ". Extra edges :");
            for (Map.Entry<String, String> entry : extraEdges.entries()) {
                logger.error("    " + entry.getKey() + " --> " + entry.getValue());
            }
            throw new RuntimeException("Found edges which shouldn't be there : " + extraEdges);
        }
    }

    private static Multimap<String, String> findDifference(Multimap<String, String> a,
        Multimap<String, String> b) {
        final Multimap<String, String> ret = HashMultimap.create(a);
        for (Map.Entry<String, String> entry : b.entries()) {
            ret.remove(entry.getKey(), entry.getValue());
        }
        return ret;
    }

    @Override
    public String toString() {
        final StringBuilder ret = new StringBuilder();

        ret.append(getClass().getCanonicalName() + System.lineSeparator());
        Collections.sort(vertices);
        ret.append("Number of Vertices : " + vertices.size() + System.lineSeparator());
        for (String name : vertices) {
            ret.append("    " + name + System.lineSeparator());
        }
        ret.append("Number of Edges : " + edges.size() + System.lineSeparator());
        for (Map.Entry<String, String> entry : edges.entries()) {
            ret.append("    " + entry.getKey() + " --> " + entry.getValue()
                + System.lineSeparator());
        }
        return ret.toString();
    }
}
