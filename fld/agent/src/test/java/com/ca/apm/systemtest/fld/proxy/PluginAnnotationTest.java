package com.ca.apm.systemtest.fld.proxy;

import static org.junit.Assert.assertFalse;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.PluginAnnotationComponent;
import com.ca.apm.systemtest.fld.plugin.Plugin;
import com.ca.apm.systemtest.fld.plugin.em.EmPlugin;
import com.ca.apm.systemtest.fld.plugin.memorymonitor.MemoryMonitorPlugin;
import com.ca.apm.systemtest.fld.plugin.pcap.PcapPlugin;
import com.ca.apm.systemtest.fld.plugin.selenium.SeleniumPlugin;
import com.ca.apm.systemtest.fld.plugin.tim.TimPlugin;
import com.ca.apm.systemtest.fld.plugin.wls.WlsPlugin;


/**
 * @author mikpe01
 *
 */
public class PluginAnnotationTest {
	
	private Map<String, Class<? extends Plugin>> pluginsMap;
	private static final Logger logger = LoggerFactory.getLogger(PluginAnnotationTest.class);
	
//	static final String PLUGIN_SELENIUM = SeleniumPlugin.class.getAnnotation(PluginAnnotationComponent.class).pluginType();
//	static final String PLUGIN_TIM = TimPlugin.class.getAnnotation(PluginAnnotationComponent.class).pluginType();
//	static final String PLUGIN_EM = EmPlugin.class.getAnnotation(PluginAnnotationComponent.class).pluginType();
//	static final String PLUGIN_WLS = WlsPlugin.class.getAnnotation(PluginAnnotationComponent.class).pluginType();

	//PluginAnnotationComponent pluginDefault = Plugin.class.getAnnotation(PluginAnnotationComponent.class);		//main Plugin interface
//	static final PluginAnnotationComponent SELENIUM_PAC_IE = IESeleniumPlugin.class.getAnnotation(PluginAnnotationComponent.class);
//	static final PluginAnnotationComponent SELENIUM_PAC_CHROME = ChromeSeleniumPlugin.class.getAnnotation(PluginAnnotationComponent.class);
//	static final PluginAnnotationComponent SELENIUM_PAC_FIREFOX = FirefoxSeleniumPlugin.class.getAnnotation(PluginAnnotationComponent.class);
//	static final PluginAnnotationComponent EM_PAC = EmPluginImpl.class.getAnnotation(PluginAnnotationComponent.class);
//	static final PluginAnnotationComponent TIM_PAC = TimPluginImpl.class.getAnnotation(PluginAnnotationComponent.class);
//	static final PluginAnnotationComponent WLS_PAC = WlsPluginImpl.class.getAnnotation(PluginAnnotationComponent.class);
	
	static final String SELENIUM_PLUGIN_IE = "seleniumPluginIE";
	static final String SELENIUM_PLUGIN_CHROME = "seleniumPluginChrome";
	static final String SELENIUM_PLUGIN_FIREFOX = "seleniumPluginFirefox";
	static final String PLUGIN_TIM = "timPlugin";
	static final String PLUGIN_EM = "emPlugin";
	static final String PLUGIN_WLS = "wlsPlugin";
	static final String PLUGIN_MEM = "memoryMonitorPlugin";
	static final String PLUGIN_PCAP = "pcapPlugin";
	
	
	
	@Ignore
	@Before
	public void initialize() {

		
//		Annotation[] annotations = this.getClass().getAnnotations();
//		for (Annotation annotation : annotations) {
//			if(annotation  instanceof PluginAnnotationComponent) {
//				//logger.info(annotation.annotationType().getTypeName());
//				System.out.println(annotation.annotationType().getTypeName());
//			}
//		}
	}

