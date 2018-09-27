package com.ca.apm.tests.test;

import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Set;

import com.ca.apm.tests.utils.IntroscopeDBAccessUtil;
import com.ca.tas.envproperty.EnvironmentPropertyContext;
import com.ca.tas.envproperty.EnvironmentPropertyContextFactory;
import com.ca.tas.envproperty.EnvironmentPropertyException;
import com.ca.tas.type.SizeType;

import org.mortbay.log.Log;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.ca.apm.tests.testbed.OneEmAbstractTestbed;
import com.ca.apm.tests.testbed.OneEmLinuxTestbed;
import com.ca.apm.tests.testbed.OneEmOneAgentAbstractTestbed;
import com.ca.apm.tests.testbed.OneEmOneAgentLinuxTestbed;
import com.ca.apm.tests.utils.agents.TomcatUtils;
import com.ca.apm.tests.utils.clw.ClwUtils;
import com.ca.apm.tests.utils.emutils.EmConfiguration;
import com.ca.apm.tests.utils.osutils.OsLocalUtils;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;


/**
 * ALM # 350849, 350851
 * 
 * @author batma08
 *
 */
public class JDBCDriverTests {
    private EnvironmentPropertyContext envProps;

    @BeforeTest
    public void setUp() throws EnvironmentPropertyException, IOException {
        envProps = new EnvironmentPropertyContextFactory().createFromSystemProperty();

    }

    /**
     * EM BAT tests #350849<br>
     * Author : Martin Batelka
     * 
     * @author batma08
     * 
     *         <h5>PRECONDITIONS</h5>
     *         <p>
     *         <ul>
     *         <li>TestBed with one running EM.</li>
     *         <li>EM with installed IntroscopeJDBC.jar in lib folder.</li>
     *         <li>EM with running DB at Admin:@localhost:5001.</li>
     *         </ul>
     *         </p>
     * 
     *         <h5>TEST ACTIVITY</h5>
     *         <p>
     *         <ol>
     *         <li>Connection leak test</li>
     *         <ol>
     *         <li>Install Standalone EM</li>
     *         <li>Start EM</li>
     *         <li>Add JDBC driver from <EMHome>\lib\IntroscopeJDBC.jar to classpath.</li>
     *         <li>Check established ports.</li>
     *         <li>Try to establish connection with wrong credentials.</li>
     *         <li>Check established ports again.</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>EXPECTED RESULTS</h5>
     *         <p>
     *         <ol>
     *         <li>After failed connection there should be no new opened port. If no new ports get
     *         into established state, then there is no connection leak. The ports are immediately
     *         released as soon as the Driver finds the credentials are incorrect.</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>RISKS MITIGATED</h5>
     *         <p>
     *         <ul>
     *         <li>JDBC driver doesn't close port after connection failure</li>
     *         </ul>
     *         </p>
     */
    @Tas(testBeds = {@TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"BAT", "IntroscopeJDBC"})
    public void connectionLeakTest() throws Exception {
        final String emInstallDir =
            (String) envProps.getMachineProperties().get(OneEmAbstractTestbed.EM_MACHINE_ID)
                .get(OneEmAbstractTestbed.KeyEmInstallDir);
        EmConfiguration config = new EmConfiguration(emInstallDir, OneEmAbstractTestbed.EM_PORT);
        Thread.sleep(10 * 1000);
        Set<Integer> ports = portCheck();
        try (IntroscopeDBAccessUtil dbAccess = new IntroscopeDBAccessUtil()) {
            Connection connection = null;
            dbAccess.setUpDriver(config.getJDBCDriverPath());

            try {
                connection = dbAccess.setUpConnection("Admin:wrongPassword@localhost:5001");
            } catch (Exception e) {
                Log.info("Expected exception due to wrong credentials.");
            }
            assertEquals(connection, null);
            Thread.sleep(10 * 1000);
            // if credentials are wrong no connection is established so nor new port is established
            assertEquals(ports, OsLocalUtils.getEnabledPorts());
        }
    }


