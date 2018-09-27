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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * This is a sample graph implementation just to compare the outputs.
 * When we compare with more granularity, this class should be improved
 *
 * @author bhusu01
 */
public class SampleGraph {

    private Map<String, Vertex> pathToVertexMap = new HashMap<String, Vertex>();
    private Map<String, Vertex> idToVertexMap = new HashMap<String, Vertex>();

    // Contains all edges from the expected map
    private Map<String, List<String>> edges = new HashMap<String, List<String>>();
    // As we match edges from expected map, we transfer the matched edges to this map, so that at
    // the end we can identify the number of identified and unidentified edges
    private Map<String, List<String>> matchedEdges = new HashMap<String, List<String>>();

    private int edgeCount = 0;

    /**
     * Iterates through map to count the edges
     *
     * @param map
     * @return
     */
    public static int countEdges(Map<String, List<String>> map) {
        int totalCount = 0;
        for (List<String> items : map.values()) {
            totalCount += items.size();
        }
        return totalCount;
    }

    /**
     * Loads the expected json graph into a simple graph for comparison
     *
     * @return
     */
    public static SampleGraph initGraph(String jsonGraph) {
        SampleGraph graph = new SampleGraph();
        JsonElement jsonElement = new JsonParser().parse(jsonGraph);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonArray vertices = jsonObject.getAsJsonArray("vertices");
        JsonArray edges = jsonObject.getAsJsonArray("edges");

        // Add all vertices
        for (JsonElement vertex : vertices) {
            String id = vertex.getAsJsonObject().get("vertexId").getAsString();
            String name =
                vertex.getAsJsonObject().get("attributes").getAsJsonObject().get("name").getAsString();
            graph.addVertex(id, name);
        }

        // Add all edges
        for (JsonElement edge : edges) {
            String sourceId = edge.getAsJsonObject().get("sourceId").getAsString(); 
            String targetId = edge.getAsJsonObject().get("targetId").getAsString(); 

            graph.addEdge(sourceId, targetId);
        }
        return graph;
    }

    /**
     * @return the edgeCount
     */
    public int getEdgeCount() {
        return edgeCount;
    }

    public void addVertex(String id, String name) {
        Vertex v = new Vertex();
        v.id = id;
        v.name = name;

        pathToVertexMap.put(name, v);
        idToVertexMap.put(id, v);
    }

    public Map<String, List<String>> getEdges() {
        return edges;
    }

    public Vertex getVertex(String id) {
        return idToVertexMap.get(id);
    }

    /**
     * Returns ID of vertex if match is found
     *
     * @param res
     * @return -1 if no match is found
     */
    public String matchVertex(String res) {
        if (pathToVertexMap.containsKey(res)) {
            return pathToVertexMap.get(res).id;
        }

        // These are some custom rules to match the resource names formed by app map tracers with
        // the names output by ATC
        int lastIndexOfSlash = res.lastIndexOf('/') + 1;

        if (lastIndexOfSlash > 0 && pathToVertexMap.containsKey(res.substring(lastIndexOfSlash))) {
            return pathToVertexMap.get(res.substring(lastIndexOfSlash)).id;
        }

        return null;
    }

    public void addEdge(String head, String tail) {
        List<String> tails = edges.get(head);
        if (tails == null) {
            tails = new ArrayList<String>();
            edges.put(head, tails);
        }
        if (!tails.contains(tail)) {
            tails.add(tail);
        }
        ++edgeCount;
    }

    public boolean matchEdge(String head, String tail) {
        // Check for edges that were already mapped
        if (matchedEdges.containsKey(head)) {
            List<String> tails = matchedEdges.get(head);
            if (tails.contains(tail)) {
                return true;
            }
        }
        if (edges.containsKey(head)) {
            List<String> tails = edges.get(head);
            // Remove from the original and add to the matched edges
            if (tails.contains(tail)) {
                tails.remove(tail);
                List<String> matchedTails = matchedEdges.get(head);
                if (matchedTails == null) {
                    matchedTails = new ArrayList<String>();
                    matchedEdges.put(head, matchedTails);
                }
                matchedTails.add(tail);
                return true;
            }
        }

        return false;
    }

    public int matchedEdgeCount() {
        return countEdges(matchedEdges);
    }

    public int unMatchedEdgeCount() {
        return countEdges(edges);
    }


    // For verification, we identify each edge by its name
    public class Vertex {

        String id;
        String name;

        public String getName() {
            return name;
        }
    }
}


