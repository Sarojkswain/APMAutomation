package com.ca.apm.systemtest.fld.flow;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;

/**
 * Oracle DB installation flow context.
 * 
 * @author sinal04
 *
 */
public class DeployOracleDBFlowContext extends FileModifierFlowContext {

    public static final String SILENT_INSTALL_ORACLE_BASE = "[ORACLE_BASE]";
    public static final String SILENT_INSTALL_ORACLE_HOME = "[ORACLE_HOME]";
    public static final String SILENT_INSTALL_ORACLE_HOME_NAME = "[ORACLE_HOME_NAME]";
    public static final String SILENT_INSTALL_N_CONFIGURATION_OPTION = "[N_CONFIGURATION_OPTION]";
    public static final String SILENT_INSTALL_N_DB_TYPE = "[N_DB_TYPE]";
    public static final String SILENT_INSTALL_S_GLOBAL_DB_NAME = "[S_GLOBAL_DB_NAME]";
    public static final String SILENT_INSTALL_S_DB_SID = "[S_DB_SID]";
    public static final String SILENT_INSTALL_N_SELECTED_MEMORY_FOR_ORACLE = "[N_SELECTED_MEMORY_FOR_ORACLE]";
    public static final String SILENT_INSTALL_N_MEMORY_OPTION = "[N_MEMORY_OPTION]";
    public static final String SILENT_INSTALL_N_DB_STORAGE_TYPE = "[N_DB_STORAGE_TYPE]";
    public static final String SILENT_INSTALL_S_SUPER_ADMIN_SAME_PASSWD = "[S_SUPER_ADMIN_SAME_PASSWD]";
    
    private final Map<String, String> responseFileOptions;
    
    private String oracleBaseDir;
    private String oracleHome;
    private String oracleHomeName;
    private String globalDbName;
    private String dbSID;
    private String superAdminSamePassword;
    private String installLocation;
    private String responseFileDir;
    private String responseFileName;
    private String installUnpackedSourcesLocation;
    private URL installPackageUrl;
    private int configurationOption;
    private int dbType;
    private int memoryOption;
    private int storageType;
    private long selectedMemoryForOracle;
    
    protected DeployOracleDBFlowContext(Builder builder) {
        super(builder);
        
        this.responseFileOptions = builder.responseFileOptions;
        this.oracleBaseDir = builder.oracleBaseDir;
        this.oracleHome = builder.oracleHome;
        this.oracleHomeName = builder.oracleHomeName;
        this.globalDbName = builder.globalDbName;
        this.dbSID = builder.dbSID;
        this.superAdminSamePassword = builder.superAdminSamePassword;
        this.configurationOption = builder.configurationOption;
        this.dbType = builder.dbType;
        this.memoryOption = builder.memoryOption;
        this.storageType = builder.storageType;
        this.selectedMemoryForOracle = builder.selectedMemoryForOracle;
        this.installLocation = builder.installLocation;
        this.installUnpackedSourcesLocation = builder.installUnpackedSourcesLocation;
        this.responseFileDir = builder.responseFileDir;
        this.responseFileName = builder.responseFileName;
        this.installPackageUrl = builder.installPackageUrl;
    }

    public URL getInstallPackageUrl() {
        return this.installPackageUrl;
    }
    
    public Map<String, String> getResponseFileOptions() {
        return responseFileOptions;
    }
    
    /**
     * @return the installLocation
     */
    public String getInstallLocation() {
        return installLocation;
    }

    /**
     * @return the responseFilePath
     */
    public String getResponseFileDir() {
        return responseFileDir;
    }

    /**
     * @return the installUnpackedSourcesLocation
     */
    public String getInstallUnpackedSourcesLocation() {
        return installUnpackedSourcesLocation;
    }

    public String getResponseFileName() {
        return responseFileName;
    }
    
    /**
     * @return the oracleBaseDir
     */
    public String getOracleBaseDir() {
        return oracleBaseDir;
    }

    /**
     * @return the oracleHome
     */
    public String getOracleHome() {
        return oracleHome;
    }

    /**
     * @return the oracleHomeName
     */
    public String getOracleHomeName() {
        return oracleHomeName;
    }

    /**
     * @return the globalDbName
     */
    public String getGlobalDbName() {
        return globalDbName;
    }

    /**
     * @return the dbSID
     */
    public String getDbSID() {
        return dbSID;
    }

    /**
     * @return the superAdminSamePassword
     */
    public String getSuperAdminSamePassword() {
        return superAdminSamePassword;
    }

    /**
     * @return the configurationOption
     */
    public int getConfigurationOption() {
        return configurationOption;
    }

    /**
     * @return the dbType
     */
    public int getDbType() {
        return dbType;
    }

    /**
     * @return the memoryOption
     */
    public int getMemoryOption() {
        return memoryOption;
    }

    /**
     * @return the storageType
     */
    public int getStorageType() {
        return storageType;
    }

