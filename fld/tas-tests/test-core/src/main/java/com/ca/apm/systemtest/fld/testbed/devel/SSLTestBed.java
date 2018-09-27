package com.ca.apm.systemtest.fld.testbed.devel;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext.EmRoleEnum;
import com.ca.apm.automation.action.flow.em.EmFeature;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.flow.ConfigureAPMJDBCQueyLoadFlowContext;
import com.ca.apm.systemtest.fld.role.APMJDBCQueryLoadRole;
import com.ca.apm.systemtest.fld.role.SslCertsAndKeysRole;
import com.ca.apm.systemtest.fld.role.loads.HVRAgentLoadRole;
import com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.CertInfo;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.EmRole;
import com.ca.tas.role.IRole;
import com.ca.tas.role.utility.ExecutionRole;
import com.ca.tas.role.utility.UniversalRole;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.testbed.ITestbed;
import com.ca.tas.testbed.ITestbedFactory;
import com.ca.tas.testbed.TestBedUtils;
import com.ca.tas.testbed.Testbed;
import com.ca.tas.testbed.TestbedMachine;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.tests.annotations.TestBedDefinition;
import com.ca.tas.type.ExclusivityType;
import com.ca.tas.type.SizeType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import static com.ca.apm.systemtest.fld.testbed.FLDLoadConstants.APM_JDBC_QUERY_LOAD_ROLE_ID;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.createCert;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.genCaCert;
import static com.ca.apm.systemtest.fld.util.SslCertsAndKeysUtils.getCanonicalHostName;
import static org.testng.Assert.assertTrue;

/**
 * This testbed is for testing of SSL connectivity.
 *
 * @author haiva01
 */
@TestBedDefinition
public class SSLTestBed implements ITestbedFactory {
    public static final String MOM_MACHINE_ID = "momMachine";
    public static final String COLLECTOR1_MACHINE_ID = "collector1Machine";
    public static final String MOM_ROLE_ID = "momRole";
    public static final String COLLECTOR1_ROLE_ID = "collector1Role";
    protected static String EM_TEMPLATE_ID = TestbedMachine.TEMPLATE_W64;

    private static EmRole.Builder setUpSslProperties(EmRole.Builder builder) {
        builder
            .configProperty("introscope.enterprisemanager.enabled.channels", "channel1,channel2")
            .configProperty("introscope.enterprisemanager.webserver.jetty.configurationFile",
                "em-jetty-config.xml");
        return builder;
    }

    private static IRole modifyEmJettyConfigRole(EmRole emRole, ITasResolver tasResolver) {
        FileModifierFlowContext emJettyConfigXmlModContext = new FileModifierFlowContext.Builder()
            .replace(emRole.getInstallDir() + "/config/em-jetty-config.xml",
                new HashMap<String, String>(1) {
                    {
                        put("</Configure>",
                            "  <Call name=\"addConnector\">\n"
                                + "    <Arg>\n"
                                + "      <New class=\"com.wily.webserver.NoNPESocketConnector\">\n"
                                + "        <Set name=\"port\">8081</Set>\n"
                                + "        <Set name=\"HeaderBufferSize\">8192</Set>\n"
                                + "        <Set name=\"RequestBufferSize\">16384</Set>\n"
                                + "        <Set name=\"ThreadPool\">\n"
                                + "          <New class=\"org.mortbay.thread.BoundedThreadPool\">\n"
                                + "            <Set name=\"minThreads\">10</Set>\n"
                                + "            <Set name=\"maxThreads\">100</Set>\n"
                                + "            <Set name=\"maxIdleTimeMs\">60000</Set>\n"
                                + "          </New>\n"
                                + "        </Set>\n"
                                + "      </New>\n"
                                + "    </Arg>\n"
                                + "  </Call>\n"
                                + "\n"
                                + "</Configure>\n");
                    }
                })
            .build();
        UniversalRole role = new UniversalRole.Builder(emRole.getRoleId() + "_emJettyConfig",
            tasResolver)
            .runFlow(FileModifierFlow.class, emJettyConfigXmlModContext)
            .build();
        role.after(emRole);
        return role;
    }

