/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.em.job;

import static com.ca.apm.systemtest.fld.plugin.em.EmPlugin.DEFAULT_EM_PORT;

import java.net.Socket;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.ca.apm.systemtest.fld.plugin.AbstractJavaDelegate;
import com.ca.apm.systemtest.fld.plugin.NodeManager;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.vo.DashboardIdStore;
import com.ca.apm.systemtest.fld.proxy.AgentProxyFactory;

/**
 * @author keyja01
 *
 */
@Component("emStatusJobs")
public class EmStatusJobsBean implements InitializingBean, ApplicationContextAware {

    @Autowired
    private NodeManager nodeManager;

    @Autowired
    private AgentProxyFactory agentProxyFactory;

    private CheckEmTaskDelegate checkEm;
    private CheckWebviewTaskDelegate checkWebView;
    private MonitorEmTaskDelegate monitorEm;
    private JdbcClientTaskDelegate jdbcClientQueries;
    private ShiftTimeFromToTaskDelegate shiftTimeFromTo;

    private ApplicationContext applicationContext;

    public CheckEmTaskDelegate getCheckEm() {
        return checkEm;
    }

    public CheckWebviewTaskDelegate getCheckWebView() {
        return checkWebView;
    }

    public MonitorEmTaskDelegate getMonitorEm() {
        return monitorEm;
    }

    public JdbcClientTaskDelegate getJdbcClientQueries() {
        return jdbcClientQueries;
    }

    public ShiftTimeFromToTaskDelegate getShiftTimeFromTo() {
        return shiftTimeFromTo;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkEm = new CheckEmTaskDelegate(nodeManager, agentProxyFactory);
        checkWebView = new CheckWebviewTaskDelegate(nodeManager, agentProxyFactory);
        monitorEm = new MonitorEmTaskDelegate(nodeManager, agentProxyFactory);
        jdbcClientQueries = new JdbcClientTaskDelegate(nodeManager, agentProxyFactory);
        shiftTimeFromTo = new ShiftTimeFromToTaskDelegate(nodeManager, agentProxyFactory);
    }


    public class CheckWebviewTaskDelegate extends AbstractJavaDelegate {

        public CheckWebviewTaskDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            RuntimeService runtimeService = applicationContext.getBean(RuntimeService.class);
            
            String node = getExecutionVariable(execution, "node");
            EmPlugin emPlugin = loadPlugin(execution, "node", "wvPlugin", EmPlugin.class);
            String statusVariable = getExecutionVariable(execution, "status");
            boolean emOK = false;
            if (emPlugin != null) {
                Socket s = null;
                try {
                    s = new Socket(node, 8080);
                } catch (Exception e) {
                    emOK = true;
                    // do nothing
                } finally {
                    if (s != null) {
                        s.close();
                    }
                }
            }
            
            String parentId = null;
            String rootExecutionId = execution.getId();
            while ((parentId = execution.getParentId()) != null) {
                rootExecutionId = parentId;
            }

