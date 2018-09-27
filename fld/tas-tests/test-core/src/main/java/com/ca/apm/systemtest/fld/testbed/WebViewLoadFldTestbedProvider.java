package com.ca.apm.systemtest.fld.testbed;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ca.apm.systemtest.fld.role.loads.WebViewLoadRole;
import com.ca.apm.systemtest.fld.role.loads.WebViewLoadRole.Builder;
import com.ca.tas.artifact.thirdParty.JavaBinary;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.testbed.Bitness;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedMachine;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.type.Platform;

public class WebViewLoadFldTestbedProvider implements FldTestbedProvider, FLDLoadConstants, FLDConstants {

    public static final JavaBinary JAVA_VERSION = JavaBinary.WINDOWS_64BIT_JDK_17;

    private ITestbedMachine[] wvLoadMachines;
    private Map<String, Map<String, Collection<String>>> urlMachineMap;
    
    /**
     * Standard no-args constructor.
     */
    public WebViewLoadFldTestbedProvider() {
        urlMachineMap = new HashMap<String, Map<String,Collection<String>>>();
        
        Map<String, Collection<String>> map = new HashMap<>();
        map.put("ff", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_1;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_2;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_3;tr=0",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_11%257CFakeMetricSet3%257CFakeServlet1%253AAverage+Response+Time+(ms)",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_14%257CBackends%257CMySqlDatabase_1%253AResponses+Per+Interval"
            ));
        //FIXME - should be IE, but the selenium driver for IE is horribly broken
        // switch to "ie" when it gets fixed
        map.put("chrome", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_4;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_17;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_18;tr=0",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_16%253AEM+Host",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_19%257CHeuristics%257CApps%257CNew_Apps_1%253AUser"
            ));
        urlMachineMap.put(WEBVIEW_LOAD_01_MACHINE_ID, map);
        
        map = new HashMap<>();
        map.put("ff", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_5;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_6;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_7;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_8;tr=0",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_27%257CFrontends%257CApps%257CNew_Apps_1%257CURLs%257CServlet_1%253AAverage+Response+Time+(ms)"
            ));
        //FIXME - should be IE, but the selenium driver for IE is horribly broken
        // switch to "ie" when it gets fixed
        map.put("chrome", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_28%257CFrontends%257CApps%257CNew_Apps_1%253AAverage+Response+Time+(ms)",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_16;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_17;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_18;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_19;tr=0"
            ));
        urlMachineMap.put(WEBVIEW_LOAD_02_MACHINE_ID, map);
        
        
        map = new HashMap<>();
        map.put("ff", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_9;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_10;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_11;tr=0",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_8%257CAgent+Stats%257CSustainability%257CCPU+Stats%253AAgent+Time+(ns)",
            "http://WEBVIEW_HOST_NAME/#management;cn=Sample_17_70_Dashboards;ct=8;dn=SuperDomain;mm=Clean_BofA_MM_17;tr=0"
            ));
        map.put("chrome", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#agentAdm;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_12;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_20;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_21;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_22;tr=0"
            ));
        urlMachineMap.put(WEBVIEW_LOAD_03_MACHINE_ID, map);

        
        map = new HashMap<>();
        //FIXME - should be IE, but the selenium driver for IE is horribly broken
        // switch to "ie" when it gets fixed
        map.put("ff", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_13;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_14;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_15;tr=0",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_7%257CAgent+Stats%257CSustainability%257CMemory+Stats%253ASocketMapCache",
            "http://WEBVIEW_HOST_NAME/#console;db=Dashboard;mm=martins;tr=0"
            ));
        map.put("chrome", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#home;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_24;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_25;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_26;tr=0",
            "http://WEBVIEW_HOST_NAME/#investigator;tab-in=mb;tab-tv=pd;tr=0;uid=SuperDomain%257Cwurlitzer-stress-app%257CStressTestProcess%257CWurlitzerAgent_1%257CAgent+Stats%257CSustainability%257CResponse+Time+Stats%253AAgent+Time+(ns)"
            ));
        urlMachineMap.put(WEBVIEW_LOAD_04_MACHINE_ID, map);


        map = new HashMap<>();
        map.put("ff", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_19;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts;dn=SuperDomain;mm=Clean_BofA_MM_20;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_23;tr=0",
            "http://WEBVIEW_HOST_NAME/#management;cn=Fake_Metrics_1_10_metrics;ct=96;dn=*SuperDomain*;mm=Clean_BofA_MM_24;tr=0"
            ));
        map.put("chrome", Arrays.asList(
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_28;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_29;tr=0",
            "http://WEBVIEW_HOST_NAME/#console;db=S3_Perf_Test_25_Metrics_Per_Agent_Total_500_Metrics_20_Charts_No_Legend;dn=SuperDomain;mm=Clean_BofA_MM_30;tr=0",
            "http://WEBVIEW_HOST_NAME/#management;cn=Fake_Metrics_5_5_metrics;ct=96;dn=SuperDomain;mm=Clean_BofA_MM_24;tr=0"
            ));
        urlMachineMap.put(WEBVIEW_LOAD_05_MACHINE_ID, map);

    }
    
    @Override
    public Collection<ITestbedMachine> initMachines() {
        wvLoadMachines = new TestbedMachine[WEBVIEW_LOAD_MACHINE_IDS.length];
        for (int i = 0; i < WEBVIEW_LOAD_MACHINE_IDS.length; i++) {
            String machineId = WEBVIEW_LOAD_MACHINE_IDS[i];
            wvLoadMachines[i] = new TestbedMachine.Builder(machineId).platform(Platform.WINDOWS)
                    .templateId(ITestbedMachine.TEMPLATE_W64).bitness(Bitness.b64).build();
        }
        
        return Arrays.asList(wvLoadMachines);
    }
    
    
    @Override
    public void initTestbed(ITestbed testbed, ITasResolver tasResolver) {
        
        for (int i = 0; i < WEBVIEW_LOAD_MACHINE_IDS.length; i++) {
            String machineId = WEBVIEW_LOAD_MACHINE_IDS[i];
            ITestbedMachine machine = wvLoadMachines[i];

            String webviewServerHost = tasResolver.getHostnameById(EM_WEBVIEW_ROLE_ID);

            Builder wvBuilder = new WebViewLoadRole.Builder("webViewLoadRole_" + machineId, tasResolver)
                    .webViewServerHost(webviewServerHost);
            
            String hostAndPort = webviewServerHost + ":8080"; 
            Map<String, Collection<String>> map = urlMachineMap.get(machineId);
            for (Entry<String, Collection<String>> entry: map.entrySet()) {
                String browser = entry.getKey();
                for (String url: entry.getValue()) {
                    url = url.replace("WEBVIEW_HOST_NAME", hostAndPort);
                    wvBuilder.openWebViewUrl(browser, url);
                }
            }
                    
            WebViewLoadRole webViewLoadRole = wvBuilder.build(); 

            machine.addRole(webViewLoadRole);
        }
    }

}
