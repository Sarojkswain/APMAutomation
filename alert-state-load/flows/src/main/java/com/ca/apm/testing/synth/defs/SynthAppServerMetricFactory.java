package com.ca.apm.testing.synth.defs;

import java.util.ArrayList;
import java.util.List;

import com.ca.apm.testing.metricsynth.DefaultMetricDataFactory;
import com.ca.apm.testing.metricsynth.MetricFactory;
import com.ca.apm.testing.synth.defs.metrics.IntAverageMetricDefinition;
import com.ca.apm.testing.synth.defs.metrics.MetricConstants;
import com.ca.apm.testing.synth.defs.metrics.MetricDefinition;
import com.ca.apm.testing.synth.defs.metrics.MetricGroup;
import com.ca.apm.testing.synth.defs.metrics.PerIntervalCounterMetricDefinition;
import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.BadlyFormedNameException;

public class SynthAppServerMetricFactory extends DefaultMetricDataFactory implements MetricFactory, MetricConstants {
    private List<String> warNames;
    private List<String> ejbNames;
    private MetricGroup ejbMetricGroup;
    private MetricGroup frontendMetricGroup;
    private MetricGroup backendMetricGroup;
    private MetricGroup jsfMetricGroup;
    private List<WebFrontend> webFrontends;
    private String prefix = "";
    private static int nextId = 10000;
    

    public SynthAppServerMetricFactory(String domain, String hostname, String processName, String agentName, 
                String warBaseName, int numWars, String ejbBaseName, int numEjbs, List<WebFrontend> webFrontends) {
        this.webFrontends = webFrontends;
        if (domain == null) {
            domain = "SuperDomain";
        }
        if (hostname == null) {
            hostname = "synth-host";
        }
        if (processName == null) {
            processName = String.format("AppServer%06d", nextId++);
        }
        if (agentName == null) {
            agentName = String.format("Agent-%06d", nextId++);
        }
        prefix = domain + "|" + hostname + "|" + processName + "|" + agentName + "|";
        
        
        
        warNames = new ArrayList<>();
        for (int i = 0; i < numWars; i++) {
            String warName = warBaseName + i;
            warNames.add(warName);
        }
        
        ejbNames = new ArrayList<>();
        for (int i = 0; i < numEjbs; i++) {
            String ejbName = ejbBaseName + i;
            ejbNames.add(ejbName);
        }
        
        setupEjbMetricGroup();
        setupFrontendMetricGroup();
        setupJsfMetricGroup();
        setupWebServiceBackends();
        
    }
    
    
    private void setupEjbMetricGroup() {
        ejbMetricGroup = new MetricGroup("EJB");
        MetricGroup sessionMG = new MetricGroup("Session");
        ejbMetricGroup.addGroup(sessionMG);
        new IntAverageMetricDefinition(prefix, AVG_METHOD_INV_TIME, sessionMG, 5, 15);
        new PerIntervalCounterMetricDefinition(prefix, METHOD_INV_PER_INT, sessionMG, 10, 30);
        for (String ejbName: ejbNames) {
            MetricGroup mg = new MetricGroup(ejbName, sessionMG);
            addBasicFive(mg);
        }
        
    }

    private void setupFrontendMetricGroup() {
        frontendMetricGroup = new MetricGroup("Frontends");
        MetricGroup appsMetricGroup = new MetricGroup("Apps");
        frontendMetricGroup.addGroup(appsMetricGroup);
        for (String warName: warNames) {
            MetricGroup warMetricGroup = new MetricGroup(warName);
            appsMetricGroup.addGroup(warMetricGroup);
            MetricGroup urlsMetricGroup = new MetricGroup("URLs", warMetricGroup);
            MetricGroup defaultUrlMetricGroup = new MetricGroup("Default", urlsMetricGroup);
            addBasicFive(defaultUrlMetricGroup);
            addHighProfile(defaultUrlMetricGroup);
            for (int i = 0; i < 20; i++) {
                MetricGroup urlMetricGroup = new MetricGroup("URL" + i, urlsMetricGroup);
                addBasicFive(urlMetricGroup);
                addHighProfile(urlMetricGroup);
            }
            MetricGroup calledBackendsMG = new MetricGroup("Called Backends", defaultUrlMetricGroup);
            MetricGroup ws1MetricGroup = new MetricGroup("WebService at http_//tas-czfld-n25_8080", calledBackendsMG);
            MetricGroup ws2MetricGroup = new MetricGroup("WebServices", calledBackendsMG);
            addBasicFour(ws1MetricGroup);
            addBasicFour(ws2MetricGroup);
        }
    }

    /**
     * Adds a "high" profile to the AVG_RESP_TIME metric
     * @param mg
     */
    private void addHighProfile(MetricGroup mg) {
        for (MetricDefinition md: mg.getMetrics()) {
            if (md instanceof IntAverageMetricDefinition) {
                IntAverageMetricDefinition iad = (IntAverageMetricDefinition) md;
                iad.addRange(HIGH_PROFILE, 100, 150);
            }
        }
    }