    /**
     * This is not ALM test.
     * Author : Martin Batelka
     * 
     * @author batma08
     * 
     *         <h5>PRECONDITIONS</h5>
     *         <p>
     *         <ul>
     *         <li>TestBed with one running EM.</li>
     *         <li>EM with installed IntroscopeJDBC.jar in lib folder.</li>
     *         <li>EM with running DB at Admin:@localhost:5001.</li>
     *         </ul>
     *         </p>
     * 
     *         <h5>TEST ACTIVITY</h5>
     *         <p>
     *         <ol>
     *         <li>Install Standalone EM</li>
     *         <li>Start EM</li>
     *         <li>Add JDBC driver from <EMHome>\lib\IntroscopeJDBC.jar to classpath.</li>
     *         <li>Check established ports.</li>
     *         <li>Try to establish connection with right credentials. (by default name=Admin, empty
     *         password)</li>
     *         <li>Check established ports again.</li>
     *         <li>Send query and check results.</li>
     *         <li>Check established ports again.</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>EXPECTED RESULTS</h5>
     *         <p>
     *         <ol>
     *         <li>After successful connection there should be a new opened port and query should
     *         return at least one row. After query the connection is closed and also port.</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>RISKS MITIGATED</h5>
     *         <p>
     *         <ul>
     *         <li>Driver is broken or it's not properly loaded.</li>
     *         </ul>
     *         </p>
     */

