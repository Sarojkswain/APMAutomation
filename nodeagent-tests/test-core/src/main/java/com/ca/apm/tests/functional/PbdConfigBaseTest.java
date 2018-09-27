package com.ca.apm.tests.functional;

import static org.testng.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;

import com.ca.apm.automation.common.Util;
import com.ca.apm.automation.common.mockem.ExpectedTraceElement;
import com.ca.apm.automation.common.mockem.ITraceElement;
import com.ca.apm.automation.common.mockem.RequestProcessor;
import com.ca.apm.automation.common.mockem.SIUtils;
import com.ca.apm.automation.common.mockem.TraceCompareUtil;
import com.ca.apm.tests.common.file.FileUtils;
import com.ca.apm.tests.utils.CommonUtils;
import com.ca.apm.tests.utils.HttpTxnGen;
import com.ca.apm.tests.utils.HttpTxnGen.HttpTxnGenBuilder.HttpRequestMethod;
import com.ca.apm.tests.utils.MetricAssertionData;
import com.ca.apm.tests.utils.TraceValidationData;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.ParameterValueTransactionTraceFilter;
import com.wily.introscope.spec.server.transactiontrace.TransactionComponentData;

public class PbdConfigBaseTest extends BaseNodeAgentTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(PbdConfigBaseTest.class);

	public void setup() {
		File f = new File(umAgentConfig.getTogglesPbdPath());

		if (!f.exists()) {
			startCollectorAgent();
			CommonUtils.waitForFile(f);
			stopCollectorAgent();
		}
	}
	
	@DataProvider
	protected Object[][] togglesTestData() {
        return new Object[][] { 
                { "NodeHttpFrontendTracing", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Frontends\\|Apps\\|server\\|URLs\\|Default:Responses Per Interval", -1, TTEvent.FRONTEND.trace, 0, "false" }, 
                { "NodeHttpFrontendTracing", true, "/api/Items/1", HttpRequestMethod.GET, 1, "Frontends\\|Apps\\|server\\|URLs\\|Default:Responses Per Interval", 1, TTEvent.FRONTEND.trace, 1, "false" },
                { "NodeSqlTracing", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Backends\\|nodetix on localhost-3306 \\(MySQL DB\\).*:Responses Per Interval", -1, TTEvent.FRONTEND_NOSQL.trace, 1, "false" }, 
                { "NodeSqlTracing", true, "/api/Items/1", HttpRequestMethod.GET, 1, "Backends\\|nodetix on localhost-3306 \\(MySQL DB\\).*:Responses Per Interval", 1, TTEvent.FRONTEND.trace, 1, "false" },                
                { "ExpressTracing", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Express\\|/_id\\|GET:Responses Per Interval", -1, TTEvent.FRONTEND_NOEXPRESS.trace, 1, "false" }, 
                { "ExpressTracing", true, "/api/Items/1", HttpRequestMethod.GET, 1, "Express\\|/_id\\|GET:Responses Per Interval", 1, TTEvent.FRONTEND.trace, 1, "false" },
                { "NodeFileBackendTracing", false, "/asyncService/complicated/", HttpRequestMethod.GET, 1, "Backends\\|fs\\|writeFile:Responses Per Interval", -1, TTEvent.ASYNC_NOFS.trace, 1, "false" }, 
                { "NodeFileBackendTracing", true, "/asyncService/complicated/", HttpRequestMethod.GET, 1, "Backends\\|fs\\|writeFile:Responses Per Interval", 1, TTEvent.ASYNC.trace, 1, "false" },
                { "FragmentTracing", false, "/asyncService/complicated/", HttpRequestMethod.GET, 1, "Fragments\\|fragment\\|route_dispatch:Responses Per Interval", -1, TTEvent.ASYNC_NOFRAGMENT.trace, 1, "false" },
                { "FragmentTracing", true, "/asyncService/complicated/", HttpRequestMethod.GET, 1, "Fragments\\|fragment\\|route_dispatch:Responses Per Interval", 1, TTEvent.ASYNC.trace, 1, "false" },
                { "NodeMongoDBTracing", false, "/rest/clickstream?user=ryan", HttpRequestMethod.GET, 1, "Backends\\|tixchange \\(MongoDB\\)\\|Read Operations\\|find:Responses Per Interval", -1, TTEvent.FRONTEND_EXPRESS_GET.trace, 1, "false" },
                { "NodeMongoDBTracing", true, "/rest/clickstream?user=ryan", HttpRequestMethod.GET, 1, "Backends\\|tixchange \\(MongoDB\\)\\|Read Operations\\|find:Responses Per Interval", 1, NO_ELEMENT_TRACE, 0, "false" },
                { "NodeHttpBackendTracing", false, "/httpGetService", HttpRequestMethod.POST, 1, "Backends\\|WebService at http_//localhost_3000:Responses Per Interval", -1, TTEvent.FRONTEND_EXPRESS_POST.trace, 1, "false" },     
                { "NodeHttpBackendTracing", true, "/httpGetService", HttpRequestMethod.POST, 1, "Backends\\|WebService at http_//localhost_3000:Responses Per Interval", 1, TTEvent.HTTP_BACKEND.trace, 1, "false" },                
                { "NodeClassDeepTracing", true, "/api/Items/1", HttpRequestMethod.GET, 1, "Frontends\\|Apps\\|server\\|URLs\\|Default:Responses Per Interval", 1, TTEvent.FRONTEND_DEEP.trace, 1, "true" },
                { "NodeClassDeepTracing;LoopbackDAOTracing", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Frontends\\|Apps\\|server\\|URLs\\|Default:Responses Per Interval", 1, TTEvent.FRONTEND.trace, 1, "true" }
        };
    }

	/**
	 * @return
	 */
	@DataProvider
    protected Object[][] commonPbdTestData() {
        return new Object[][] { 
                { "TraceOneMethodIfFlagged: NodeSqlTracing query NodeStatementBackendTracer.*", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Backends\\|nodetix on localhost-3306 \\(MySQL DB\\):Responses Per Interval", -1, TTEvent.FRONTEND_NOSQL.trace, 1, "false" },
                { "TraceOneMethodIfFlagged: NodeSqlTracing query NodeDbCommandTracer.*", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)\\|SQL.*:Responses Per Interval", -1, TTEvent.FRONTEND_NOSQL_QUERY.trace, 1, "false" }, 
                // disabling redundant mongodb tracing on/off tests. we already covered toggles.
                //{ "TraceOneMethodIfFlagged: NodeMongoDBTracing find NodeMongoDBBackendTracer.*", false, "/rest/clickstream?user=ryan", HttpRequestMethod.GET, 1, "Backends\\|tixchange on localhost-27017 \\(MongoDB\\):Responses Per Interval", -1, TTEvent.FRONTEND_MONGODB_NOSQL_SUM.trace, 1, "false" },
                //{ "TraceOneMethodIfFlagged: NodeMongoDBTracing find NodeMongoDBTracer.*", false, "/rest/clickstream?user=ryan", HttpRequestMethod.GET, 1, "Backends\\|tixchange on localhost-27017 \\(MongoDB\\)\\|find:Responses Per Interval", -1, TTEvent.FRONTEND_MONGODB_NOSQL_QUERY.trace, 1, "false" },
                { "TraceOneMethodIfFlagged: ExpressTracing dispatch.*", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Express\\|/_id\\|GET:Responses Per Interval", -1, TTEvent.FRONTEND_NOEXPRESS.trace, 1, "false" },                  
                { "TraceAllMethodsIfFlagged: NodeHttpFrontendTracing NodeHttpFrontendTracer.*", false, "/api/Items/1", HttpRequestMethod.GET, 1, "Frontends\\|Apps\\|server\\|URLs\\|Default:Responses Per Interval", -1, TTEvent.FRONTEND.trace, 0, "false" },
                { "TraceAllMethodsIfFlagged: NodeFileBackendTracing.*", false, "/asyncService/complicated/", HttpRequestMethod.GET, 1, "Backends\\|fs\\|writeFile:Responses Per Interval", -1, TTEvent.ASYNC_NOFS.trace, 1, "false" },
                { "TraceAllMethodsIfFlagged: FragmentTracing.*", false, "/asyncService/complicated/", HttpRequestMethod.GET, 1, "Fragments\\|fragment\\|route_dispatch:Responses Per Interval", -1, TTEvent.ASYNC_NOFRAGMENT.trace, 1, "false" },
                { "TraceAllMethodsIfFlagged: NodeHttpBackendTracing.*", false, "/httpGetService", HttpRequestMethod.POST, 1, "Backends\\|WebService at http_//localhost_3000:Responses Per Interval", -1, TTEvent.FRONTEND_EXPRESS_POST.trace, 1, "false" }                
        };
    }	
	
	protected void updatePbd(String tracer,
	                         boolean isEnabled) {
	    
	    //update common pbd
	    try {
            if(isEnabled) {
                FileUtils.replace(umAgentConfig.getPbdPath(), "#(" + tracer + ")", "$1");
            }
            else {
                String newDirective =  "#" + tracer;
                if (!Util.matchPattern(umAgentConfig.getPbdPath(), newDirective)) {
                    FileUtils.replace(umAgentConfig.getPbdPath(), "(" + tracer + ")", "#$1");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error while updating pbd file: " + e.getMessage());
        }
	}
	
	protected void updateToggle(String tracers,
                                boolean isEnabled) {
        
        //update toggle pbd
        try {
            for(String tracer: tracers.split(";")) {
                if(isEnabled) {
                    FileUtils.replace(umAgentConfig.getTogglesPbdPath(), 
                         "#TurnOn: " + tracer, "TurnOn: " + tracer);
                }
                else {
                    String newDirective =  "#TurnOn: " + tracer;
                    if (!Util.matchPattern(umAgentConfig.getTogglesPbdPath(), newDirective)) {
                        FileUtils.replace(umAgentConfig.getTogglesPbdPath(), "TurnOn: " + tracer, newDirective);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail("Error while updating toggles file: " + e.getMessage());
        }
    }
	
    protected void makeHttpRequest(String path, int numRequests, 
                                   HttpRequestMethod requestMethod) {
        
        String url = appUrlBase + path;        
        Map<String, String> postParams = new HashMap<String, String>();
		postParams.put("host", "localhost");
		postParams.put("port", tixChangeConfig.getPort());
		postParams.put("path", "/api/Items");
		
		HttpTxnGen txnGen = new HttpTxnGen.HttpTxnGenBuilder(url)
		        .setHttpMethod(requestMethod).setParams(postParams)
		        .setNumberReqs(numRequests).build();
		
		txnGen.start();
    }
    
    protected void checkMetrics(String metricPath, long value) {
        
        LOGGER.info("Checking metric: " + metricPath);
        long waitTime = 30000;        
        MetricAssertionData metricData = new MetricAssertionData.
              RegexMetricNameBuilder(metricPath, value)
                   .setAgentName(testMethodName)
                   .setDuration(waitTime)
                   .build();
                
        if(value >= 0) {
            mockEm.processMetrics(metricData);
        }
        else {
            mockEm.processMetricNames(metricData, false);
        }
    }
	
	protected TraceValidationData getTraceData(ExpectedTraceElement[] expectedTrace,
	                                         int numExpected) {
	  
        final List<ITraceElement[]> snapshotList = new ArrayList<ITraceElement []>();
        snapshotList.add(expectedTrace);
        
        TraceValidationData traceValidationData = new TraceValidationData.Builder(
            numExpected, new RequestProcessor.ITransactionTraceValidator() {
                public boolean validate(TransactionComponentData t) {
                    
                    LOGGER.info(SIUtils.dumpTrace(t,0,"",true)); 
                    return TraceCompareUtil.compareTraceToPatterns(t, snapshotList);
                }
            }).setAgentName(testMethodName).build();

        ITransactionTraceFilter filter = new ParameterValueTransactionTraceFilter(16, "username", "dummy");
        mockEm.getReqProcessor(traceValidationData).addTraceFilter(filter);
        return traceValidationData;
    }
	
	private final ExpectedTraceElement [] NO_ELEMENT_TRACE = {};
	
	private enum TTEvent {
	    FRONTEND(0), FRONTEND_NOSQL(1), FRONTEND_NOEXPRESS(2), ASYNC(3), 
	    ASYNC_NOFS(4), ASYNC_NOFRAGMENT(5), FRONTEND_MONGODB(6), 
	    FRONTEND_EXPRESS_GET(7), FRONTEND_EXPRESS_POST(8), HTTP_BACKEND(9), 
	    FRONTEND_NOSQL_QUERY(10), FRONTEND_MONGODB_NOSQL_QUERY(11), 
	    FRONTEND_MONGODB_NOSQL_SUM(12), FRONTEND_DEEP(13);

	    private ExpectedTraceElement[] trace;

	    TTEvent(int traceNumber) {
	        this.trace = TT_EVENTS[traceNumber];
	    }
	}
	
	private static final ExpectedTraceElement [] [] TT_EVENTS = {
         
        {//0 - frontend with mysql event
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/_id\\|GET"),
            new ExpectedTraceElement("Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)"),
            new ExpectedTraceElement("Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)\\|SQL\\|Dynamic\\|Query\\|SELECT.*")
        },    
        {//1 - frontend w/o sql
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/_id\\|GET")
        },
        {//2 - frontend w/o express
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)"),
            new ExpectedTraceElement("Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)\\|SQL\\|Dynamic\\|Query\\|SELECT.*")
        }, 
        {//3 - async event
            new ExpectedTraceElement("Fragments\\|fragment\\|route_dispatch"),
            new ExpectedTraceElement("Backends\\|fs\\|writeFile")
        },
        {//4 - async event w/o fs
            new ExpectedTraceElement("Fragments\\|fragment\\|route_dispatch")            
        },
        {//5 - async event w/o fragment           
            new ExpectedTraceElement("Backends\\|fs\\|writeFile")
        },
        {//6 - frontend with mongodb event
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/rest/clickstream\\|GET"),            
            new ExpectedTraceElement("Backends\\|tixchange \\(MongoDB\\)\\|Read Operations\\|find"),
            new ExpectedTraceElement("Backends\\|tixchange \\(MongoDB\\)\\|Read Operations\\|toArray")
        }, 
        {//7 - frontend with express get
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/rest/clickstream\\|GET")
        },
        {//8 - frontend with express post
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/httpGetService\\|POST")
        },
        {//9 - http backend
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/httpGetService\\|POST"),
            new ExpectedTraceElement("Backends\\|WebService at http_//localhost_3000"),
            new ExpectedTraceElement("Backends\\|WebService at http_//localhost_3000\\|Paths\\|.*")
        },
        {//10 - frontend w/o sql query
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/_id\\|GET"),
            new ExpectedTraceElement("Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)")
        }, 
        {//11 - frontend w/o mongodb query
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/rest/clickstream\\|GET"),
            new ExpectedTraceElement("Backends\\|tixchange on localhost-27017 \\(MongoDB\\)")
        }, 
        {//12 - frontend w/o mongodb summary
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/rest/clickstream\\|GET"),
            new ExpectedTraceElement("Backends\\|tixchange on localhost-27017 \\(MongoDB\\)\\|find")
        }, 
        {//13 - frontend with deep components
            new ExpectedTraceElement("Frontends\\|Apps\\|server\\|URLs\\|Default"),
            new ExpectedTraceElement("Express\\|/_id\\|GET"),
            new ExpectedTraceElement("loopbackDAO::ACL_find"),
            new ExpectedTraceElement("loopbackDAO::Item_find_by_id"),
            new ExpectedTraceElement("loopbackDAO::Item_find"),
            new ExpectedTraceElement("Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)"),
            new ExpectedTraceElement("Backends\\|nodetix on localhost-3306 \\(MySQL DB\\)\\|SQL\\|Dynamic\\|Query\\|SELECT.*")
        }
	};
}
