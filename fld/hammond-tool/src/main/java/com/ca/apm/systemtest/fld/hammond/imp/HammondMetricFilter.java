package com.ca.apm.systemtest.fld.hammond.imp;

import com.ca.apm.systemtest.fld.hammond.Filter;
import com.ca.apm.systemtest.fld.hammond.MetricNameFilter;
import com.ca.apm.systemtest.fld.hammond.SimpleFilter;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.metric.AgentMetricPrefix;
import com.wily.introscope.util.Log;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class HammondMetricFilter {
    /*
     * Hardcoded for now until we can figure out how to reliably detect them as a
     * "calculator metric"
     */
    private static String[] excludeNames = {};

    private static List<Filter> filters = new ArrayList<>();
    private static ConcurrentHashMap<AgentMetric, Boolean> filteredCache = new ConcurrentHashMap<>();

    static {
        filters.add(new SimpleFilter(".*Frontends\\|Apps\\|.*", 3));
        // filters.add(new
        // SimpleFilter(".*Frontends\\|Apps\\|MetroClient\\|URLs\\|Default\\|Called Backends\\|WebServices.*"));

        filters.add(new MetricNameFilter(
                ".*Frontends\\|Apps\\|.*\\|URLs\\|.*\\|Called Backends\\|WebServices.*", null,
                new String[] {"Average Response Time (ms)", "Errors Per Interval",
                        "Responses Per Interval", "Stall Count"}, true));

        filters.add(new MetricNameFilter(
                ".*Frontends\\|Apps\\|MetroClient\\|URLs\\|.*\\|Called Backends\\|WebServices.*",
                null, new String[] {"Average Response Time (ms)", "Errors Per Interval",
                "Responses Per Interval", "Stall Count"}, true));

        filters.add(new MetricNameFilter(
                "Frontends\\|Apps\\|(BPELServerManagerBean|SLSBContainerBean|SyncDispatcherBean)\\|Called Backends\\|WebServices.*",
                5, new String[] {"Average Response Time (ms)", "Errors Per Interval",
                "Responses Per Interval", "Stall Count"}, true));

        filters.add(new MetricNameFilter(".*Business Service\\|.*", null, new String[] {
                "Defects Per Interval", "Total Performance Defective Transactions Per Interval",
                "Average Response Time (ms)", "Total Transactions Per Interval",
                "Total Defects Per Interval", "Defect %", "Total Defect Ratio (%)",
                "Total RT 95th Percentile Transactions Per Interval", "Total Availability Defective Transactions Per Interval",
                "Response Time 95th Percentile (ms)"}, true));

        filters.add(new MetricNameFilter(".*Agent Stats\\|Resources.*", 2, new String[] {
                "% CPU Utilization (Host)", "% Time Spent in GC", "Threads in Use"}, true));

        filters.add(new MetricNameFilter(".*GC Monitor.*", 1, new String[] {"GC Policy",
                "Jvm Type", "Percentage of Java Heap Used"}, false));
        filters.add(new MetricNameFilter(".*GC Heap.*", 1, new String[] {"Bytes In Use",
                "Bytes Total"}, false));

        filters.add(new MetricNameFilter(".*GC Monitor\\|Garbage Collectors\\|.*", 3, new String[] {
                "GC Algorithm", "GC Invocation Total Count", "Total GC Time (ms)"}, false));

        filters.add(new MetricNameFilter(".*GC Monitor\\|Memory Pools\\|.*", 3, new String[] {
                "Amount of Space Used (bytes)", "Current Capacity (bytes)",
                "Maximum Capacity (bytes)", "Memory Type",
                "Percentage of Maximum Capacity Currently Used"}, false));

        filters.add(new MetricNameFilter(".*CEM\\|.*", null, new String[] {"Defects Per Interval",
                "Total Performance Defective Transactions Per Interval", "Average Response Time (ms)",
                "Total Transactions Per Interval", "Total Defects Per Interval", "Defect %", "Total Defect Ratio (%)",
                "Total RT 95th Percentile Transactions Per Interval", "Total Availability Defective Transactions Per Interval",
                "Response Time 95th Percentile (ms)"}, true));

        // filters.add(new SimpleFilter(".*GC Monitor\\|Garbage Collectors\\|.*", 3));
        // filters.add(new SimpleFilter(".*GC Monitor\\|Memory Pools\\|.*", 3));
        // filters.add(new SimpleFilter(".*GC Monitor:.*", 1));
    }

    public static void importMetricNameFileters(Path path) {
        if (path == null) {
            return;
        }

        try (Stream<String> stream = Files.lines(path)) {
            stream
                .filter(line -> StringUtils.isNotBlank(line))
                .forEach(line -> filters.add(new SimpleFilter(line)));

        } catch (IOException e) {
            Log.out.error("Cannot import metric filters", e);
        }
    }

    public static boolean filterMetric(AgentMetric agentMetric) {

        Boolean filtered = filteredCache.get(agentMetric);
        if (filtered != null) {
            return  filteredCache.get(agentMetric);
        }

        for (String s : excludeNames) {
            if (s.equals(agentMetric.toString())) {
                filteredCache.put(agentMetric, Boolean.TRUE);
                return true;
            }
        }

        AgentMetricPrefix prefix = agentMetric.getAgentMetricPrefix();
        if (prefix.getSegmentCount() > 0) {
            String firstSegment = agentMetric.getAgentMetricPrefix().getSegment(0);
            if (firstSegment.equals("Enterprise Manager") || firstSegment.equals("Variance")
                    || firstSegment.equals("Alerts")) {
                filteredCache.put(agentMetric, Boolean.TRUE);
                return true;
            }
        }

        // if (agentMetric.toString().startsWith("Frontends")) {
        // out.println(agentMetric.toString());
        // out.flush();
        // }


        for (Filter f : filters) {
            if (f.metricIsExcluded(agentMetric)) {
                filteredCache.put(agentMetric, Boolean.TRUE);
                return true;
            }
        }

        filteredCache.put(agentMetric, Boolean.FALSE);
        return false;
    }
}