    @Override
    public ITestbed create(ITasResolver tasResolver) {
        // Collector1 role
        EmRole.Builder collector1RoleBuilder = new EmRole.Builder(COLLECTOR1_ROLE_ID, tasResolver)
            .emClusterRole(EmRoleEnum.COLLECTOR)
            .silentInstallChosenFeatures(
                EnumSet.of(EmFeature.ENTERPRISE_MANAGER, EmFeature.WEBVIEW))
            .dbhost(tasResolver.getHostnameById(MOM_ROLE_ID))
            .version("99.99.sys-SNAPSHOT")
            .nostartEM()
            .nostartWV();
        setUpSslProperties(collector1RoleBuilder);
        EmRole collector1Role = collector1RoleBuilder.build();
        IRole collector1JettyConfigModRole = modifyEmJettyConfigRole(collector1Role, tasResolver);

        // MOM role
        EmRole.Builder momRoleBuilder = new EmRole.Builder(MOM_ROLE_ID, tasResolver)
            .emClusterRole(EmRoleEnum.MANAGER)
            .emCollector(collector1Role)
            .autostartApmSqlServer()
            .apmSqlServerBindAddress("0.0.0.0")
            .version("99.99.sys-SNAPSHOT")
            .nostartEM()
            .nostartWV();
        setUpSslProperties(momRoleBuilder);
        EmRole momRole = momRoleBuilder.build();
        IRole momJettyConfigModRole = modifyEmJettyConfigRole(momRole, tasResolver);

        CertInfo caCertInfo = genCaCert();
        final String momHost = getCanonicalHostName(
            tasResolver.getHostnameById(momRole.getRoleId()));
        CertInfo momCertInfo = createCert(caCertInfo.kp.getPrivate(), caCertInfo.kp.getPublic(),
            momHost);

        SslCertsAndKeysRole momSslKeysRoleJreCacerts = new SslCertsAndKeysRole.Builder(
            momRole.getRoleId() + "_sslKeys_JreCacerts")
            .setKeyStorePath(momRole.getInstallDir() + "/jre/lib/security/cacerts", "changeit")
            .addCertToKeystore("Faux CA cert", caCertInfo.certificate)
            .addCertToKeystore(momHost + " cert", momCertInfo.certificate)
            .addPrivateKeyToKeystore("caapm", momCertInfo.kp.getPrivate(), "password",
                Arrays.asList(momCertInfo.certificate, caCertInfo.certificate))
            .writeCertToPemFile(momRole.getInstallDir() + "/config/internal/server/EMcert.pem",
                momCertInfo.certificate)
            .writePrivateKeyToPemFile(momRole.getInstallDir() + "/config/internal/server/EMkey.pem",
                momCertInfo.kp.getPrivate())
            .build();
        momSslKeysRoleJreCacerts.after(momRole);

        SslCertsAndKeysRole momSslKeysRoleServerKeystore = new SslCertsAndKeysRole.Builder(
            momRole.getRoleId() + "_sslKeys_ServerKeystore")
            .setKeyStorePath(momRole.getInstallDir() + "/config/internal/server/keystore",
                "password")
            .addCertToKeystore("Faux CA cert", caCertInfo.certificate)
            .addCertToKeystore(momHost + " cert", momCertInfo.certificate)
            .addPrivateKeyToKeystore("caapm", momCertInfo.kp.getPrivate(), "password",
                Arrays.asList(momCertInfo.certificate, caCertInfo.certificate))
            .build();
        momSslKeysRoleServerKeystore.after(momRole);

        final String collector1Host = getCanonicalHostName(
            tasResolver.getHostnameById(collector1Role.getRoleId()));
        CertInfo collector1CertInfo = createCert(caCertInfo.kp.getPrivate(),
            caCertInfo.kp.getPublic(), collector1Host);

        SslCertsAndKeysRole collector1SslKeysRoleServerKeystore = new SslCertsAndKeysRole.Builder(
            collector1Role.getRoleId() + "_sslKeys_ServerKeystore")
            .setKeyStorePath(collector1Role.getInstallDir() + "/config/internal/server/keystore",
                "password")
            .addCertToKeystore("Faux CA cert", caCertInfo.certificate)
            .addCertToKeystore(collector1Host + " cert", collector1CertInfo.certificate)
            .addPrivateKeyToKeystore("caapm", collector1CertInfo.kp.getPrivate(), "password",
                Arrays.asList(collector1CertInfo.certificate, caCertInfo.certificate))
            .writeCertToPemFile(
                collector1Role.getInstallDir() + "/config/internal/server/EMcert.pem",
                collector1CertInfo.certificate)
            .writePrivateKeyToPemFile(
                collector1Role.getInstallDir() + "/config/internal/server/EMkey.pem",
                collector1CertInfo.kp.getPrivate())
            .build();
        collector1SslKeysRoleServerKeystore.after(collector1Role);

        // Configuration of Collector1 machine
        TestbedMachine collector1Machine = TestBedUtils.createWindowsMachine(
            COLLECTOR1_MACHINE_ID, EM_TEMPLATE_ID, collector1Role, collector1JettyConfigModRole,
            collector1SslKeysRoleServerKeystore);

        // Configuration of mom machine
        TestbedMachine momMachine = TestBedUtils.createWindowsMachine(
            MOM_MACHINE_ID, EM_TEMPLATE_ID, momRole, momSslKeysRoleJreCacerts,
            momJettyConfigModRole, momSslKeysRoleServerKeystore);

        // Start MOM.
        ExecutionRole startMom = new ExecutionRole.Builder(MOM_ROLE_ID + "_start")
            .asyncCommand(momRole.getWvRunCommandFlowContext())
            .asyncCommand(momRole.getEmRunCommandFlowContext())
            .build();
        startMom.after(momRole, momSslKeysRoleJreCacerts, momSslKeysRoleServerKeystore,
            momJettyConfigModRole);
        momMachine.addRole(startMom);

        // JDBC query load setup.
        APMJDBCQueryLoadRole apmjdbcQueryLoadRole = new APMJDBCQueryLoadRole.Builder(
            APM_JDBC_QUERY_LOAD_ROLE_ID, tasResolver)
            .setApmServer(tasResolver.getHostnameById(momRole.getRoleId()))
            .build();
        collector1Machine.addRole(apmjdbcQueryLoadRole);

        // Some load
        HVRAgentLoadRole hvrRole =
            new HVRAgentLoadRole.Builder("hvrRole", tasResolver)
                .addMetricsArtifact(
                    new TasArtifact.Builder("100_TSD")
                        .groupId("com.ca.apm.coda.HVRAgent")
                        .version("1.0")
                        .extension("zip")
                        .build()
                        .getArtifact())
                .loadFile("100_TSD")
                .agentHost(tasResolver.getHostnameById(collector1Role.getRoleId()))
                .emHost(tasResolver.getHostnameById(collector1Role.getRoleId()))
                .replay()
                .start()
                .build();
        collector1Machine.addRole(hvrRole);


        // Start collector.
        ExecutionRole startCollector1 = new ExecutionRole.Builder(COLLECTOR1_ROLE_ID + "_start")
            .asyncCommand(momRole.getEmRunCommandFlowContext())
            .build();
        startCollector1.after(collector1Role, collector1SslKeysRoleServerKeystore,
            collector1JettyConfigModRole);
        collector1Machine.addRole(startCollector1);

        return Testbed.create(this, collector1Machine, momMachine);
    }

