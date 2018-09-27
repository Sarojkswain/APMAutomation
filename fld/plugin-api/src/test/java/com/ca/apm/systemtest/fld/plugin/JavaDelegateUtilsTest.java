package com.ca.apm.systemtest.fld.plugin;

import org.activiti.engine.delegate.DelegateExecution;
import org.junit.Test;
import org.mockito.Mockito;
import org.testng.Assert;

/**
 * Unit tests for {@link JavaDelegateUtils}.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
public class JavaDelegateUtilsTest {

    @Test
    public void testEnum() {
        TestEnumConfigItem testEnumItem = new TestEnumConfigItem("{1=One,2=Two}");
        testEnumItem.setStringValue("2");
        
        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testEnumItem", Object.class)).thenReturn(testEnumItem);
        
        String retValue = JavaDelegateUtils.getEnumExecutionVariable(executionMock, "testEnumItem");
        System.out.println("Enum value: " + retValue);
        Assert.assertNotNull(retValue);
        Assert.assertEquals(retValue, "2");
    }

    @Test
    public void testString() {
        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testStringItem", String.class)).thenReturn("testString");
        
        String retValue = JavaDelegateUtils.getStringExecutionVariable(executionMock, "testStringItem");
        System.out.println("String value: " + retValue);
        Assert.assertNotNull(retValue);
        Assert.assertEquals(retValue, "testString");
    }

    @Test
    public void testDefaultString() {
        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testStringItem", String.class)).thenReturn("testString");
        
        String retValue = JavaDelegateUtils.getStringExecutionVariable(executionMock, "stringItem", "defaultString!");
        System.out.println("Default String value: " + retValue);
        Assert.assertNotNull(retValue);
        Assert.assertEquals(retValue, "defaultString!");
    }

    @Test
    public void testWrongType() {
        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testStringItem", String.class)).thenReturn("testString");
        
        Boolean retValue = JavaDelegateUtils.getBooleanExecutionVariable(executionMock, "testStringItem");
        System.out.println("Wrong type (Boolean) value: " + retValue);
        
        Assert.assertNull(retValue);
    }
    
    @Test
    public void testStringByProperty() {
        TestLooneyConfigItem testLooneyItem = new TestLooneyConfigItem();
        testLooneyItem.setStringValue("string value 1");
        testLooneyItem.setLooneyProp1("abc");

        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testLooneyItem", Object.class)).thenReturn(testLooneyItem);
        
        String retValue = JavaDelegateUtils.getStringByPropertyName(executionMock, "testLooneyItem", "looneyProp1");
        System.out.println("String-By-Property-Name value: " + retValue);
        Assert.assertNotNull(retValue);
        Assert.assertEquals(retValue, "abc");
    }

    @Test
    public void testStringByGetter() {
        TestLooneyConfigItem testLooneyItem = new TestLooneyConfigItem();
        testLooneyItem.setStringValue("string value 2");
        testLooneyItem.setLooneyPROP2("xyz");

        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testLooneyItem", Object.class)).thenReturn(testLooneyItem);

        String retValue = JavaDelegateUtils.getStringByGetter(executionMock, "testLooneyItem", "getLooneyPROP2");
        System.out.println("String-By-Getter value: " + retValue);
        Assert.assertNotNull(retValue);
        Assert.assertEquals(retValue, "xyz");
    }
    
    @Test
    public void testInteger() {
        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testLongItem", Number.class)).thenReturn(333);
        
        Integer retValue = JavaDelegateUtils.getIntegerExecutionVariable(executionMock, "testLongItem");
        System.out.println("Integer value: " + retValue);
        Assert.assertNotNull(retValue);
        Assert.assertEquals(retValue.intValue(), 333);
    }

    @Test
    public void testBoolean() {
        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testBooleanItem", Boolean.class)).thenReturn(Boolean.TRUE);
        
        Boolean retValue = JavaDelegateUtils.getBooleanExecutionVariable(executionMock, "testBooleanItem");
        System.out.println("Boolean value: " + retValue);
        Assert.assertNotNull(retValue);
        Assert.assertTrue(retValue);
    }

    @Test
    public void testNode() {
        TestNode testNodeItem = new TestNode("test-node-1");
        DelegateExecution executionMock = Mockito.mock(DelegateExecution.class);
        Mockito.when(executionMock.getVariable("testNodeItem", Object.class)).thenReturn(testNodeItem);

        String nodeName = JavaDelegateUtils.getNodeExecutionVariable(executionMock, "testNodeItem");
        System.out.println("Node value: " + nodeName);
        Assert.assertNotNull(nodeName);
        Assert.assertEquals(nodeName, "test-node-1");
    }
    
    public static abstract class TestConfigItem {

        public abstract Object getValue();
        
    }
    
    public static class TestLongConfigItem extends TestConfigItem {
        private Long longValue;

        @Override
        public Object getValue() {
            return longValue;
        }

        /**
         * @return the longValue
         */
        public Long getLongValue() {
            return longValue;
        }

        /**
         * @param longValue the longValue to set
         */
        public void setLongValue(Long longValue) {
            this.longValue = longValue;
        }
    
        
    }

    public static class TestBooleanConfigItem extends TestConfigItem {
        private Boolean booleanValue;

        public TestBooleanConfigItem() {
            
        }
        
        public TestBooleanConfigItem(Boolean booleanValue) {
            this.booleanValue = booleanValue;
        }


        @Override
        public Object getValue() {
            return booleanValue;
        }

    }
    
    public static class TestStringConfigItem extends TestConfigItem {
        private String stringValue;
        
        public String getStringValue() {
            return stringValue;
        }

        public void setStringValue(String stringValue) {
            this.stringValue = stringValue;
        }
        
        @Override
        public Object getValue() {
            return stringValue;
        }

    }
    
    public static class TestCustomConfigItem extends TestStringConfigItem {
        private String customItemType = "TestCustomConfigItem";

        public TestCustomConfigItem() {
            
        }
        
        public TestCustomConfigItem(String customItemType) {
            this.customItemType = customItemType;
        }

        public String getType() {
            return customItemType;
        }
    
    }
    
    public static class TestNode {
        private String name;

        
        public TestNode(String name) {
            this.name = name;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }
        
        
    }
    
    public static class TestEnumConfigItem extends TestCustomConfigItem {
        private String enumValues;
        
        public TestEnumConfigItem(String enumValues) {
            super();
            this.enumValues = enumValues;
        }

        public String getEnumValues() {
            return enumValues;
        }

        public void setEnumValues(String enumValues) {
            this.enumValues = enumValues;
        }

        @Override
        public String getType() {
            return "TestEnumConfigItem";
        }

    }

    public static class TestLooneyConfigItem extends TestCustomConfigItem {
        private String looneyProp1;
        private String looneyPROP2;
        
        /**
         * @return the looneyProp1
         */
        public String getLooneyProp1() {
            return looneyProp1;
        }
        
        /**
         * @param looneyProp1 the looneyProp1 to set
         */
        public void setLooneyProp1(String looneyProp1) {
            this.looneyProp1 = looneyProp1;
        }
        
        /**
         * @return the looneyPROP2
         */
        public String getLooneyPROP2() {
            return looneyPROP2;
        }

        /**
         * @param looneyPROP2 the looneyPROP2 to set
         */
        public void setLooneyPROP2(String looneyPROP2) {
            this.looneyPROP2 = looneyPROP2;
        }
        
        
    }
}
