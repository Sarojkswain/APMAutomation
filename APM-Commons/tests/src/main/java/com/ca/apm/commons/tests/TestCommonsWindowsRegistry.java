package com.ca.apm.commons.tests;

import org.testng.Assert;
import org.testng.annotations.Test;

import com.ca.apm.automation.action.flow.em.DeployEMFlowContext;
import com.ca.apm.commons.coda.common.win.WinHKEY;
import com.ca.apm.commons.coda.common.win.WindowsRegistry;
import com.ca.apm.commons.coda.common.win.WindowsRegistryFactory;
import com.ca.apm.commons.testbed.CommonsWindowsTestbed;
import com.ca.tas.test.TasTestNgTest;
import com.ca.tas.tests.annotations.Tas;
import com.ca.tas.tests.annotations.TestBed;
import com.ca.tas.type.SizeType;

public class TestCommonsWindowsRegistry extends TasTestNgTest
{
    private final String emRoleId; 
    private final String emConfigDir;   
    
    public TestCommonsWindowsRegistry() {
        emRoleId = CommonsWindowsTestbed.EM_ROLE_ID;    
        emConfigDir = envProperties.getRolePropertyById(emRoleId, DeployEMFlowContext.ENV_EM_CONFIG_DIR);  
      }

    @Tas(testBeds = @TestBed(name = CommonsWindowsTestbed.class, executeOn = CommonsWindowsTestbed.EM_MACHINE_ID), size = SizeType.COLOSSAL, owner = "balra06")
    @Test
    public void windowsRegistryTest() 
    {
        WindowsRegistry winReg = WindowsRegistryFactory.getInstance().getWindowsRegistryImpl();
        Assert.assertNotNull(winReg);
       
        // creates a registry key with a DWORD value , checks for its existence and deleted it from the registry
        winReg.setREG_DWORDValue(WinHKEY.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Services\\Tcpip\\Parameters\\", "TcpTimedWaitDelay", Integer.parseInt("00000258"));
        Assert.assertTrue(winReg.checkRegKeyExists(WinHKEY.HKEY_LOCAL_MACHINE,"System\\CurrentControlSet\\Services\\Tcpip\\Parameters\\","TcpTimedWaitDelay"));
        Assert.assertEquals(winReg.getREG_DWORDValue(WinHKEY.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Services\\Tcpip\\Parameters\\", "TcpTimedWaitDelay"), Integer.parseInt("00000258"));
        winReg.deleteRegKey(WinHKEY.HKEY_LOCAL_MACHINE, "System\\CurrentControlSet\\Services\\Tcpip\\Parameters\\", "TcpTimedWaitDelay");
        Assert.assertFalse(winReg.checkRegKeyExists(WinHKEY.HKEY_LOCAL_MACHINE,"System\\CurrentControlSet\\Services\\Tcpip\\Parameters\\","TcpTimedWaitDelay"));
                
        // creates a registry key with a QDWORD value, checks for its existence and deleted it from the registry
        winReg.setREG_QDWORDValue(WinHKEY.HKEY_CURRENT_CONFIG, "System\\CurrentControlSet\\SERVICES\\VGASAVE\\","QDWordRegistry", Long.parseLong("10000000030030399"));
        Assert.assertTrue(winReg.checkRegKeyExists(WinHKEY.HKEY_CURRENT_CONFIG,"System\\CurrentControlSet\\SERVICES\\VGASAVE\\","QDWordRegistry"));
        Assert.assertEquals(winReg.getREG_QDWORDValue(WinHKEY.HKEY_CURRENT_CONFIG, "System\\CurrentControlSet\\SERVICES\\VGASAVE\\", "QDWordRegistry"), Long.parseLong("10000000030030399"));
		winReg.deleteRegKey(WinHKEY.HKEY_CURRENT_CONFIG, "System\\CurrentControlSet\\SERVICES\\VGASAVE\\", "QDWordRegistry");
		Assert.assertFalse(winReg.checkRegKeyExists(WinHKEY.HKEY_CURRENT_CONFIG,"System\\CurrentControlSet\\SERVICES\\VGASAVE\\","QDWordRegistry"));
		
		// creates a registry key with a Binary value, checks for its existence and deleted it from the registry
		byte[] data = {(byte) 805101000};
		winReg.setREG_BINARYValue(WinHKEY.HKEY_USERS, ".DEFAULT\\SYSTEM\\CurrentControlSet\\Control\\NetTrace\\", "BinaryValueRegistry", data);
		Assert.assertTrue(winReg.checkRegKeyExists(WinHKEY.HKEY_USERS,".DEFAULT\\SYSTEM\\CurrentControlSet\\Control\\NetTrace\\","BinaryValueRegistry"));
		Assert.assertEquals(winReg.getREG_BINARYValue(WinHKEY.HKEY_USERS, ".DEFAULT\\SYSTEM\\CurrentControlSet\\Control\\NetTrace\\", "BinaryValueRegistry"), data);
		winReg.deleteRegKey(WinHKEY.HKEY_USERS, ".DEFAULT\\SYSTEM\\CurrentControlSet\\Control\\NetTrace\\", "BinaryValueRegistry");
		Assert.assertFalse(winReg.checkRegKeyExists(WinHKEY.HKEY_USERS,".DEFAULT\\SYSTEM\\CurrentControlSet\\Control\\NetTrace\\","BinaryValueRegistry"));
		
		// creates a registry key with a SZ value, checks for its existence and deleted it from the registry
		winReg.setREG_SZValue(WinHKEY.HKEY_CLASSES_ROOT, "Applications\\cmd.exe\\", "SZValueRegistry", "command port");
        Assert.assertTrue(winReg.checkRegKeyExists(WinHKEY.HKEY_CLASSES_ROOT,"Applications\\cmd.exe\\","SZValueRegistry"));
        Assert.assertEquals(winReg.getREG_SZValue(WinHKEY.HKEY_CLASSES_ROOT, "Applications\\cmd.exe\\", "SZValueRegistry"), "command port");
        winReg.deleteRegKey(WinHKEY.HKEY_CLASSES_ROOT, "Applications\\cmd.exe\\", "SZValueRegistry");
        Assert.assertFalse(winReg.checkRegKeyExists(WinHKEY.HKEY_CLASSES_ROOT,"Applications\\cmd.exe\\","SZValueRegistry"));
        
        //gets the value of a registry key 
        Object value = winReg.getValue(WinHKEY.HKEY_CURRENT_USER, "Control Panel\\Input Method\\", "Show Status");
        System.out.println("value of the Show Status key from the getValue method is " + value);
        
    }
          
 }
