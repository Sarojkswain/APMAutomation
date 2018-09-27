package com.ca.tas.role.distributor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import com.ca.apm.automation.action.flow.IBuilder;
import com.ca.tas.client.IAutomationAgentClient;
import com.ca.tas.envproperty.RoleEnvironmentProperties;
import com.ca.tas.property.RolePropertyContainer;
import com.ca.tas.property.TestProperty;
import com.ca.tas.role.AbstractRole;
import com.ca.tas.role.IRole;

/**
 * The <code>RolePropertiesDistributorRole</code> is kind of a fake non-deployable role that
 * can be used to distribute the same role properties to many other roles.
 * 
 * <p/>
 * Example: imagine, you have some following role properties:
 * 
 * propName1=Value1 propName2=Value2
 * 
 * And, say, you have 50 roles which you'd like to have those same two properties:
 * 
 * role1 role2 ... role50
 * 
 * Instead of defining and setting those properties to every single role you could just create one
 * <code>RolePropertiesDistributorRole</code> instance feeding it with those two properties and a
 * collection of the 50 roles you would like to distribute the properties to:
 * 
 * <pre>
 *     IRole role1  = ...;
 *     ...
 *     IRole role50 = ...;
 *     
 *     Collection<IRole> roles = ...;
 *     
 *     roles.add(role1);
 *     ...
 *     roles.add(role2);
 *     
 *     RolePropertiesDistributorRole distributorRole = new RolePropertiesDistributorRole.Builder("role-distributor")
 *                                                         .addRoleProperty("propName1", "Value1")
 *                                                         .addRoleProperty("propName2", "Value2")
 *                                                         .addRolesToConfigure(roles).build();
 *     ITestbedMachine machine = ...;
 *     machine.addRole(role1);
 *     ...
 *     machine.addRole(role50);
 *     machine.addRole(distributorRole);                                                         
 * </pre>
 * 
 * @author sinal04
 * 
 * @copyright 2014 CA Technology, All rights reserved.
 */
public class RolePropertiesDistributorRole extends AbstractRole {
    /**
     * <code>"qcuploadtool.hostname"</code> role property defining the host
     * where the qcuploadtool is deployed.
     */
    public static final String QCUPLOADTOOL_HOSTNAME = "qcuploadtool.hostname";
    /**
     * <code>"qcuploadtool.java.home"</code> role property defining the path
     * to the system java installation for the qcuploadtool to use.
     */
    public static final String QCUPLOADTOOL_JAVA_HOME = "qcuploadtool.java.home";
    /**
     * <code>"qcuploadtool.scp.user"</code> role property defining the
     * ssh user login name for the <code>scp</code> command. TestNG results are
     * collected by copying them from all nodes to the qcupload one using ssh protocol.
     */
    public static final String QCUPLOADTOOL_SCP_USER = "qcuploadtool.scp.user";
    /**
     * <code>"qcuploadtool.scp.password"</code> role property defining the
     * ssh user password for the <code>scp</code> command. TestNG results are
     * collected by copying them from all nodes to the qcupload one using ssh protocol.
     */
    public static final String QCUPLOADTOOL_SCP_PASSWORD = "qcuploadtool.scp.password";
    /**
     * <code>"qcuploadtool.results.dir"</code> role property defining the location
     * on the qcuploadtool node where the result files should be copied to.
     */
    public static final String QCUPLOADTOOL_RESULTS_DIR = "qcuploadtool.results.dir";
    /**
     * <code>"qcuploadtool.upload.results"</code> role property saying whether the node
     * test results should be uploaded to the qcuploadtool or not.
     */
    public static final String QCUPLOADTOOL_UPLOAD_RESULTS = "qcuploadtool.upload.results";

    private final Collection<IRole> rolesToConfigure;

    @NotNull
    private final RolePropertyContainer propertyContainer;

    protected RolePropertiesDistributorRole(@NotNull Builder builder) {
        super(builder.roleId);
        this.rolesToConfigure = builder.rolesToConfigure;
        this.propertyContainer = builder.envPropertyContainer;
    }

    /**
     * Returns configured properties. Please, note, that property names will not contain role ID for
     * this
     * distributor role. Also, the returned result will not contain properties for the distributor
     * role.
     * 
     * @return distributed environment properties
     */
    @Override
    public Map<String, String> getEnvProperties() {
        if (this.rolesToConfigure != null) {
            for (IRole role : this.rolesToConfigure) {
                this.properties.putAll(new RoleEnvironmentProperties(role.getRoleId(),
                    this.propertyContainer.getTestPropertiesAsProperties()));
            }
        }

        return this.properties;
    }

    /**
     * 
     * The <code>Builder</code> should be used to construct
     * <code>RolePropertiesDistributorRole</code> objects.
     * 
     * @author sinal04
     * 
     * @copyright 2014 CA Technology, All rights reserved.
     */
    public static class Builder implements IBuilder<RolePropertiesDistributorRole> {
        private final String roleId;
        private final RolePropertyContainer envPropertyContainer = new RolePropertyContainer();
        private final Collection<IRole> rolesToConfigure = new LinkedList<IRole>();

        /**
         * Constructor.
         * 
         * @param roleId role id
         */
        public Builder(String roleId) {
            this.roleId = roleId;
        }

        /**
         * Provides the collection of roles which we would like to distribute some common properties
         * to.
         * 
         * @param roles roles
         * @return this object
         */
        public Builder addRolesToConfigure(@NotNull Collection<IRole> roles) {
            this.rolesToConfigure.addAll(roles);
            return this;
        }

        /**
         * Adds a property to be distributed.
         * 
         * @param propName property name
         * @param propValue property value
         * @return this object
         */
        public Builder addRoleProperty(@NotNull String propName, @NotNull String propValue) {
            this.envPropertyContainer.add(new TestProperty<>(propName, propValue));
            return this;
        }

        @Override
        public RolePropertiesDistributorRole build() {
            return new RolePropertiesDistributorRole(this);
        }

    }

    @Override
    public void deploy(IAutomationAgentClient aaClient) {
        // TODO Auto-generated method stub

    }

}
