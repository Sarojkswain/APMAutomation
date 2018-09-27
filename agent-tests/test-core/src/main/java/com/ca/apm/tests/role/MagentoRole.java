/*
 * Copyright (c) 2016 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.tests.role;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.http.util.Args;
import org.jetbrains.annotations.Nullable;

import com.ca.apm.automation.action.flow.FlowConfig.FlowConfigBuilder;
import com.ca.apm.automation.action.flow.RunMySqlImportFlow;
import com.ca.apm.automation.action.flow.RunMySqlImportFlowContext;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlow;
import com.ca.apm.automation.action.flow.commandline.RunCommandFlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.action.flow.utility.GenericFlow;
import com.ca.apm.automation.action.flow.utility.GenericFlowContext;
import com.ca.apm.tests.artifact.MagentoSampleDataVersion;
import com.ca.apm.tests.artifact.MagentoVersion;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.MysqlRole;

/**
 * MagentoRole class.
 *
 * Deploys Magento and loads sample data into it.
 *
 * @author Aleem Ahmad (ahmal01@ca.com)
 */
public class MagentoRole extends AbstractRole {

    private static final int UNPACK_TIMEOUT = 240;

    private static final String HOSTNAME_FQDN_APPENDIX = ".ca.com";

    private static final String MAGENTO_DB_NAME = "magento";
    private static final String MAGENTO_CONTEXT = "/magento";

    private static final String DEFAULT_MAGENTO_ADMIN_USERNAME = "admin";
    private static final String DEFAULT_MAGENTO_ADMIN_PASSWORD = "Test1234";

    /** Constant <code>ENV_MAGENTO_URL="magentoUrl"</code> */
    public static final String ENV_MAGENTO_URL = "magentoUrl";
    /** Constant <code>ENV_MAGENTO_ADMIN_USERNAME="magentoAdminUsername"</code> */
    public static final String ENV_MAGENTO_ADMIN_USERNAME = "magentoAdminUsername";
    /** Constant <code>ENV_MAGENTO_ADMIN_PASSWORD="magentoAdminPassword"</code> */
    public static final String ENV_MAGENTO_ADMIN_PASSWORD = "magentoAdminPassword";

    private final GenericFlowContext deployFlowContext;
    private final GenericFlowContext deploySampleDataFlowContext;
    private final FileModifierFlowContext modifySampleDataSqlFlowContext;
    private final FileModifierFlowContext copySampleDataFlowContext;
    private final RunCommandFlowContext chmodFlowContext;
    private final RunCommandFlowContext installFlowContext;
    private final RunMySqlImportFlowContext mysqlImportContext;

    private final ApacheRole apacheRole;

