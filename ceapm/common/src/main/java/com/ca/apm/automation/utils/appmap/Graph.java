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

package com.ca.apm.automation.utils.appmap;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Graph {
    public static final String WEBVIEW_PATH = "/apm/appmap/private/graph";

    private static final Logger logger = LoggerFactory.getLogger(Graph.class);

    protected static final String M_VERTICES = "vertices";
    protected static final String M_EDGES = "edges";

    Map<String, Vertex> vertices = new HashMap<String, Vertex>();
    List<Edge> edges = new ArrayList<Edge>();

    /**
     * Parses the data found at
     * <em-server>/apm/appmap/private/graph and generates a {@link Graph} instance.
     *
     * @param source HTML source to parse.
     * @return Graph instance or {@link null} if parsing failed.
     */
    public static Graph fromWebviewHtmlSource(String source) {
        DocumentBuilder builder;
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document html = builder.parse(new InputSource(new StringReader(source)));
            NodeList pres = html.getElementsByTagName("pre");
            if (pres.getLength() != 1) {
                logger.error("Unexpected html source data (pres)");
                return null;
            }

            Node pre = pres.item(0);
            if (!pre.getNodeName().equals("pre") || pre.getNodeType() != Node.ELEMENT_NODE) {
                logger.error("Unexpected html source data (pre, nodetype)");
                return null;
            }

            return new Graph(pre.getTextContent());
        } catch (ParserConfigurationException | SAXException | IOException e) {
            logger.error("Caught exception while parsing JSON source for AppMap graph: "
                + e.getMessage());
            return null;
        }
    }

    public Graph(String json) {
        JsonElement jsonElement = new JsonParser().parse(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // At the top level there are two arrays, one for vertices and one for edges
        // Process Vertices
        for (JsonElement jVertex : jsonObject.getAsJsonArray(M_VERTICES)) {
            assert jVertex.isJsonObject();

            Vertex vertex = Vertex.fromJsonObject(jVertex.getAsJsonObject());
            vertices.put(vertex.getId(), vertex);
        }

        // Process Edges
        for (JsonElement jEdge : jsonObject.getAsJsonArray(M_EDGES)) {
            assert jEdge.isJsonObject();

            Edge edge = Edge.fromJsonObject(jEdge.getAsJsonObject(), vertices);
            edges.add(edge);
        }

        if (logger.isDebugEnabled()) {
            for (Edge edge : edges) {
                logger.debug(edge.toString());
            }
        }
    }

    public Collection<Vertex> getAllVertices() {
        return vertices.values();
    }

    public Collection<Vertex> getVerticesMatching(Map<String, Pattern> attributeFilters) {
        List<Vertex> matched = new ArrayList<Vertex>();

        for (Vertex vertex : vertices.values()) {
            if (vertex.matches(attributeFilters)) {
                matched.add(vertex);
            }
        }

        return matched;
    }
}
