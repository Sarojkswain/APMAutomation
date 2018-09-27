/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.logmonitor.config;

import static org.junit.Assert.*;

import org.junit.Test;

import com.ca.apm.systemtest.fld.logmonitor.config.CompoundCondition;
import com.ca.apm.systemtest.fld.logmonitor.config.Condition;
import com.ca.apm.systemtest.fld.logmonitor.config.Operator;
import com.ca.apm.systemtest.fld.logmonitor.config.OrCondition;
import com.ca.apm.systemtest.fld.logmonitor.config.SimpleCondition;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author keyja01
 *
 */
public class ConditionSerializerTest {
    @Test
    public void testConditionSerialization() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        
        SimpleCondition sc = new SimpleCondition(Operator.EndsWith, "1234", true);
        String json = mapper.writeValueAsString(sc);
        Condition c = mapper.readValue(json, Condition.class);
        
        assertEquals(sc, c);
        
        SimpleCondition sc2 = new SimpleCondition(Operator.Regexp, "foo", false);
        CompoundCondition cc = new OrCondition();
        cc.addCondition(sc);
        cc.addCondition(sc2);
        
        json = mapper.writeValueAsString(cc);
        CompoundCondition cc2 = mapper.readValue(json, CompoundCondition.class);
        assertEquals(cc, cc2);
        
        SimpleCondition sc3 = new SimpleCondition(Operator.StartsWith, "q123", true);
        cc2.addCondition(sc3);
        assertNotEquals(cc, cc2);
    }
}
