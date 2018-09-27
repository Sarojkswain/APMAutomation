/**
 * 
 */
package com.ca.apm.systemtest.fld.test;

import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.role.JDBCQueryLoadRole;
import com.ca.apm.systemtest.fld.test.loads.BaseFldLoadTest;
import com.ca.apm.systemtest.fld.testbed.FLDConstants;
import com.ca.apm.systemtest.fld.testbed.FLDLoadConstants;

/**
 * Starts the jdbc query load in the FLD
 * @author filja01
 *
 */
@Test
public class FLDJDBCQueryLoadTest extends BaseFldLoadTest implements FLDConstants, FLDLoadConstants {
    
    @Override
    protected String getLoadName() {
        return "jdbcquery";
    }

    @Override
    protected void startLoad() {
        runSerializedCommandFlowFromRoleAsync(JDBC_QUERY_ROLE_ID, JDBCQueryLoadRole.JDBCQUERY_START_LOAD, TimeUnit.DAYS, 28);
    }

    @Override
    protected void stopLoad() {
        runSerializedCommandFlowFromRoleAsync(JDBC_QUERY_ROLE_ID, JDBCQueryLoadRole.JDBCQUERY_STOP_LOAD);
    }
}
