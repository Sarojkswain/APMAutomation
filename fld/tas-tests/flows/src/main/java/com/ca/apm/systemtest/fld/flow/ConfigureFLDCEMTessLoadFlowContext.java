package com.ca.apm.systemtest.fld.flow;

import java.util.HashMap;
import java.util.Map;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.property.AbstractEnvPropertySerializer;
import com.ca.tas.property.EnvPropSerializable;

/**
 * Context which holds settings for CEM Tess Load. 
 * 
 * @author filja01
 *
 */
public class ConfigureFLDCEMTessLoadFlowContext implements IFlowContext, EnvPropSerializable<ConfigureFLDCEMTessLoadFlowContext> {

    protected String testAppUrl;
    protected String database;
    protected String dbPort;
    protected String dbUser;
    protected String dbPass;
    protected Integer users;
    protected Integer defects;
    protected Integer logins;
    protected Integer databaseLogins;
    protected Integer speedRate;
    
    private final transient Serializer envPropSerializer = new Serializer(this);

    public String getTestAppUrl() {
		return testAppUrl;
	}

	public String getDatabase() {
		return database;
	}

	public String getDbPort() {
		return dbPort;
	}
	
	public String getDbUser() {
		return dbUser;
	}
	
	public String getDbPass() {
        return dbPass;
    }
	
	public Integer getUsers() {
        return users;
    }
	
	public Integer getDefects() {
        return defects;
    }
	
	public Integer getLogins() {
        return logins;
    }
	
	public Integer getDatabaselogins() {
        return databaseLogins;
    }
	
	public Integer getSpeedRate() {
        return speedRate;
    }

	public static Builder getBuilder() {
        BuilderFactory<ConfigureFLDCEMTessLoadFlowContext, Builder> factory = new BuilderFactory<>();
        return factory.newBuilder(ConfigureFLDCEMTessLoadFlowContext.class, Builder.class);
    }

    public interface Builder extends IGenericBuilder<ConfigureFLDCEMTessLoadFlowContext> {
        public Builder testAppUrl(String testAppUrl);
        public Builder database(String database);
        public Builder dbPort(String dbPort);
        public Builder dbUser(String dbUser);
        public Builder dbPass(String dbPass);
        public Builder users(Integer users);
        public Builder defects(Integer defects);
        public Builder logins(Integer logins);
        public Builder databaseLogins(Integer databaseLogins);
        public Builder speedRate(Integer speedRate);
    }

    @Override
    public ConfigureFLDCEMTessLoadFlowContext deserialize(String key, Map<String, String> map) {
        return envPropSerializer.deserialize(key, map);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return envPropSerializer.serialize(key);
    }
    
    public static class Serializer extends AbstractEnvPropertySerializer<ConfigureFLDCEMTessLoadFlowContext> {

        private static final String TEST_APP_URL = "TEST_APP_URL";
        private static final String DATABASE = "DATABASE";
        private static final String DB_PORT = "DB_PORT";
        private static final String DB_USER = "DB_USER";
        private static final String DB_PASS = "DB_PASS";
        private static final String USERS = "USERS";
        private static final String DEFECTS = "DEFECTS";
        private static final String LOGINS = "LOGINS";
        private static final String DATABASE_LOGINS = "DATABASE_LOGINS";
        private static final String SPEED_RATE = "SPEED_RATE";
        
        private ConfigureFLDCEMTessLoadFlowContext ctx;

        public Serializer() {
            super(Serializer.class);
        }

        public Serializer(ConfigureFLDCEMTessLoadFlowContext ctx) {
            super(Serializer.class);
            this.ctx = ctx;
        }

        @Override
        public ConfigureFLDCEMTessLoadFlowContext deserialize(String key, Map<String, String> map) {
            Map<String, String> deserializedMap = deserializeMapWithKey(key, map);
            
            ConfigureFLDCEMTessLoadFlowContext.Builder builder = ConfigureFLDCEMTessLoadFlowContext.getBuilder();
            
            builder.testAppUrl(deserializedMap.get(TEST_APP_URL))
            	.database(deserializedMap.get(DATABASE))
                .dbPort(deserializedMap.get(DB_PORT))
                .dbUser(deserializedMap.get(DB_USER))
                .dbPass(deserializedMap.get(DB_PASS))
                .users(Integer.parseInt(deserializedMap.get(USERS)))
                .defects(Integer.parseInt(deserializedMap.get(DEFECTS)))
                .logins(Integer.parseInt(deserializedMap.get(LOGINS)))
                .databaseLogins(Integer.parseInt(deserializedMap.get(DATABASE_LOGINS)))
                .speedRate(Integer.parseInt(deserializedMap.get(SPEED_RATE)));
            
            return builder.build();
        }
        
        @Override
        public Map<String, String> serialize(String key) {
            Map<String, String> recordingParams = new HashMap<>();
            if (ctx != null) {
                if (ctx.testAppUrl != null) {
                	recordingParams.put(TEST_APP_URL, ctx.testAppUrl);	
                }
            	if (ctx.database != null) {
            		recordingParams.put(DATABASE, ctx.database);	
            	}
                if (ctx.dbPort != null) {
                	recordingParams.put(DB_PORT, ctx.dbPort);	
                }
                if (ctx.dbUser != null) {
                    recordingParams.put(DB_USER, ctx.dbUser);    
                }
                if (ctx.dbPass != null) {
                    recordingParams.put(DB_PASS, ctx.dbPass);
                }
                if (ctx.users != null) {
                    recordingParams.put(USERS, ctx.users.toString());
                }
                if (ctx.defects != null) {
                    recordingParams.put(DEFECTS, ctx.defects.toString());
                }
                if (ctx.logins != null) {
                    recordingParams.put(LOGINS, ctx.logins.toString());
                }
                if (ctx.databaseLogins != null) {
                    recordingParams.put(DATABASE_LOGINS, ctx.databaseLogins.toString());
                }
                if (ctx.speedRate != null) {
                    recordingParams.put(SPEED_RATE, ctx.speedRate.toString());
                }
            }
            
            Map<String, String> serializedData = super.serialize(key);
            serializedData.putAll(serializeMapWithKey(key, recordingParams));
            
            return serializedData;
        }
        
    }

}
