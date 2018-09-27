package com.ca.apm.systemtest.fld.util.networktrafficmonitor;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkTrafficMonitorRunner {

    private static final String DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_HOST;
    private static final int DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_PORT = 8080;
    private static final String DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_CONTEXT_ROOT =
        "network-traffic-monitor"; // "network-traffic-monitor-webapp-99.99.aquarius-SNAPSHOT";

    private static final int DEFAULT_CHART_WIDTH = 1600;
    private static final int DEFAULT_CHART_HEIGHT = 640;

    private String networkTrafficMonitorWebappHost = DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_HOST;
    private int networkTrafficMonitorWebappPort = DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_PORT;
    private String networkTrafficMonitorWebappContextRoot =
        DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_CONTEXT_ROOT;

    private int chartWidth = DEFAULT_CHART_WIDTH;
    private int chartHeight = DEFAULT_CHART_HEIGHT;

    static {
        String defaultNetworkTrafficMonitorWebappHost;
        try {
            defaultNetworkTrafficMonitorWebappHost = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            defaultNetworkTrafficMonitorWebappHost = "localhost";
        }
        DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_HOST = defaultNetworkTrafficMonitorWebappHost;
    }

    public NetworkTrafficMonitorRunner(String networkTrafficMonitorWebappHost,
        Integer networkTrafficMonitorWebappPort, String networkTrafficMonitorWebappContextRoot,
        Integer chartWidth, Integer chartHeight) {
        this.networkTrafficMonitorWebappHost = networkTrafficMonitorWebappHost;
        this.networkTrafficMonitorWebappPort =
            networkTrafficMonitorWebappPort == null
                ? DEFAULT_NETWORK_TRAFFIC_MONITOR_WEBAPP_PORT
                : networkTrafficMonitorWebappPort;
        this.networkTrafficMonitorWebappContextRoot = networkTrafficMonitorWebappContextRoot;
        this.chartHeight = chartHeight == null ? DEFAULT_CHART_HEIGHT : chartHeight;
        this.chartWidth = chartWidth == null ? DEFAULT_CHART_WIDTH : chartWidth;
    }

    public void createChart() {
        // TODO Auto-generated method stub
        System.out.println("NetworkTrafficMonitorRunner.createChart():: XXXXXXXXXX ");
    }

}
