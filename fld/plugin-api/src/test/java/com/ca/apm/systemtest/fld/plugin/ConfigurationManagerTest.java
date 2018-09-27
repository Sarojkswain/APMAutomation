/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ca.apm.systemtest.fld.plugin.cm.ConfigurationManagerImpl;

/**
 * @author keyja01
 *
 */
public class ConfigurationManagerTest {

    private static final String TESTPLUGIN = "testplugin";
    private ConfigurationManagerImpl cm;
    private Path tmp;
    
    /**
     * 
     */
    public ConfigurationManagerTest() {
    }
    
    
    @BeforeMethod
    public void setup() throws Exception {
        cm = new ConfigurationManagerImpl();
        tmp = Files.createTempDirectory("cmtest");
        cm.setConfigFilePath(tmp.toString());
    }

    
    @Test
    public void testConfigurationManager() throws Exception {
        TestConfigurationA cfg = new TestConfigurationA();
        cm.savePluginConfiguration(TESTPLUGIN, cfg);
        
        // check that the file was created
        Path configFile = tmp.resolve(TESTPLUGIN + ".conf.json");
        Assert.assertTrue(Files.exists(configFile), TESTPLUGIN + ".conf.json must exist");
        
        // check that it can be read
        cfg = cm.loadPluginConfiguration(TESTPLUGIN, TestConfigurationA.class);
        Assert.assertNotNull(cfg);
        
        // check that it is autocreated when necessary
        Files.delete(configFile);
        cfg = cm.loadPluginConfiguration(TESTPLUGIN, TestConfigurationA.class);
        Assert.assertNotNull(cfg, "Automatically created test config cannot be null");
        
        // check that values are saved, handling nulls gracefully
        cfg.setList(new ArrayList<EmbeddedClass>());
        cfg.getList().add(new EmbeddedClass());
        cfg.getList().add(new EmbeddedClass(null, new int[] {9, 8, 7, 6}));
        cfg.getList().add(new EmbeddedClass("foo foo foo", null));
        cm.savePluginConfiguration(TESTPLUGIN, cfg);
        cfg = cm.loadPluginConfiguration(TESTPLUGIN, TestConfigurationA.class);
        Assert.assertNotNull(cfg);
        Assert.assertNotNull(cfg.getList());
        Assert.assertEquals(cfg.getList().size(), 3);
        
        EmbeddedClass[] arr = cfg.getList().toArray(new EmbeddedClass[3]);
        Assert.assertNull(arr[0].foo);
        Assert.assertNull(arr[0].numbers);
        Assert.assertNull(arr[1].foo);
        Assert.assertEquals(new int[] {9, 8, 7, 6}, arr[1].numbers);
        Assert.assertEquals("foo foo foo", arr[2].foo);
        Assert.assertNull(arr[0].numbers);
    }
    
    
    @AfterMethod
    public void after() {
        if (tmp != null) {
            try {
                Files.delete(tmp);
            } catch (Exception e) {
                // at least we tried
            }
        }
        tmp = null;
    }

    /**
     * Just used for testing the configuration manager
     * @author keyja01
     *
     */
    public static class TestConfigurationA implements PluginConfiguration {
        private List<EmbeddedClass> list;

        public List<EmbeddedClass> getList() {
            return list;
        }

        public void setList(List<EmbeddedClass> list) {
            this.list = list;
        }
    }
    
    public static class EmbeddedClass {
        private String foo;
        private int[] numbers;
        
        public EmbeddedClass(String foo, int[] numbers) {
            this.foo = foo;
            this.numbers = numbers;
        }

        public EmbeddedClass() {
        }

        public String getFoo() {
            return foo;
        }
        
        public void setFoo(String foo) {
            this.foo = foo;
        }
        
        public int[] getNumbers() {
            return numbers;
        }
        
        public void setNumbers(int[] numbers) {
            this.numbers = numbers;
        }
    }

}
