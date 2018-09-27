/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author keyja01
 *
 */
public class TestJsonMapping {
	private ThreadLocal<ObjectMapper> objectMapperTL = new ThreadLocal<ObjectMapper>() {
		protected ObjectMapper initialValue() {
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			return mapper;
		}
	};
	
	@Test
	public void testMapping() throws Exception {
		RemoteCallResult rcr = new RemoteCallResult();
		rcr.setCallReferenceId("12345");
		rcr.setSuccess(true);
		rcr.setErrorCode("9875");
		rcr.setResult(new Attribute("attr", String.class.getName(), true, false));
		
		String json = objectMapperTL.get().writeValueAsString(rcr);
		RemoteCallResult rcr2 = objectMapperTL.get().readValue(json, RemoteCallResult.class);
		
		Assert.assertEquals(rcr, rcr2);
		
		Attribute[] attributes = new Attribute[2];
		attributes[0] = new Attribute("attr", String.class.getName(), true, false);
		attributes[1] = new Attribute("attr2", String.class.getName(), false, true);
		rcr.setResult(attributes);
		
		json = objectMapperTL.get().writeValueAsString(rcr);
		
		rcr2 = objectMapperTL.get().readValue(json, RemoteCallResult.class);
		Assert.assertEquals(rcr, rcr2);
	}
}
