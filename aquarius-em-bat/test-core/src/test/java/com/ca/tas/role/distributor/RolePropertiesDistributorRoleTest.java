package com.ca.tas.role.distributor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


import java.util.Map.Entry;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.tas.role.DeployFreeRole;
import com.ca.tas.role.IRole;


public class RolePropertiesDistributorRoleTest {

	@Test
	public void testDistributorRole() {
		DeployFreeRole role1 = new DeployFreeRole("role1");
		DeployFreeRole role2 = new DeployFreeRole("role2");
		Collection<IRole> roles = new ArrayList<IRole>(2);
		roles.add(role1);
		roles.add(role2);
		RolePropertiesDistributorRole distrRole = new RolePropertiesDistributorRole.Builder("distributor")
														.addRoleProperty("prop1", "val1")
														.addRoleProperty("prop2", "val2")
														.addRolesToConfigure(roles).build();
		Map<String, String> envProps = new HashMap<String, String>(distrRole.getEnvProperties());
		
		System.out.println("Result:");
		for (Entry<String, String> entry : envProps.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}
		
		Assert.assertNotNull(envProps);
		Assert.assertEquals(envProps.size(), 4);
		Map<String, String> expectedProps = new HashMap<String, String>();
		expectedProps.put("role.role1.prop1", "val1");
		expectedProps.put("role.role1.prop2", "val2");
		expectedProps.put("role.role2.prop1", "val1");
		expectedProps.put("role.role2.prop2", "val2");
		
		for (Entry<String, String> entry : expectedProps.entrySet()) {
			System.out.println("Comparing expected property: " + entry.getKey() + "=" + entry.getValue());
			System.out.println("Existing property: " + entry.getKey() + "=" + envProps.get(entry.getKey()));
			Assert.assertTrue(envProps.containsKey(entry.getKey()));
			Assert.assertEquals(envProps.get(entry.getKey()), entry.getValue());
		}
	}
}
