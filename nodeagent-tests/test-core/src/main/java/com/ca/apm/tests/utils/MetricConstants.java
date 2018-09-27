package com.ca.apm.tests.utils;

public class MetricConstants {

	public static final String METRIC_NODE_DELIMETER = "|";
	public static final String METRIC_NAME_DELIMETER = ":";

	public static final class BlameMetricType {
		public static final String ART = "Average Response Time (ms)";
		public static final String CI = "Concurrent Invocations";
		public static final String EPI = "Errors Per Interval";
		public static final String RPI = "Responses Per Interval";
		public static final String SC = "Stall Count";
	}

}
