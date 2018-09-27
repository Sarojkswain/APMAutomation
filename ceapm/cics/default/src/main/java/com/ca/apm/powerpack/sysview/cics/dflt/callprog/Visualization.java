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

package com.ca.apm.powerpack.sysview.cics.dflt.callprog;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple class that can be used to generate visualizations of an actions tree.
 */
public class Visualization {
    public static final String INIT_LABEL = "INIT";

    private Map<String, String> nodes = new LinkedHashMap<>();
    private Map<String, List<String>> edges = new LinkedHashMap<>();

    public Visualization(List<Action> actions) {
        String initId = getVertexId(actions, 0);
        addVertex(initId, INIT_LABEL);
        addActions(initId, actions);
    }

    public String getDotGraph() {
        StringBuilder dotGraph = new StringBuilder();

        dotGraph.append("digraph actions {").append(System.lineSeparator());
        dotGraph.append("   node [fontname=Courier,fontsize=10,shape=box,style=filled,"
            + "fillcolor=lightblue];").append(System.lineSeparator());
        dotGraph.append("   edge [arrowsize=0.5, weight=2];").append(System.lineSeparator());
        for (Map.Entry<String, String> node : nodes.entrySet()) {
            dotGraph.append("   \"").append(node.getKey()).append("\"[label=\"")
                .append(node.getValue()).append("\"");
            if (node.getValue().startsWith("Delay")) {
                dotGraph.append(",fillcolor=yellowgreen,shape=octagon");
            } else if (node.getValue().startsWith("Abend")) {
                dotGraph.append(",fillcolor=orangered,shape=ellipse");
            } else if (node.getValue().equals(INIT_LABEL)) {
                dotGraph.append(",fillcolor=dodgerblue,shape=diamond");
            }
            dotGraph.append("];").append(System.lineSeparator());
        }

        for (String from : edges.keySet()) {
            String targets = edges.get(from).stream()
                .map(id -> "\"" + id + "\"")
                .collect(Collectors.joining(","));

            dotGraph.append("   \"").append(from).append("\" -> {").append(targets).append("};")
                .append(System.lineSeparator());
            dotGraph.append("   {rank=same;").append(targets).append("};")
                .append(System.lineSeparator());
        }
        dotGraph.append("}");

        return dotGraph.toString();
    }

    private void addActions(String parentId, List<Action> actions) {
        assert parentId != null;
        assert actions != null;

        for (Action action : actions) {
            for (int i = 0; i < action.getCount(); ++i) {
                String id = getVertexId(action, i);

                addVertex(id, action.toString());
                addEdge(parentId, id);

                if (action.getProgram() != null && action.getSubActions() != null) {
                    final List<Action> subActions = action.getSubActions();

                    addActions(id, subActions);
                }
            }
        }
    }

    private static String getVertexId(Object action, int sequence) {
        assert action != null;

        return Integer.toHexString(action.hashCode()) + "_" + sequence;
    }

    private void addVertex(String id, String label) {
        assert id != null;
        assert label != null;
        assert !nodes.containsKey(id);

        nodes.put(id, label);
    }

    private void addEdge(String fromId, String toId) {
        assert fromId != null;
        assert toId != null;

        if (!edges.containsKey(fromId)) {
            edges.put(fromId, new ArrayList<>());
        }

        assert !edges.get(fromId).contains(toId);

        edges.get(fromId).add(toId);
    }
}