    /**
     * @return the selectedMemoryForOracle
     */
    public long getSelectedMemoryForOracle() {
        return selectedMemoryForOracle;
    }


    /**
     * Oracle DB installation flow context builder.
     * 
     * @author sinal04
     *
     */
    public static class Builder extends FileModifierFlowContext.Builder {

        private final Map<String, String> responseFileOptions = new HashMap<>();
        
        protected String oracleBaseDir = "C:/sw/oracle";
        protected String oracleHome = "C:/sw/oracle/product/11.1.0/db_1";
        protected String oracleHomeName = "OraDb11g_home1";
        protected String globalDbName = "tradedb.ca.com";
        protected String dbSID = "tradedb";
        protected String superAdminSamePassword = "password";
        protected String installLocation;
        protected String responseFileDir;
        protected String responseFileName = "standard.rsp";
        protected String installUnpackedSourcesLocation;
        protected String encoding;
        protected URL installPackageUrl;

        protected int configurationOption = 1;
        protected int dbType = 1;
        protected int memoryOption = 1;
        protected int storageType = 1;
        protected long selectedMemoryForOracle = 1024L;
        
        
        @Override
        public DeployOracleDBFlowContext build() {
            Args.notNull(oracleBaseDir, "oracleBaseDir");
            Args.notNull(oracleHome, "oracleHome");
            Args.notNull(oracleHomeName, "oracleHomeName");
            Args.notNull(globalDbName, "globalDbName");
            Args.notNull(dbSID, "dbSID");
            Args.notNull(superAdminSamePassword, "superAdminSamePassword");
            Args.notNull(installLocation, "installLocation");
            Args.notNull(installPackageUrl, "installPackageUrl");
            Args.notNull(installUnpackedSourcesLocation, "installUnpackedSourcesLocation");
            Args.notNull(responseFileName, "responseFileName");
            
            DeployOracleDBFlowContext context = getInstance();
            return context;
        }

        public Builder setOracleBaseDir(String dir) {
            this.oracleBaseDir = dir;
            return this;
        }
        
        public Builder setOrachleHome(String dir) {
            this.oracleHome = dir;
            return this;
        }
        
        public Builder setOracleHomeName(String name) {
            this.oracleHomeName = name;
            return this;
        }

        /**
         * @param installPackageUrl the installPackageUrl to set
         */
        public Builder setInstallPackageUrl(URL installPackageUrl) {
            this.installPackageUrl = installPackageUrl;
            return this;
        }

        /**
         * @param installLocation the installLocation to set
         */
        public Builder setInstallLocation(String installLocation) {
            this.installLocation = installLocation;
            return this;
        }

        /**
         * @param installUnpackedSourcesLocation the installUnpackedSourcesLocation to set
         */
        public Builder setInstallUnpackedSourcesLocation(String installUnpackedSourcesLocation) {
            this.installUnpackedSourcesLocation = installUnpackedSourcesLocation;
            return this;
        }
        
        public Builder setResponseFileName(String responseFileName) {
            this.responseFileName = responseFileName;
            return this;
        }

        public Builder setResponseFileDir(String responseFileDir) {
            this.responseFileDir = responseFileDir;
            return this;
        }
        
        protected void initResponseFileData() {
            responseFileOptions.put(SILENT_INSTALL_ORACLE_BASE, oracleBaseDir);
            responseFileOptions.put(SILENT_INSTALL_ORACLE_HOME, oracleHome);
            responseFileOptions.put(SILENT_INSTALL_ORACLE_HOME_NAME, oracleHomeName);
            responseFileOptions.put(SILENT_INSTALL_N_CONFIGURATION_OPTION, String.valueOf(configurationOption));
            responseFileOptions.put(SILENT_INSTALL_N_DB_TYPE, String.valueOf(dbType));
            responseFileOptions.put(SILENT_INSTALL_S_GLOBAL_DB_NAME, globalDbName);
            responseFileOptions.put(SILENT_INSTALL_S_DB_SID, dbSID);
            responseFileOptions.put(SILENT_INSTALL_N_SELECTED_MEMORY_FOR_ORACLE, String.valueOf(selectedMemoryForOracle));
            responseFileOptions.put(SILENT_INSTALL_N_MEMORY_OPTION, String.valueOf(memoryOption));
            responseFileOptions.put(SILENT_INSTALL_N_DB_STORAGE_TYPE, String.valueOf(storageType));
            responseFileOptions.put(SILENT_INSTALL_S_SUPER_ADMIN_SAME_PASSWD, superAdminSamePassword);
        }

        public Builder setEncoding(String customEncoding) {
            encoding = customEncoding;
            super.encoding(encoding);
            return this;
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        protected DeployOracleDBFlowContext getInstance() {
            initResponseFileData();
            if (encoding == null) {
                setEncoding(StandardCharsets.UTF_8.name());
            }
            return new DeployOracleDBFlowContext(this);
        }
        
    }
}
