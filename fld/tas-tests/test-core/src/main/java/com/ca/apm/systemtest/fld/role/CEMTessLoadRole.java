package com.ca.apm.systemtest.fld.role;

import com.ca.apm.systemtest.fld.flow.ConfigureFLDCEMTessLoadFlowContext;
import com.ca.tas.builder.BuilderBase;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.resolver.ITasResolver;
import com.ca.tas.role.AbstractRole;

/**
 * Role setup parameters for CEM Tess load. 
 * 
 * @author filja01
 *
 */
public class CEMTessLoadRole extends AbstractRole {
    public static String CEM_TESS_LOAD_FLOW_CTX_KEY = "CEM_TESS_LOAD_FLOW_CTX_KEY";
    
    private ConfigureFLDCEMTessLoadFlowContext flowCtx;

    private CEMTessLoadRole(Builder builder) {
        super(builder.roleId, builder.getEnvProperties());
        this.flowCtx = builder.flowCtx;
    }

	@Override
	public void deploy(IAutomationAgentClient automationAgentClient) {
		;
	}

	public static class Builder extends BuilderBase<Builder, CEMTessLoadRole> {
	    private String roleId;
		private String testAppUrl = "";
        private String database = "flddb01c";
        private String dbPort = "5432";
        private String dbUser = "postgres";
        private String dbPass = "password123";
        private Integer users = 300;
        private Integer defects = 20;
        private Integer logins = 0;
        private Integer databaseLogins = 200;
        private Integer speedRate = 1;
        
        private ConfigureFLDCEMTessLoadFlowContext flowCtx;
        
        public Builder(String roleId, ITasResolver resolver) {
        	super();
        	this.roleId = roleId;
        }

        /**
         * Sets up the role to automatically run the configuration flow during the deploy phase.
         * 
         * @return
         */
        public Builder setTestAppUrl(String testAppUrl) {
            assert testAppUrl != null;
			this.testAppUrl = testAppUrl;
			return this;
		}

		public Builder setDatabase(String database) {
		    assert database != null;
			this.database = database;
			return this;
		}

		public Builder setDbPort(String dbPort) {
		    assert dbPort != null;
			this.dbPort = dbPort;
			return this;
		}

		public Builder setDbUser(String dbUser) {
		    assert dbUser != null;
			this.dbUser = dbUser;
			return this;
		}
		
		public Builder setDbPass(String dbPass) {
		    assert dbPass != null;
            this.dbPass = dbPass;
            return this;
        }
		
		public Builder setUsers(Integer users) {
            assert users != null;
            this.users = users;
            return this;
        }
		
		public Builder setDefects(Integer defects) {
            assert defects != null;
            this.defects = defects;
            return this;
        }
		
		public Builder setLogins(Integer logins) {
            assert logins != null;
            this.logins = logins;
            return this;
        }
		
		public Builder setDatabaseLogins(Integer databaseLogins) {
            assert databaseLogins != null;
            this.databaseLogins = databaseLogins;
            return this;
        }
		
		public Builder setSpeedRate(Integer speedRate) {
            assert speedRate != null;
            this.speedRate = speedRate;
            return this;
        }
		
		@Override
		public CEMTessLoadRole build() {
		    ConfigureFLDCEMTessLoadFlowContext.Builder builder = ConfigureFLDCEMTessLoadFlowContext.getBuilder();
            builder.testAppUrl(testAppUrl)
                .database(database)
                .dbPort(dbPort)
                .dbUser(dbUser)
                .dbPass(dbPass)
                .users(users)
                .defects(defects)
                .logins(logins)
                .databaseLogins(databaseLogins)
                .speedRate(speedRate);
            
            flowCtx = builder.build();
            
            RolePropertyContainer props = getEnvProperties();
            props.add(CEM_TESS_LOAD_FLOW_CTX_KEY, flowCtx);
            return getInstance();
		}

		@Override
		protected Builder builder() {
			return this;
		}

		@Override
		protected CEMTessLoadRole getInstance() {
			return new CEMTessLoadRole(this);
		}
		
	}
}
