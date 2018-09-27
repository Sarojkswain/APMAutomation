package com.ca.apm.commons.coda.common.win;

public interface WindowsRegistry
{

    public String getREG_SZValue(WinHKEY hKey, String regPath, String keyName);

    public byte[] getREG_BINARYValue(WinHKEY hKey, String regPath, String keyName);
    
    public int getREG_DWORDValue(WinHKEY hKey, String regPath, String keyName);
    
    public long getREG_QDWORDValue(WinHKEY hKey, String regPath, String keyName);
    
    public Object getValue(WinHKEY hKey, String regPath, String keyName);
    
    public boolean checkRegKeyExists(WinHKEY hKey, String regPath, String keyName);
    
    public void setREG_SZValue(WinHKEY hKey, String regPath, String keyName, String keyValue);
    
    public void setREG_BINARYValue(WinHKEY hKey, String regPath, String keyName, byte[] data);
    
    public void setREG_DWORDValue(WinHKEY hKey, String regPath, String keyName, int value);
    
    public void setREG_QDWORDValue(WinHKEY hKey, String regPath, String keyName, long value);
    
    public void deleteRegKey(WinHKEY hKey, String regPath, String keyName);
}