    private void setupJsfMetricGroup() {
        jsfMetricGroup = new MetricGroup("JSF");
        setupJsfApplications();
        setupJsfEvent();
        setupJsfLifecycle();
    }
    
    
    private void setupJsfLifecycle() {
        MetricGroup lifecycleMG = new MetricGroup("Lifecycle", jsfMetricGroup);
        MetricGroup lifecycleImplMG = new MetricGroup("LifecycleImpl", lifecycleMG);
        addBasicTwo(lifecycleImplMG);
        addViewsToHandler(lifecycleImplMG, "execute", "render");
    }


    private void setupJsfEvent() {
        MetricGroup eventMG = new MetricGroup("Event", jsfMetricGroup);
        MetricGroup actionListenerMG = new MetricGroup("ActionListenerImpl", eventMG);
        addBasicTwo(actionListenerMG);
        addViewsToHandler(actionListenerMG, "processAction");
        
        MetricGroup elResolverMG = new MetricGroup("ELResolverInitPhaseListener", eventMG);
        addBasicTwo(elResolverMG);
        addViewsToHandler(elResolverMG, "afterPhase", "beforePhase");
        
        MetricGroup weldPhaseListenerMG = new MetricGroup("WeldPhaseListener", eventMG);
        addBasicTwo(weldPhaseListenerMG);
        addViewsToHandler(weldPhaseListenerMG, "afterPhase", "beforePhase");
    }
    
    
    
    private void setupJsfApplications() {
        MetricGroup applicationMG = new MetricGroup("Application", jsfMetricGroup);
        MetricGroup converstationMG = new MetricGroup("ConversationAwarViewHandler", applicationMG);
        addBasicTwo(converstationMG);
        addViewsToHandler(converstationMG, "createView", "renderView", "restoreView");
        
        MetricGroup multiViewMG = new MetricGroup("MultiViewHandler", applicationMG);
        addBasicTwo(multiViewMG);
        addViewsToHandler(multiViewMG, "createView", "renderView", "restoreView");

        MetricGroup stateManagerMG = new MetricGroup("StateManagerImpl", applicationMG);
        addBasicTwo(stateManagerMG);
        addViewsToHandler(stateManagerMG, "restoreView", "saveView", "writeState");
    }
    

    private void addViewsToHandler(MetricGroup mg, String... views) {
        for (String view: views) {
            MetricGroup viewMG = new MetricGroup(view, mg);
            addBasicFive(viewMG);
        }
    }

    private void setupWebServiceBackends() {
        backendMetricGroup = new MetricGroup("Backends");
        for (WebFrontend wf: webFrontends) {
            // http_//tas-czfld-n25_8080:A
            String name = "WebService at http_" + wf.getHost() + "_" + wf.getPort();
            MetricGroup mg = new MetricGroup(name, backendMetricGroup);
            addBasicFive(mg);
            MetricGroup pathsMG = new MetricGroup("Paths", mg);
            MetricGroup defaultMG = new MetricGroup("Default", pathsMG);
            addBasicFive(defaultMG);
        }
    }

    
    private void addBasicFive(MetricGroup mg) {
        new IntAverageMetricDefinition(prefix, AVG_RESP_TIME, mg, 3, 7);
        new PerIntervalCounterMetricDefinition(prefix, CONCURRENT_INVOCATIONS, mg, 2, 5);
        new PerIntervalCounterMetricDefinition(prefix, ERRORS_PER_INTERVAL, mg, 0);
        new PerIntervalCounterMetricDefinition(prefix, RESPONSES_PER_INTERVAL, mg, 2, 5);
        new PerIntervalCounterMetricDefinition(prefix, STALL_COUNT, mg, 0);
    }
    
    private void addBasicFour(MetricGroup mg) {
        new IntAverageMetricDefinition(prefix, AVG_RESP_TIME, mg, 3, 7);
        new PerIntervalCounterMetricDefinition(prefix, ERRORS_PER_INTERVAL, mg, 0);
        new PerIntervalCounterMetricDefinition(prefix, RESPONSES_PER_INTERVAL, mg, 2, 5);
        new PerIntervalCounterMetricDefinition(prefix, STALL_COUNT, mg, 0);
    }
    
    
    private void addBasicTwo(MetricGroup mg) {
        new IntAverageMetricDefinition(prefix, AVG_RESP_TIME, mg, 3, 7);
        new PerIntervalCounterMetricDefinition(prefix, RESPONSES_PER_INTERVAL, mg, 2, 5);
    }
    
    
    
    
    @Override
    public AgentMetricData[] generateMetricData() throws BadlyFormedNameException {
        List<AgentMetricData> list = new ArrayList<>();
        backendMetricGroup.generateAgentMetrics(list);
        ejbMetricGroup.generateAgentMetrics(list);
        frontendMetricGroup.generateAgentMetrics(list);
        jsfMetricGroup.generateAgentMetrics(list);
        
        return list.toArray(new AgentMetricData[0]);
    }

    
    @Override
    public void setActiveProfile(String profileName) {
        backendMetricGroup.setActiveProfile(profileName);
        ejbMetricGroup.setActiveProfile(profileName);
        frontendMetricGroup.setActiveProfile(profileName);
        jsfMetricGroup.setActiveProfile(profileName);
    }
}