    @Tas(testBeds = {@TestBed(name = OneEmLinuxTestbed.class, executeOn = OneEmAbstractTestbed.EM_MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"BAT", "IntroscopeJDBC"})
    public void succesfullConnectionTest() throws Exception {
        final String emInstallDir =
            (String) envProps.getMachineProperties().get(OneEmAbstractTestbed.EM_MACHINE_ID)
                .get(OneEmAbstractTestbed.KeyEmInstallDir);
        EmConfiguration config = new EmConfiguration(emInstallDir, OneEmAbstractTestbed.EM_PORT);
        Thread.sleep(10 * 1000);
        Set<Integer> ports = portCheck();
        int resultCount = 0;
        try (IntroscopeDBAccessUtil dbAccess = new IntroscopeDBAccessUtil()) {
            dbAccess.setUpDriver(config.getJDBCDriverPath());
            dbAccess.setUpConnection("Admin:@localhost:5001");
            resultCount = dbAccess.getCount("select * from metric_data");
            assertNotEquals(ports, OsLocalUtils.getEnabledPorts());
            assertEquals(resultCount > 0, true);
        }
        Thread.sleep(30 * 1000);
        assertEquals(ports, OsLocalUtils.getEnabledPorts());
    }


    /**
     * EM BAT tests # 350851<br>
     * Author : Martin Batelka
     * 
     * @author batma08
     * 
     *         <h5>PRECONDITIONS</h5>
     *         <p>
     *         <ul>
     *         <li>TestBed with one running EM.</li>
     *         <li>EM with installed IntroscopeJDBC.jar in lib folder.</li>
     *         <li>EM with running DB at Admin:@localhost:5001.</li>
     *         </ul>
     *         </p>
     * 
     *         <h5>TEST ACTIVITY</h5>
     *         <p>
     *         <ol>
     * 
     *         <li>Install Standalone EM</li> *
     *         <li>Install Tomcat or any other agent</li>
     *         <li>Start EM</li>
     *         <li>Configure agent to conenct to EM with em's host and port.</li>
     *         <li>Start Agent.</li>
     *         <li>Check for node GC Heap under the agent node in the investigator tree</li>
     *         </ol>
     *         </p>
     * 
     *         <h5>EXPECTED RESULTS</h5>
     *         <p>
     *         <ol>
     *         <li>Successful connection test</li>
     *         <ol>
     *         <li>After successful connection there should be a new opened port and query should
     *         return at least one row. After query the connection is closed and also port.</li>
     *         </ol>
     *         </ol>
     *         </p>
     * 
     *         <h5>RISKS MITIGATED</h5>
     *         <p>
     *         <ul>
     *         <li>Driver logic for obtaining aggregated metrics.</li>
     *         </ul>
     *         </p>
     */
    @Tas(testBeds = {@TestBed(name = OneEmOneAgentLinuxTestbed.class, executeOn = OneEmOneAgentAbstractTestbed.MACHINE_ID)}, size = SizeType.MEDIUM, owner = "batma08")
    @Test(groups = {"BAT", "IntroscopeJDBC"})
    public void metricAgregationTest() throws Exception {
        final String emInstallDir =
            (String) envProps.getMachineProperties().get(OneEmOneAgentAbstractTestbed.MACHINE_ID)
                .get(OneEmOneAgentAbstractTestbed.KeyEmInstallDir);
        EmConfiguration config =
            new EmConfiguration(emInstallDir, OneEmOneAgentAbstractTestbed.EM_PORT);

        final String tomcatInstallDir =
            (String) envProps.getMachineProperties().get(OneEmOneAgentAbstractTestbed.MACHINE_ID)
                .get(OneEmOneAgentAbstractTestbed.TomcatInstallDir);
        TomcatUtils.startTomcat(tomcatInstallDir);

        Thread.sleep(60 * 1000);// wait till some metric will be generated and collected

        Calendar start = new GregorianCalendar();
        Calendar end = (Calendar) start.clone();
        start.set(Calendar.MINUTE, start.get(Calendar.MINUTE) - 1);

        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yy h:mm a");
        String startFormated = formatter.format(start.getTime());
        String endFormated = formatter.format(end.getTime());

        ClwUtils cu = new ClwUtils();
        Long inUse = 0L;
        Long total = 0L;

        cu.setClWorkstationJarFileLocation(config.getClwPath());
        inUse =
            cu.getMaxMetricsValueFromAgent(".*|Tomcat|Tomcat Agent", "GC Heap:Bytes In Use.*",
                start, end);
        total =
            cu.getMaxMetricsValueFromAgent(".*|Tomcat|Tomcat Agent", "GC Heap:Bytes Total.*",
                start, end);

        try (IntroscopeDBAccessUtil dbAccess = new IntroscopeDBAccessUtil()) {

            dbAccess.setUpDriver(config.getJDBCDriverPath());
            dbAccess.setUpConnection("Admin:@localhost:5001");

            String bytesTotalQuery =
                "select * from metric_data where agent='.*|Tomcat|Tomcat Agent' and metric='.*Bytes Total.*' and timestamp between '"
                    + startFormated + "' and '" + endFormated + "' aggregateall";
            compareQueryResultWithValue(dbAccess, bytesTotalQuery, total);

            String bytesInUseQuery =
                "select * from metric_data where agent='.*|Tomcat|Tomcat Agent' and metric='.*Bytes In Use.*' and timestamp between '"
                    + startFormated + "' and '" + endFormated + "' aggregateall";
            compareQueryResultWithValue(dbAccess, bytesInUseQuery, inUse);
        }

    }

    /**
     * Method compares query result with given value
     * 
     * @param dbAccess - IntroscopeDB access utility
     * @param query - query to DB, which should return resultset with column "Value" of long format
     * @param value - long value to compare with query result
     */
    private void compareQueryResultWithValue(IntroscopeDBAccessUtil dbAccess, String query,
        Long value) throws Exception {
        try (ResultSet rs = dbAccess.getResult(query)) {
            assertTrue(rs.next());
            Long valueFromDB = rs.getLong("Value");
            assertEquals(valueFromDB, value, "Value from DB(" + valueFromDB
                + ") is different than value from CLW(" + value + "). ");
        }
    }


    /**
     * This method checks open ports.
     * 
     * @return Set of opened ports.
     */
    private Set<Integer> portCheck() throws Exception {
        Set<Integer> ports = OsLocalUtils.getEnabledPorts();
        assertFalse(ports.isEmpty());
        return ports;
    }


}