    /**
     * <p>
     * Constructor for PhpAgentRole.
     * </p>
     *
     * @param builder Builder object containing all necessary data
     */
    protected MagentoRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());

        deployFlowContext = builder.deployFlowContext;
        deploySampleDataFlowContext = builder.deploySampleDataFlowContext;
        modifySampleDataSqlFlowContext = builder.modifySampleDataSqlFlowContext;
        copySampleDataFlowContext = builder.copySampleDataFlowContext;
        chmodFlowContext = builder.chmodFlowContext;
        installFlowContext = builder.installFlowContext;
        mysqlImportContext = builder.mysqlImportContext;
        apacheRole = builder.apacheRole;
    }

    /** {@inheritDoc} */
    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        deployMagento(aaClient);
        deployMagentoSampleData(aaClient);
        setFileAccessRights(aaClient);
        importSqlToMysql(aaClient);
        runMagentoSilentInstall(aaClient);
    }

    /**
     * <p>
     * deployMagento.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void deployMagento(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(GenericFlow.class, deployFlowContext,
            getHostWithPort()).timeout(UNPACK_TIMEOUT));
    }

    /**
     * <p>
     * deployMagentoSampleData.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void deployMagentoSampleData(IAutomationAgentClient aaClient) {
        aaClient.runJavaFlow(new FlowConfigBuilder(GenericFlow.class, deploySampleDataFlowContext,
            getHostWithPort()).timeout(UNPACK_TIMEOUT));

        aaClient.runJavaFlow(new FlowConfigBuilder(FileModifierFlow.class,
            modifySampleDataSqlFlowContext, getHostWithPort()));

        aaClient.runJavaFlow(new FlowConfigBuilder(FileModifierFlow.class,
            copySampleDataFlowContext, getHostWithPort()));
    }

    /**
     * <p>
     * setFileAccessRights.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void setFileAccessRights(IAutomationAgentClient aaClient) {
        if (chmodFlowContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, chmodFlowContext,
                getHostWithPort()));
        }
    }

    /**
     * <p>
     * Import sql into mysql
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void importSqlToMysql(IAutomationAgentClient aaClient) {
        assert getHostingMachine() != null;

        if (mysqlImportContext != null) {
            aaClient.runJavaFlow(new FlowConfigBuilder(RunMySqlImportFlow.class, mysqlImportContext,
                getHostWithPort()));
        }
    }

    /**
     * <p>
     * runMagentoSilentInstall.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void runMagentoSilentInstall(IAutomationAgentClient aaClient) {
        startApache(aaClient);

        aaClient.runJavaFlow(new FlowConfigBuilder(RunCommandFlow.class, installFlowContext,
            getHostWithPort()));

        stopApache(aaClient);
    }

    /**
     * <p>
     * startApache.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void startApache(IAutomationAgentClient aaClient) {
        apacheRole.start(aaClient);
    }

    /**
     * <p>
     * stopApache.
     * </p>
     *
     * @param aaClient a {@link com.ca.tas.client.IAutomationAgentClient} object.
     */
    protected void stopApache(IAutomationAgentClient aaClient) {
        apacheRole.stop(aaClient);
    }

    /**
     * Linux Builder responsible for holding all necessary properties to instantiate
     * {@link MagentoRole}
     */
    public static class LinuxBuilder extends Builder {

        public LinuxBuilder(String roleId, ITasResolver tasResolver) {
            super(roleId, tasResolver);
        }

        @Override
        protected void initFlowContextBuilders() {
            super.initFlowContextBuilders();

            chmodFlowContextBuilder = new RunCommandFlowContext.Builder("chmod");
        }

        protected void initChmodFlow() {
            List<String> args = new ArrayList<String>();

            args.add("-R");
            args.add("777");
            args.add(deployFlowContext.getDestination());

            chmodFlowContext = chmodFlowContextBuilder.args(args).build();
        }

        @Override
        protected LinuxBuilder builder() {
            return this;
        }

        @Override
        protected String getDeployBase() {
            return getLinuxDeployBase();
        }

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    /**
     * Builder responsible for holding all necessary properties to instantiate {@link MagentoRole}
     */
    public static class Builder extends BuilderBase<Builder, MagentoRole> {

        private final String roleId;
        protected final ITasResolver tasResolver;

        protected GenericFlowContext.Builder deployFlowContextBuilder;
        protected GenericFlowContext.Builder deploySampleDataFlowContextBuilder;
        protected FileModifierFlowContext.Builder modifySampleDataSqlContextBuilder;
        protected FileModifierFlowContext.Builder copySampleDataFlowContextBuilder;
        protected RunCommandFlowContext.Builder chmodFlowContextBuilder;
        protected RunCommandFlowContext.Builder installFlowContextBuilder;
        protected RunMySqlImportFlowContext.Builder mysqlImportContextBuilder;

        protected GenericFlowContext deployFlowContext;
        protected GenericFlowContext deploySampleDataFlowContext;
        protected FileModifierFlowContext modifySampleDataSqlFlowContext;
        protected FileModifierFlowContext copySampleDataFlowContext;
        protected RunCommandFlowContext chmodFlowContext;
        protected RunCommandFlowContext installFlowContext;
        protected RunMySqlImportFlowContext mysqlImportContext;


        protected MysqlRole mysqlRole;
        protected ApacheRole apacheRole;

        protected MagentoVersion magentoVersion;
        protected String magentoDestination;

        protected MagentoSampleDataVersion magentoSampleDataVersion;
        protected String magentoSampleDataDestination;

        @Nullable
        protected String magentoAdminUsername;
        @Nullable
        protected String magentoAdminPassword;
        @Nullable
        protected String sqlImportScript;

        public Builder(String roleId, ITasResolver tasResolver) {
            this.roleId = roleId;
            this.tasResolver = tasResolver;
            initFlowContextBuilders();
        }

        protected void initFlowContextBuilders() {
            deployFlowContextBuilder = new GenericFlowContext.Builder();
            deploySampleDataFlowContextBuilder = new GenericFlowContext.Builder();
            modifySampleDataSqlContextBuilder = new FileModifierFlowContext.Builder();
            copySampleDataFlowContextBuilder = new FileModifierFlowContext.Builder();
            installFlowContextBuilder = new RunCommandFlowContext.Builder("php");
            mysqlImportContextBuilder = new RunMySqlImportFlowContext.Builder();
        }

        @Override
        public MagentoRole build() {
            initDeployFlow();
            initDeploySampleDataFlow();
            initModifySampleDataSqlFlow();
            initCopySampleDataFlow();
            initChmodFlow();
            initCredentials();
            initInstallFlow();
            initMysqlImportFlow();

            return getInstance();
        }

        public Builder mysqlRole(MysqlRole mysqlRole) {
            Args.notNull(mysqlRole, "mysqlRole");
            this.mysqlRole = mysqlRole;
            return builder();
        }

        public Builder apacheRole(ApacheRole apacheRole) {
            Args.notNull(apacheRole, "apacheRole");
            this.apacheRole = apacheRole;
            return builder();
        }

        public Builder magentoVersion(MagentoVersion magentoVersion) {
            Args.notNull(magentoVersion, "magentoVersion");
            this.magentoVersion = magentoVersion;
            return builder();
        }

        public Builder magentoDestination(String magentoDestination) {
            Args.notNull(magentoDestination, "magentoDestination");
            this.magentoDestination = magentoDestination;
            return builder();
        }

        public Builder magentoSampleDataVersion(MagentoSampleDataVersion magentoSampleDataVersion) {
            Args.notNull(magentoSampleDataVersion, "magentoSampleDataVersion");
            this.magentoSampleDataVersion = magentoSampleDataVersion;
            return builder();
        }

        public Builder magentoSampleDataDestination(String magentoSampleDataDestination) {
            Args.notNull(magentoSampleDataDestination, "magentoSampleDataDestination");
            this.magentoSampleDataDestination = magentoSampleDataDestination;
            return builder();
        }

        public Builder magentoAdminUsername(String magentoAdminUsername) {
            Args.notNull(magentoAdminUsername, "magentoAdminUsername");
            this.magentoAdminUsername = magentoAdminUsername;
            return builder();
        }

        public Builder magentoAdminPassword(String magentoAdminPassword) {
            Args.notNull(magentoAdminPassword, "magentoAdminPassword");
            this.magentoAdminPassword = magentoAdminPassword;
            return builder();
        }
        
        public Builder sqlImportScript(String sqlImportScript) {
            this.sqlImportScript = sqlImportScript;
            return builder();
        }

        protected void initDeployFlow() {
            URL artifactURL = tasResolver.getArtifactUrl(magentoVersion);
            deployFlowContext =
                deployFlowContextBuilder.artifactUrl(artifactURL).destination(magentoDestination)
                    .build();
        }

        protected void initDeploySampleDataFlow() {
            URL artifactURL = tasResolver.getArtifactUrl(magentoSampleDataVersion);
            deploySampleDataFlowContext =
                deploySampleDataFlowContextBuilder.artifactUrl(artifactURL)
                    .destination(magentoSampleDataDestination).build();
        }

        protected void initModifySampleDataSqlFlow() {
            String magentoSampleDataSqlFile =
                concatPaths(deploySampleDataFlowContext.getDestination(),
                    magentoSampleDataVersion.getSqlFileWithinArchive());
            modifySampleDataSqlFlowContext =
                modifySampleDataSqlContextBuilder
                    .insertAt(
                        magentoSampleDataSqlFile,
                        0,
                        Arrays.asList(
                            "CREATE DATABASE IF NOT EXISTS `" + getMagentoDbName() + "`;", "USE `"
                                + getMagentoDbName() + "`;"))
                    .append(
                        magentoSampleDataSqlFile,
                        Collections
                            .singleton("UPDATE `core_config_data` SET value = '0' WHERE scope = 'default' AND scope_id = 0 AND path = 'web/seo/use_rewrites';"))
                    .build();
        }

        protected void initCopySampleDataFlow() {
            copySampleDataFlowContext =
                copySampleDataFlowContextBuilder
                    .copy(concatPaths(deploySampleDataFlowContext.getDestination(), "media"),
                        deployFlowContext.getDestination())
                    .copy(concatPaths(deploySampleDataFlowContext.getDestination(), "skin"),
                        deployFlowContext.getDestination())
                        .build();
        }

        protected void initChmodFlow() {
            // noop
        }

        protected void initMysqlImportFlow() {
            if (sqlImportScript != null) {
                mysqlImportContext = mysqlImportContextBuilder
                    .importSql(sqlImportScript)
                    .username(getMysqlUsername())
                    .password(getMysqlPassword())
                    .mysqlExec("mysql").build();
            }
        }
        
        protected void initCredentials() {
            if (magentoAdminUsername == null) {
                magentoAdminUsername = DEFAULT_MAGENTO_ADMIN_USERNAME;
            }
            getEnvProperties().add(ENV_MAGENTO_ADMIN_USERNAME, magentoAdminUsername);
            if (magentoAdminPassword == null) {
                magentoAdminPassword = DEFAULT_MAGENTO_ADMIN_PASSWORD;
            }
            getEnvProperties().add(ENV_MAGENTO_ADMIN_PASSWORD, magentoAdminPassword);
        }

        protected void initInstallFlow() {
            final String magentoUrl = getMagentoHostnamePlusContext();

            final List<String> args =
                new ArrayList<String>(Arrays.asList("-f", "install.php", "--",
                    "--license_agreement_accepted", "yes", "--locale", "en_US", "--timezone",
                    "America/Los_Angeles", "--default_currency", "USD", "--db_host", "localhost",
                    "--db_name", getMagentoDbName(), "--db_user", getMysqlUsername(), "--db_pass",
                    getMysqlPassword(), "--url", "http://" + magentoUrl, "--use_rewrites", "no",
                    "--use_secure", "no", "--secure_base_url", "https://" + magentoUrl,
                    "--use_secure_admin", "no", "--admin_lastname", "Admin", "--admin_firstname",
                    "Magento", "--admin_email", "magento.admin@example.com", "--admin_username",
                    magentoAdminUsername, "--admin_password", getMagentoAdminPassword()));

            installFlowContext =
                installFlowContextBuilder.args(args).workDir(deployFlowContext.getDestination())
                    .doNotPrependWorkingDirectory().build();

            getEnvProperties().add(ENV_MAGENTO_URL, "http://" + magentoUrl);
        }

        protected String getMagentoDbName() {
            return MAGENTO_DB_NAME;
        }

        protected String getMagentoAdminUsername() {
            return magentoAdminUsername;
        }

        protected String getMagentoAdminPassword() {
            return magentoAdminPassword;
        }

        protected String getMagentoHostnamePlusContext() {
            return tasResolver.getHostnameById(apacheRole.getRoleId()) + HOSTNAME_FQDN_APPENDIX
                + MAGENTO_CONTEXT;
        }

        protected String getMysqlUsername() {
            return mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_USERNAME);
        }

        protected String getMysqlPassword() {
            return mysqlRole.getEnvPropertyById(MysqlRole.ENV_MYSQL_PASSWORD);
        }

        @Override
        protected MagentoRole getInstance() {
            return new MagentoRole(this);
        }

        protected Builder builder() {
            return this;
        }
    }
}
