package com.ca.apm.systemtest.alertstateload.testbed;

public interface Constants {

    public static final String ASL_DB_MACHINE_ID = "dbMachine";
    public static final String ASL_MOM_MACHINE_ID = "momMachine";
    public static final String ASL_WV_MACHINE_ID = "wvMachine";
    public static final String ASL_MEMORY_MONITOR_WEBAPP_MACHINE_ID = "memoryMonitorWebappMachine";
    public static final String ASL_TEST_MACHINE_ID = ASL_MEMORY_MONITOR_WEBAPP_MACHINE_ID;

    public static final String ASL_COLL01_MACHINE_ID = "coll01Machine";
    public static final String ASL_COLL02_MACHINE_ID = "coll02Machine";
    public static final String ASL_COLL03_MACHINE_ID = "coll03Machine";
    public static final String ASL_COLL04_MACHINE_ID = "coll04Machine";
    public static final String ASL_COLL05_MACHINE_ID = "coll05Machine";
    public static final String[] ASL_COLL_MACHINES = {
        ASL_COLL01_MACHINE_ID,
        ASL_COLL02_MACHINE_ID,
        ASL_COLL03_MACHINE_ID,
        ASL_COLL04_MACHINE_ID,
        ASL_COLL05_MACHINE_ID
    };

    public static final String ASL_LOAD01_MACHINE_ID = "load01Machine";
    public static final String ASL_LOAD02_MACHINE_ID = "load02Machine";
    public static final String ASL_LOAD03_MACHINE_ID = "load03Machine";
    public static final String ASL_LOAD04_MACHINE_ID = "load04Machine";
    public static final String ASL_LOAD05_MACHINE_ID = "load05Machine";
    public static final String[] ASL_LOAD_MACHINES = {
        ASL_LOAD01_MACHINE_ID,
        ASL_LOAD02_MACHINE_ID,
        ASL_LOAD03_MACHINE_ID,
        ASL_LOAD04_MACHINE_ID,
        ASL_LOAD05_MACHINE_ID
    };

    public static final String ASL_LOAD_ROLE = "loadRole";
    public static final String ASL_TYPEPERFROLE_ROLE = "typeperfRole";

    public static final String ASL_EM_MACHINE_ID = "emMachine"; // used only for data preparation
    public static final String ASL_TOMCAT_MACHINE_ID = "tomcatMachine"; // used only for data preparation
    public static final String ASL_TOMCAT_ROLE_ID = "tomcatRole"; // used only for data preparation

}
