package com.ca.apm.systemtest.fld.plugin.vo;

public class DashboardIdStore {

	public static final String DASHBOARD_VARIABLE = "DASHBOARD_ID";

	private static ThreadLocal<Long> dashboardId = new ThreadLocal<Long>();

	public static Long getDashboardId() {
		return dashboardId.get();
	}

	public static void setDashboardId(Long newDashboardId) {
		dashboardId.set(newDashboardId);
	}

	public static void clearDashboardId() {
		dashboardId.remove();
	}
}