    public static class Tester extends TasTestNgTest {
        private Logger log = LoggerFactory.getLogger(Tester.class);

        @Tas(testBeds = @TestBed(name = SSLTestBed.class,
            executeOn = SSLTestBed.COLLECTOR1_MACHINE_ID),
            owner = "haiva01",
            size = SizeType.SMALL,
            exclusivity = ExclusivityType.EXCLUSIVE)
        @Test()
        public void test() throws InterruptedException {
            assertTrue(true);
            startLoad();
            java.util.concurrent.TimeUnit.MINUTES.sleep(2);
        }

        private void runStatement(Connection conn, String query) throws SQLException {
            log.info("About to run APM SQL query: {}", query);
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setQueryTimeout(30);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs == null) {
                        log.warn("No data returned - check agents");
                        return;
                    }
                    int lines = 0;
                    while (rs.next()) {
                        lines++;
                    }
                    log.info("Query returned {} lines", lines);
                }
            } catch (Throwable ex) {
                throw ErrorUtils.logExceptionAndWrapFmt(log, ex,
                    "Failed to run SQL query {1}. Exception: {0}", query);
            }
        }

        protected void startLoad() {
            final ConfigureAPMJDBCQueyLoadFlowContext ctx = deserializeFlowContextFromRole(
                APM_JDBC_QUERY_LOAD_ROLE_ID,
                APMJDBCQueryLoadRole.APM_JDBC_QUERY_LOAD_FLOW_CTX_KEY,
                ConfigureAPMJDBCQueyLoadFlowContext.class);

            final String apmServer = ctx.getApmServer();

            Timer timer = new Timer(true);
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    // Load the database driver
                    try {
                        Class.forName("org.teiid.jdbc.TeiidDriver");
                    } catch (ClassNotFoundException e) {
                        throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                            "Failed to load JDBC driver. Exception: {0}");
                    }

                    String jdbcApmSqlServerUrl = "jdbc:teiid:apm_base@mm://" + apmServer + ":54321";
                    log.info("Opening connection to {}", jdbcApmSqlServerUrl);
                    try (Connection conn = DriverManager.getConnection(jdbcApmSqlServerUrl,
                        "Admin", "")) {
                        Date now = new Date();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("YYYY-MM-dd");
                        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(now);
                        cal.add(Calendar.HOUR, -1);
                        String dateStr = dateFormat.format(cal.getTime());
                        String dateStr2 = dateFormat.format(now);
                        String timeStr = timeFormat.format(cal.getTime());
                        String timeStr2 = timeFormat.format(now);

                        String query1 =
                            "select * from wsmodel.metric_data where "
                                + "agent_name='Agent_007' and metric_attribute like "
                                + "'Average%'and ts between {ts '"
                                + dateStr + " " + timeStr + "'} AND {ts '" + dateStr2
                                + " " + timeStr2 + "'} AND frequency = 15000";

                        String query2 =
                            "select * from wsmodel.metric_data where "
                                + "agent_name='WurlitzerAgent_1' and metric_attribute like "
                                + "'Average%'and ts between {ts '"
                                + dateStr + " " + timeStr + "'} AND {ts '" + dateStr2
                                + " " + timeStr2 + "'} AND frequency = 15000";

                        runStatement(conn, query1);
                        runStatement(conn, query2);
                    } catch (SQLException e) {
                        throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                            "Exception in JDBC load. Exception: {0}");
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 0, 60000L);
        }
    }
}