            runtimeService.setVariable(rootExecutionId, statusVariable, emOK ? "OK" : "Error");
        }
    }
    
    
    public class CheckEmTaskDelegate extends AbstractJavaDelegate {

        public CheckEmTaskDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            RuntimeService runtimeService = applicationContext.getBean(RuntimeService.class);
            
            String node = getExecutionVariable(execution, "node");
            EmPlugin emPlugin = loadPlugin(execution, "node", "emPlugin", EmPlugin.class);
            String statusVariable = getExecutionVariable(execution, "status");
            boolean emOK = false;
            if (emPlugin != null) {
                Socket s = null;
                try {
                    s = new Socket(node, 5001);
                    emOK = true;
                } catch (Exception e) {
                    // do nothing
                } finally {
                    if (s != null) {
                        s.close();
                    }
                }
            }
            
            
            String dashboardId = getExecutionVariable(execution, DashboardIdStore.DASHBOARD_VARIABLE);
            Set<String> names = execution.getVariableNames();
            for (String name: names) {
                System.out.println(name);
            }
            
            ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
            List<ProcessInstance> list = query.excludeSubprocesses(true)
                .variableValueEquals(DashboardIdStore.DASHBOARD_VARIABLE, dashboardId)
                .list();
            
            for (ProcessInstance pi: list) {
                System.out.println(pi + ": "+ pi.getProcessDefinitionKey() + ":" + pi.getProcessDefinitionName());
            }
            
//            String parentId = null;
//            String rootExecutionId = execution.getId();
//            while ((parentId = execution.getParentId()) != null) {
//                rootExecutionId = parentId;
//            }
//            
//            if (emOK) {
//                runtimeService.setVariable(rootExecutionId, statusVariable, "OK");
//            } else {
//                runtimeService.setVariable(rootExecutionId, statusVariable, "Error");
//            }
        }
    }


    public static class MonitorEmTaskDelegate extends AbstractJavaDelegate {
        private static final Logger LOGGER = LoggerFactory.getLogger(MonitorEmTaskDelegate.class);

        static final String CLW_COMMAND = "list historical agents";

        public MonitorEmTaskDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String emHost = getStringExecutionVariable(execution, "emHost");
            Number emPort = getNumberExecutionVariable(execution, "emPort");
            String emStatusVariable = getStringExecutionVariable(execution, "emStatusVariable");
            boolean performEmClwQuery = getBooleanExecutionVariable(execution, "performEmClwQuery");

            LOGGER.debug("emHost            = {}", emHost);
            LOGGER.debug("emPort            = {}", emPort);
            LOGGER.debug("emStatusVariable  = {}", emStatusVariable);
            LOGGER.debug("performEmClwQuery = {}", performEmClwQuery);

            int port = (emPort == null) ? DEFAULT_EM_PORT : emPort.intValue();
            LOGGER.debug("using EM port {}", port);

            boolean emOK;
            Socket s = null;
            try {
                s = new Socket(emHost, port);
                LOGGER.info("MONITOR_EM - socket {}:{} connected - OK", emHost, port);
                emOK = true;
            } catch (Exception e) {
                LOGGER.info("MONITOR_EM - unable to connect EM {}:{} : {}", emHost, port, e);
                emOK = false;
            } finally {
                if (s != null) {
                    try {
                        s.close();
                    } catch (Exception e) {
                        // nothing to do
                    }
                }
            }

            if (performEmClwQuery && emOK) {
                emOK = performEmClwQuery(execution, "clw.out", CLW_COMMAND);
            }

            execution.setVariable(emStatusVariable, emOK);
            LOGGER.debug("emOK = {}", emOK);
        }

        private boolean performEmClwQuery(DelegateExecution execution, String outFileName, String clwCommand) {
            try {
                EmPlugin emPlugin = loadPlugin(execution, "emNode", "emPlugin", EmPlugin.class);
                Integer exitCode = emPlugin.executeCLWCommand(clwCommand, outFileName, true);
                LOGGER.debug("exitCode = {}", exitCode);
                return exitCode != null && exitCode.intValue() == 0;
            } catch (Exception e) {
                LOGGER.info("MONITOR_EM - unable to perform CLW query: {}", e);
                return false;
            }
        }
    }


    public static class JdbcClientTaskDelegate extends AbstractJavaDelegate {
        private static final Logger LOGGER = LoggerFactory.getLogger(JdbcClientTaskDelegate.class);

        static final String SQL_QUERY_1 = "select * from metric_data where agent=''.*1ComplexAgent_.*'' and metric=''.*Average.*'' and timestamp between ''{0}'' and ''{1}'' maxmatches=1";
        static final String SQL_QUERY_2 = "select * from metric_data where agent=''.*1PortletAgent_.*'' and metric=''.*Average.*'' and timestamp between ''{0}'' and ''{1}'' maxmatches=1";

        static final String TIME_FROM_TO_DATE_FORMAT = "MM/dd/yy HH:mm:ss";

        private final DateFormat timeFromToDateFormat = new SimpleDateFormat(TIME_FROM_TO_DATE_FORMAT);

        public JdbcClientTaskDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @SuppressWarnings("null")
        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            String timeFrom = getStringExecutionVariable(execution, "timeFrom"); // i.e. "09/13/15 00:00:00";
            String timeTo   = getStringExecutionVariable(execution, "timeTo");   // i.e. "09/14/15 04:00:00";
            LOGGER.debug("timeFrom = {}", timeFrom);
            LOGGER.debug("timeTo   = {}", timeTo);
            if (timeFromToDateFormat.parse(timeTo).after(new Date())) {
                Boolean preventUsingFutureTime = getBooleanExecutionVariable(execution, "preventUsingFutureTime");
                LOGGER.debug("preventUsingFutureTime = {}", preventUsingFutureTime);
                if (preventUsingFutureTime != null && preventUsingFutureTime.booleanValue()) {
                    timeTo = timeFromToDateFormat.format(new Date());
                    LOGGER.info("Parameter preventUsingFutureTime is set: setting timeTo = {}", timeTo);
                } else {
                    LOGGER.warn("Parameter timeTo = {} is in future, SQL query will wait!", timeTo);
                }
            }

            String query1 = MessageFormat.format(SQL_QUERY_1, timeFrom, timeTo);
            String query2 = MessageFormat.format(SQL_QUERY_2, timeFrom, timeTo);
            LOGGER.debug("query1 = {}", query1);
            LOGGER.debug("query2 = {}", query2);

            String emHost = getStringExecutionVariable(execution, "emHost");
            Number emPort = getNumberExecutionVariable(execution, "emPort");
            String user = getStringExecutionVariable(execution, "user");
            String password = getStringExecutionVariable(execution, "password");
            LOGGER.debug("emHost   = {}", emHost);
            LOGGER.debug("emPort   = {}", emPort);
            LOGGER.debug("user     = {}", user);
            LOGGER.debug("password = {}", password);
            boolean urlParamsFilled = StringUtils.hasText(emHost) && (emPort != null) && StringUtils.hasText(user) /*&& (password != null)*/;

            EmPlugin emPlugin = loadPlugin(execution, "emNode", "emPlugin", EmPlugin.class);
            List<Integer> counts;
            if (urlParamsFilled) {
                LOGGER.info("EM DB params filled");
                counts = emPlugin.executeJdbc(emHost, emPort.intValue(), user, password, query1, query2);
            } else {
                LOGGER.info("EM DB params not filled");
                counts = emPlugin.executeJdbc(query1, query2);
            }
            LOGGER.info("emPlugin.executeJdbc result: {}", counts);

            boolean fldMonitorJdbcQuery = (counts != null) && (counts.size() == 2) && (counts.get(0) != null && counts.get(0).intValue() > 0) && (counts.get(1) != null && counts.get(1).intValue() > 0);
            LOGGER.debug("fldMonitorJdbcQuery = {}", fldMonitorJdbcQuery);
            execution.setVariable("fld.monitor.jdbc.query", fldMonitorJdbcQuery);
        }
    }


    private static class ShiftTimeFromToTaskDelegate extends AbstractJavaDelegate {
        private static final Logger LOGGER = LoggerFactory.getLogger(ShiftTimeFromToTaskDelegate.class);

        private final DateFormat timeFromToDateFormat = new SimpleDateFormat(JdbcClientTaskDelegate.TIME_FROM_TO_DATE_FORMAT);

        public ShiftTimeFromToTaskDelegate(NodeManager nodeManager, AgentProxyFactory agentProxyFactory) {
            super(nodeManager, agentProxyFactory);
        }

        @Override
        protected void handleExecution(DelegateExecution execution) throws Throwable {
            Boolean shiftQueryTimes = getBooleanExecutionVariable(execution, "shiftQueryTimes");
            LOGGER.debug("shiftQueryTimes = {}", shiftQueryTimes);
            if (shiftQueryTimes == null || !shiftQueryTimes.booleanValue()) {
                LOGGER.info("Shifting query times not set, exiting");
                return;
            }

            String timeFrom_1 = getStringExecutionVariable(execution, "timeFrom");
            String timeTo_1   = getStringExecutionVariable(execution, "timeTo");
            LOGGER.debug("timeFrom = {}", timeFrom_1);
            LOGGER.debug("timeTo   = {}", timeTo_1);

            long now = System.currentTimeMillis();
            LOGGER.debug("now = {}", now);

            Number lastStart = getNumberExecutionVariable(execution, "lastStart", now);
            LOGGER.debug("lastStart = {}", lastStart);

            long shift = (now - lastStart.longValue());
            LOGGER.debug("shift = {} [ms]", shift);

            String timeFrom_2 = timeFromToDateFormat.format(new Date(shift + timeFromToDateFormat.parse(timeFrom_1).getTime()));
            String timeTo_2 = timeFromToDateFormat.format(new Date(shift + timeFromToDateFormat.parse(timeTo_1).getTime()));

            LOGGER.info("timeFrom: {} ==> {}", timeFrom_1, timeFrom_2);
            LOGGER.info("timeTo:   {} ==> {}", timeTo_1, timeTo_2);

            setExecutionVariable(execution, "timeFrom", timeFrom_2);
            setExecutionVariable(execution, "timeTo", timeTo_2);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

}