	@Test
	public void test() {
		
		 pluginsMap = new HashMap<String, Class<? extends Plugin>>();
		
		 Reflections reflections = new Reflections("com.ca.apm.systemtest.fld.plugin");
		 Set<Class<? extends Plugin>> subTypePlugins = reflections.getSubTypesOf(Plugin.class);  
	     //Set<Class<? extends SeleniumPlugin>> subTypeSeleniumPlugins = reflections.getSubTypesOf(SeleniumPlugin.class);
	     //Set<Class<?>> pacPluginsSet = reflections.getTypesAnnotatedWith(PluginAnnotationComponent.class);
	     
	     logger.info("Reflection subTypes scan of '" + Plugin.class.getName() + "' found count: " + subTypePlugins.size());

	     assertFalse(subTypePlugins.isEmpty());
	     
	     for (Class<? extends Plugin> plugin : subTypePlugins) {
	    	    //logger.info("Found plugin ...... " + plugin.getName());
	    	    
				if(isPAC(plugin)){		
					PluginAnnotationComponent currentAnnotatedPlugin = plugin.getAnnotation(PluginAnnotationComponent.class);
					
		    	    //TODO udelat metodu na zjisteni implementace interfacu ?
//					Class<?>[] plugInterfaces = plugin.getInterfaces();
//		    	    for (Class<?> plugI : plugInterfaces) {
//		    	    	//System.out.println(plugI.getName());
//					}

		    	    
		    	    //SELENIUM PLUGINS
		    	    //if(plugin.isInstance(SeleniumPlugin.class)) {
		    	    	
					if (currentAnnotatedPlugin.pluginType().equals(SELENIUM_PLUGIN_IE)) {	//IE
					     pluginsMap.put(SELENIUM_PLUGIN_IE, SeleniumPlugin.class);
					     logger.info("Register plugin IE Selenium: " + SeleniumPlugin.class.getName());
					}
					else if (currentAnnotatedPlugin.pluginType().equals(SELENIUM_PLUGIN_CHROME)) {  //Chrome
						pluginsMap.put(SELENIUM_PLUGIN_CHROME, SeleniumPlugin.class);
					    logger.info("Register plugin CHROME Selenium: " + SeleniumPlugin.class.getName());
					}
					else if (currentAnnotatedPlugin.pluginType().equals(SELENIUM_PLUGIN_FIREFOX)) {	//Firefox
						pluginsMap.put(SELENIUM_PLUGIN_FIREFOX, SeleniumPlugin.class);
					    logger.info("Register plugin FIREFOX Selenium: " + SeleniumPlugin.class.getName());
					}
					
					
					else if (currentAnnotatedPlugin.pluginType().equals(PLUGIN_TIM)) {
						pluginsMap.put(PLUGIN_TIM, TimPlugin.class);
					    logger.info("Register plugin TIM: " + TimPlugin.class.getName());
					}
					else if (currentAnnotatedPlugin.pluginType().equals(PLUGIN_EM)) {
						pluginsMap.put(PLUGIN_EM, EmPlugin.class);
						logger.info("Register plugin EM: " + EmPlugin.class.getName());
					}
					else if (currentAnnotatedPlugin.pluginType().equals(PLUGIN_WLS)) {
						pluginsMap.put(PLUGIN_WLS, WlsPlugin.class);
						logger.info("Register plugin WLS: " + WlsPlugin.class.getName());
					}
                    else if (currentAnnotatedPlugin.pluginType().equals(PLUGIN_MEM)) {
                        pluginsMap.put(PLUGIN_MEM, MemoryMonitorPlugin.class);
                        logger.info("Register plugin WLS: " + MemoryMonitorPlugin.class.getName());
                    } else if (currentAnnotatedPlugin.pluginType().equals(PLUGIN_PCAP)) {
                        pluginsMap.put(PLUGIN_PCAP, PcapPlugin.class);
                        logger.info("Register plugin PCAP: " + PcapPlugin.class.getName());
                    }
    			}
	     	}
	}

	
		/** Check if custom annotation PluginAnnotationComponent exists over class, interface

		 * 
		 * @param plugin class or interface 
		 * @return
		 */
		public static boolean isPAC(Class<? extends Plugin> plugin){
			return plugin.isAnnotationPresent(PluginAnnotationComponent.class);
		}


}
