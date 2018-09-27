/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides unified access to information on the current operating system family, architecture (32 vs 64-bit), and version. 
 * @author KEYJA01
 *
 */
public class SystemUtil {
    private static Logger log = LoggerFactory.getLogger(SystemUtil.class);

	private OperatingSystemFamily osFamily;
	private OperatingSystemName osName;
	private OperatingSystemArch osArch;
	private JvmArch jvmArch;
	
	/**
	 * Currently supporting only Linux, Windows, MacOS. Support for others (Solaris, SunOS, HP-UX) may be added if necessary
	 * @author KEYJA01
	 */
	public enum OperatingSystemFamily {
		Linux, Windows, MacOS, zOS, Other
	}
	
	public enum OperatingSystemName {
		Centos4, Centos5, Centos6, RHEL4, RHEL5, RHEL6, RHEL7, Windows95, Windows98, WindowsXP, WindowsVista, Windows7, Windows8, WindowsServer2003, WindowsServer2008,
		Other, OtherWindows
	}
	
	public enum OperatingSystemArch {
		Arch32Bit, Arch64Bit, Other
	}
	
	public enum JvmArch {
		Jvm32Bit, Jvm64Bit
	}
	
	private static final SystemUtil instance = new SystemUtil();
	
	private SystemUtil() {
        String value = null;
        
        if (log.isDebugEnabled()) {
            Properties props = System.getProperties();
            for (Entry<Object, Object> entry: props.entrySet()) {
                log.debug(entry.getKey() + " -> " + entry.getValue());
            }
            
            log.debug("System env ------------------------------------");
            Map<String, String> map = System.getenv();
            for (Entry<String, String> entry: map.entrySet()) {
                log.debug(entry.getKey() + " -> " + entry.getValue());
            }
        }
        
        try {
            value = System.getProperty("os.name");
            if (value.startsWith("Windows")) {
                osFamily = OperatingSystemFamily.Windows;
            } else if (value.startsWith("Linux")) {
                osFamily = OperatingSystemFamily.Linux;
            } else if (value.startsWith("Mac OS")) {
                osFamily = OperatingSystemFamily.MacOS;
            } else {
                osFamily = OperatingSystemFamily.Other;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            value = System.getProperty("os.arch");
            if (value.contains("64")) {
                jvmArch = JvmArch.Jvm64Bit;
            } else {
                jvmArch = JvmArch.Jvm32Bit;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            value = System.getenv().get("PROCESSOR_ARCHITECTURE");
            if (value == null) {
                value = System.getProperty("os.arch");
            }
            if (value != null) {
                if (value.contains("64")) {
                    osArch = OperatingSystemArch.Arch64Bit;
                } else {
                    osArch = OperatingSystemArch.Arch32Bit;
                }
            } else {
                osArch = OperatingSystemArch.Other;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            value = System.getProperty("os.version");
            osName = OperatingSystemName.Other;
            if (osFamily == OperatingSystemFamily.Windows) {
                if (value.equals("4.0")) {
                    osName = OperatingSystemName.Windows95;
                } else if (value.equals("4.10")) {
                    osName = OperatingSystemName.Windows98;
                } else if (value.equals("5.1")) {
                    osName = OperatingSystemName.WindowsXP;
                } else if (value.equals("5.2")) {
                    // this should be right
                    osName = OperatingSystemName.WindowsServer2003;
                } else if (value.startsWith("Windows Server 2008")) {
                    osName = OperatingSystemName.WindowsServer2008;
                } else {
                    osName = OperatingSystemName.OtherWindows;
                }
            } else if (osFamily == OperatingSystemFamily.Linux) {
                if (checkRedHat()) {
                    // just L it
                    System.out.println("Found osName == " + osName);
                }
                // TODO check me
            } else if (osFamily == OperatingSystemFamily.MacOS) {
                // TODO complete if we ever run on a MacOS box
            } else {
                osName = OperatingSystemName.Other;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	
	public static OperatingSystemFamily getOsFamily() {
		return instance.osFamily;
	}


	public static OperatingSystemName getOsName() {
		return instance.osName;
	}


	public static OperatingSystemArch getOsArch() {
		return instance.osArch;
	}


	public static JvmArch getJvmArch() {
		return instance.jvmArch;
	}


	private boolean checkRedHat() {
		File f = new File("/etc/centos-release");
		if (f.exists() && f.canRead()) {
		} else {
			f = new File("/etc/redhat-release");
		}
		LineNumberReader reader = null;
		try {
			if (f.exists() && f.canRead()) {
				reader = new LineNumberReader(new FileReader(f));
				String line = null;
				while ((line = reader.readLine()) != null) {
					OperatingSystemName val = null;
					if (line.contains("Maipo")) {
						val = OperatingSystemName.RHEL7;
					} else if (line.contains("Santiago"))  {
						val = OperatingSystemName.RHEL6;
					} else if (line.contains("Tikanga")) {
						val = OperatingSystemName.RHEL5;
					} else if (line.contains("Nahant")) {
						val = OperatingSystemName.RHEL4;
					} else if (line.contains("CentOS release 6")) {
						val = OperatingSystemName.Centos6;
					} else if (line.contains("CentOS release 5")) {
						val = OperatingSystemName.Centos5;
					} else if (line.contains("CentOS release 4")) {
						val = OperatingSystemName.Centos4;
					}
					if (val != null) {
					    osName = val;
						return true;
					}
				}
			}
		} catch (IOException ioe) {
			return false;
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
                    final String msg = MessageFormat.format(
                        "Failed to close reader for {1}. Exception: {0}",
                        e.getMessage(), f.getAbsolutePath());
                    log.error(msg, e);
				}
			}
		}
		
		return false;
	}
	
	
	/**
	 * Method for TESTING only.  Use at your own peril!
	 * @param osFamily
	 * @param jvmArch
	 * @param osArch
	 * @param osName
	 */
	public static void override(OperatingSystemFamily osFamily, JvmArch jvmArch, OperatingSystemArch osArch, OperatingSystemName osName) {
	    instance.osFamily = osFamily;
	    instance.jvmArch = jvmArch;
	    instance.osArch = osArch;
	    instance.osName = osName;
	}
	

	static {
	}
}
