package com.ca.apm.systemtest.sizingguidetest.testbed;

public interface Constants {

    public static final String DB_MACHINE_ID = "dbMachine";
    public static final String TIM01_MACHINE_ID = "tim01Machine";
    public static final String TIM02_MACHINE_ID = "tim02Machine";
    public static final String[] TIM_MACHINES = new String[] {
                                                              TIM01_MACHINE_ID,
                                                              TIM02_MACHINE_ID
                                                             };
    public static final String EM_MACHINE_ID = "emMachine";
    public static final String WV_MACHINE_ID = "wvMachine";
    public static final String TOMCAT01_MACHINE_ID = "tomcat01Machine";
    public static final String TOMCAT02_MACHINE_ID = "tomcat02Machine";
    public static final String[] TOMCAT_MACHINES = new String[] {
                                                                 TOMCAT01_MACHINE_ID,
                                                                 TOMCAT02_MACHINE_ID
                                                                };
    public static final String MEMORY_MONITOR_WEBAPP_MACHINE_ID = "memoryMonitorWebappMachine";
    public static final String TEST_MACHINE_ID = "testMachine";

    public static final String DB_ROLE = "dbRole";
    public static final String TIM01_ROLE = "tim01role";
    public static final String TIM02_ROLE = "tim02role";
    public static final String EM_ROLE = "emRole";
    public static final String WV_ROLE = "wvRole";
    public static final String JAVA_DBMACHINE_ROLE = "javaDbMachineRole";
    public static final String IMPORT_DOMAIN_CONFIG_ROLE = "dbImportDomainConfigRole";
    public static final String TOMCAT01_ROLE = "tomcat01Role";
    public static final String TOMCAT02_ROLE = "tomcat02Role";
    public static final String CONFIG_TESS_ROLE = "configTessRole";
    public static final String TIM01_ROLE_CONFIGURE_ETH02 = "tim01role_configureEth02";
    public static final String TIM02_ROLE_CONFIGURE_ETH02 = "tim02role_configureEth02";
    public static final String CEM_TESS_LOAD01_ROLE_ID = "cemTessLoad01Role";
    public static final String CEM_TESS_LOAD02_ROLE_ID = "cemTessLoad02Role";
    public static final String PORTFORWARD_CEM_TESS_LOAD01_ROLE_ID = "portforward_cemTessLoad01Role";
    public static final String PORTFORWARD_CEM_TESS_LOAD02_ROLE_ID = "portforward_cemTessLoad02Role";
    public static final String METRIC_SYNTH_ROLE = "metricSynthRole";
    public static final String EM_MACHINE_TYPEPERFROLE_ROLE = "emMachine_typeperfRole";
    public static final String WV_MACHINE_TYPEPERFROLE_ROLE = "wvMachine_typeperfRole";

}
