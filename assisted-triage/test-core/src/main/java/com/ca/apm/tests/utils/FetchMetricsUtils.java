package com.ca.apm.tests.utils;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ca.apm.classes.from.appmap.plugin.DataChunk;
import com.ca.apm.classes.from.appmap.plugin.KESESpecifierConstants;
import com.ca.apm.classes.from.appmap.plugin.MetricValueResponse;
import com.ca.apm.classes.from.appmap.plugin.MetricValues;
import com.ca.apm.classes.from.appmap.plugin.RegularExpressionBuilder;
import com.ca.apm.classes.from.appmap.plugin.Specifier;
import com.ca.apm.classes.from.appmap.plugin.Vertex;
import com.ca.apm.classes.from.appmap.plugin.VertexType;
import com.ca.apm.classes.from.appmap.plugin.Specifier.SpecifierType;
import com.ca.tas.restClient.IRestResponse;
import com.ca.tas.restClient.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FetchMetricsUtils {
    private Common common = new Common();
    private RestClient restClient = new RestClient();

    private static final String VIRTUAL_AGENT =
        "Custom Metric Host (Virtual)|Custom Metric Process (Virtual)|Custom Business Application Agent (Virtual)";
    private static final String APPLICATION_SERVICE = "ApplicationService";

    public final String TOTAL = "total";
    public final String COUNT = "count";

    // Constructs payload to query AppMap Rest API to get metrics
    public String metricRegexPayload(String metricComponent, String type, Long range,
        Timestamp endTime, String momRole) {

        String callType = null;

        if (type.equalsIgnoreCase("frontend")) {
            callType = "By Frontend";
        } else if (type.equalsIgnoreCase("BUSINESSTRANSACTION")) {
            callType = "By Business Service" + "\\\\|PipeOrganApp";
        } else {
            return null;
        }

        String payload =
            "{\"metricQueries\":"
                + "[{\"agentSpecifier\":"
                + "{\"specifier\": \""
                + VIRTUAL_AGENT
                + "\","
                + "\"type\": \"EXACT\"},"
                + "\"metricSpecifier\":"
                + "{\"specifier\": \""
                + callType
                + "\\\\|"
                + RegularExpressionBuilder.convertClearTextToRegExp(metricComponent)
                + "\\\\|[^|]*:Responses Per Interval\","
                + "\"type\": \"REGEX\"},"
                + "\"momFilter\":[\""
                + momRole
                + ".ca.com"
                + ":8081\"]}],\"queryRange\":{\"rangeSize\":"
                + range
                + ","
                + "\"endTime\":\""
                + common.timestamp2String(endTime)
                + "\",\"frequency\":15000 },\"fillGapsFlag\":false,\"uvb\":false,\"aggregate\":false}";

        return payload;
    }

    // Constructs payload to query AppMap Rest API to get metrics
    public String metricPayload(String agentSpecifier, String metricSpecifier,
        String metricCallType, String momRole, Long range, Timestamp lastTimestamp) {
        String specifier;

        if (metricSpecifier.contains("By Frontend")
            || metricSpecifier.contains("By Business Service")) {
            specifier = VIRTUAL_AGENT;
        } else {
            specifier = agentSpecifier;
        }

        String payload =
            "{\"metricQueries\":" + "[{\"agentSpecifier\":" + "{\"specifier\": \"" + specifier
                + "\"," + "\"type\": \"EXACT\"}," + "\"metricSpecifier\":" + "{\"specifier\": \""
                + metricSpecifier + "\"," + "\"type\": \"" + metricCallType + "\"},"
                + "\"momFilter\":[\"" + momRole + ".ca.com"
                + ":8081\"]}],\"queryRange\":{\"rangeSize\":" + range + "," + "\"endTime\":\""
                + common.timestamp2String(lastTimestamp)
                + "\"},\"fillGapsFlag\":false,\"uvb\":false,\"aggregate\":false}";

        return payload;
    }


    public List<MetricValues> fetchMetricsFromPayload(String urlPart, String payload)
        throws IOException {

        // Calls AppMap Rest and metrics data
        EmRestRequest request = new EmRestRequest(urlPart, payload);
        IRestResponse<String> response = restClient.process(request);
        String jsonResponse = response.getContent();

        ObjectMapper mapper = new ObjectMapper();
        MetricValueResponse metricResponse =
            mapper.readValue(jsonResponse, MetricValueResponse.class);
        List<MetricValues> returnMetrics = metricResponse.getMetrics();

        return returnMetrics;
    }

    /*
     * Get aggregated volume for the transaction. Takes json response as input and gives a list with
     * Total Volume and size of the metrics.
     * Input : MetricValues
     * Returns : Map<Double, Integer> = Map<TotalMetricAggregation, Count>
     */
    public Map<Long, Double> getTimeslicedResults(MetricValues values) {
        HashMap<Long, Double> resultMap = new HashMap<Long, Double>();

        long interval = values.getInterval();
        List<DataChunk> metricDataList = values.getDataChunks();

        for (DataChunk data : metricDataList) {

            long firstStopTimeMs = data.getFirstEndTimestamp();
            for (double max : data.getMaxes()) {

                resultMap.put(firstStopTimeMs, max);
                firstStopTimeMs += interval;
            }
        }
        return resultMap;
    }

    public int calculateRatio(Map<Long, Double> actorMap, Map<Long, Double> dbMap) {

        double cumulativeRatio = 0;
        int viableSlices = 0;

        /*
         * Match up actor time slice with db time slice and extract max data
         * from each to do the calculation.
         */
        for (Map.Entry<Long, Double> entry : actorMap.entrySet()) {

            /*
             * Make sure there is a matching time slice in the DB
             * which matches the actor.
             */
            boolean found = dbMap.containsKey(entry.getKey());

            // Reject all time slices which don't have a match actor time slice
            if (found) {

                /*
                 * Count the number of slices which are contained in both
                 * the frontend actor and the backend we are considering.
                 */
                ++viableSlices;

                // Ratio of callee/caller for responses per interval
                double ratio = dbMap.get(entry.getKey()) / actorMap.get(entry.getKey());

                cumulativeRatio += ratio;

            }
        }

        // Average the ratio and return it
        int avgRatio = (int) (cumulativeRatio / (viableSlices == 0 ? 1 : viableSlices));

        return avgRatio;
    }

    /*
     * Method which figures out the metric specifier
     * given an vertexInfo and metricType. Utilized type and pre-known
     * expressions for that type to form it.
     */
    public Specifier getMetricSpecifier(Map<String, String> VertexInfo, String metricType)
        throws Exception {

        // convert metricSpecifier to String
        Specifier metricSpecifier = new Specifier();

        String type = VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_TYPE.getName());
        String name = VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_NAME.getName());

        /*
         * Build a Metric Prefix based on type of Node :
         * 
         * METRIC_SPECIFIER_SOCKET: "Backends|<name>",
         * METRIC_SPECIFIER_DATABASE: "Backends|<name>",)
         * METRIC_SPECIFIER_SERVLET: "Servlets|<servletClassName>",
         * METRIC_SPECIFIER_WEBSERVICE: "WebServices|Client|<wsNamespace>|<wsOperation>",
         * METRC_SPECIFIER_WEBSERVICE_SERVER: "WebServices|Server|<wsNamespace>|<wsOperation>",
         * METRIC_SPECIFIER_BUSINESSTRANSACTION: "By Business Service|<serviceId>|<name>|[^|]*"
         */

        if (VertexType.Type.DATABASE.toString().equalsIgnoreCase(type)
            || VertexType.Type.DATABASE_SOCKET.toString().equalsIgnoreCase(type)
            || VertexType.Type.SOCKET.toString().equalsIgnoreCase(type)) {

            metricSpecifier.setSpecifier("Backends|" + name
                + KESESpecifierConstants.kAttributeSeparatorString + metricType);
            metricSpecifier.setType(SpecifierType.EXACT);
        } else if (VertexType.Type.SERVLET.toString().equalsIgnoreCase(type)) {

            String servlet =
                VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_SERVLET_CLASSNAME.getName());

            String prefix = null;
            if (servlet != null && !servlet.isEmpty()) {
                prefix = "Servlets|" + servlet;
            } else {
                prefix = "Servlets|" + name;
            }
            metricSpecifier.setSpecifier(prefix + KESESpecifierConstants.kAttributeSeparatorString
                + metricType);
            metricSpecifier.setType(SpecifierType.EXACT);
        } else if (VertexType.Type.WEBSERVICE.toString().equalsIgnoreCase(type)) {

            String prefix = null;
            // possible backend webservice.
            String backend =
                VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_BACKEND_NAME.getName());

            if (backend == null || backend.isEmpty()) {

                String namespace =
                    VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_WS_NAMESPACE.getName());
                String operation =
                    VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_WS_OPERATION_NAME.getName());

                prefix = "WebServices|Client|" + namespace + "|" + operation;
            } else {
                prefix = "Backends|" + backend;
            }

            metricSpecifier.setSpecifier(prefix + KESESpecifierConstants.kAttributeSeparatorString
                + metricType);
            metricSpecifier.setType(SpecifierType.EXACT);
        } else if (VertexType.Type.WEBSERVICE_SERVER.toString().equalsIgnoreCase(type)) {

            String namespace =
                VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_WS_NAMESPACE.getName());
            String operation =
                VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_NAME_WS_OPERATION_NAME.getName());

            metricSpecifier.setSpecifier("WebServices|Server|" + namespace + "|" + operation
                + KESESpecifierConstants.kAttributeSeparatorString + metricType);
            metricSpecifier.setType(SpecifierType.EXACT);
        } else if (VertexType.Type.BUSINESSTRANSACTION.toString().equalsIgnoreCase(type)) {

            String serviceId = VertexInfo.get(Vertex.MainAttribute.ATTRIBUTE_SERVICE_ID.getName());

            if (APPLICATION_SERVICE.equalsIgnoreCase(serviceId)) {
                // frontend
                metricSpecifier.setSpecifier("By Frontend|" + name + "|Health"
                    + KESESpecifierConstants.kAttributeSeparatorString + metricType);
                metricSpecifier.setType(SpecifierType.EXACT);
            } else {
                // bt

                metricSpecifier.setSpecifier("By Business Service\\|"
                    + RegularExpressionBuilder.convertClearTextToRegExp(serviceId) + "\\|"
                    + RegularExpressionBuilder.convertClearTextToRegExp(name) + "\\|[^|]*"
                    + KESESpecifierConstants.kAttributeSeparatorString
                    + RegularExpressionBuilder.convertClearTextToRegExp(metricType));
                metricSpecifier.setType(SpecifierType.REGEX);
            }
        } else {

            metricSpecifier.setSpecifier(name + KESESpecifierConstants.kAttributeSeparatorString
                + metricType);
            metricSpecifier.setType(SpecifierType.EXACT);
        }

        return metricSpecifier;
    }

}
