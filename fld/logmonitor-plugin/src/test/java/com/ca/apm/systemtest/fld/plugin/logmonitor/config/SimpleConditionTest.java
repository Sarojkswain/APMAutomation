/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.logmonitor.config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ca.apm.systemtest.fld.logmonitor.config.Operator;
import com.ca.apm.systemtest.fld.logmonitor.config.SimpleCondition;


/**
 * @author keyja01
 *
 */
public class SimpleConditionTest {
    @Test
    public void testSimpleCondition() {
        SimpleCondition cond = new SimpleCondition(Operator.Contains, "asdfasdfasdf", true);
        assertTrue(cond.match("***********asdfasdfasdf********"));
        assertTrue(cond.match("***********AsdfAsdfAsdf********"));
        assertFalse(cond.match("fdsafdsafdsa"));
        
        cond = new SimpleCondition(Operator.Contains, "AsdfAsdfAsdf", false);
        assertFalse(cond.match("***********asdfasdfasdf********"));
        assertTrue(cond.match("***********AsdfAsdfAsdf********"));
        assertFalse(cond.match("fdsafdsafdsa"));
        
        cond = new SimpleCondition(Operator.EndsWith, "ending", true);
        assertTrue(cond.match("1234ending"));
        assertTrue(cond.match("1234enDING"));
        assertFalse(cond.match("1234ending!"));
        
        cond = new SimpleCondition(Operator.NotContains, "asdfasdfasdf", true);
        assertFalse(cond.match("***********asdfasdfasdf********"));
        assertFalse(cond.match("***********AsdfAsdfAsdf********"));
        assertTrue(cond.match("***********NotAsdfAsdf********"));
        
        cond = new SimpleCondition(Operator.NotContains, "asdfasdfasdf", false);
        assertFalse(cond.match("***********asdfasdfasdf********"));
        assertTrue(cond.match("***********AsdfAsdfAsdf********"));
        assertTrue(cond.match("***********NotAsdfAsdf********"));
        
        cond = new SimpleCondition(Operator.StartsWith, "start", true);
        assertTrue(cond.match("start**********"));
        assertTrue(cond.match("stART**********"));
        assertFalse(cond.match("notStart**********"));
        
        cond = new SimpleCondition(Operator.StartsWith, "Start", false);
        assertFalse(cond.match("start**********"));
        assertTrue(cond.match("Start**********"));
        assertFalse(cond.match("notStart**********"));
        
        cond = new SimpleCondition(Operator.Regexp, ".*ABC.*", false);
        assertTrue(cond.match("asdfasdfasdfABC"));
        assertTrue(cond.match("**********ABC*"));
        assertFalse(cond.match("asdfasdfasdfA*BC*"));
        assertFalse(cond.match("*****abc*****"));
    }
}
