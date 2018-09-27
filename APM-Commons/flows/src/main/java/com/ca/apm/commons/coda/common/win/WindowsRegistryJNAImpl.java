package com.ca.apm.commons.coda.common.win;

import com.sun.jna.platform.win32.Advapi32Util;

class WindowsRegistryJNAImpl implements WindowsRegistry
{

    @Override
    public String getREG_SZValue(WinHKEY hKey, String regPath, String keyName)
    {
        return Advapi32Util.registryGetStringValue(hKey.getJnaHKEY(), regPath, keyName);
    }

    @Override
    public byte[] getREG_BINARYValue(WinHKEY hKey,
                                     String regPath,
                                     String keyName)
    {
        return Advapi32Util.registryGetBinaryValue(hKey.getJnaHKEY(), regPath, keyName);
    }

    @Override
    public int getREG_DWORDValue(WinHKEY hKey, String regPath, String keyName)
    {
        return Advapi32Util.registryGetIntValue(hKey.getJnaHKEY(), regPath, keyName);
    }

    @Override
    public long getREG_QDWORDValue(WinHKEY hKey, String regPath, String keyName)
    {
        return Advapi32Util.registryGetLongValue(hKey.getJnaHKEY(), regPath, keyName);
    }

    @Override
    public Object getValue(WinHKEY hKey, String regPath, String keyName)
    {
        return Advapi32Util.registryGetValue(hKey.getJnaHKEY(), regPath, keyName);
    }

    @Override
    public boolean checkRegKeyExists(WinHKEY hKey,
                                     String regPath,
                                     String keyName)
    {
        return Advapi32Util.registryValueExists(hKey.getJnaHKEY(), regPath, keyName);
    }

    @Override
    public void setREG_SZValue(WinHKEY hKey,
                               String regPath,
                               String keyName,
                               String keyValue)
    {
        Advapi32Util.registrySetStringValue(hKey.getJnaHKEY(), regPath, keyName, keyValue);
    }

    @Override
    public void setREG_BINARYValue(WinHKEY hKey,
                                   String regPath,
                                   String keyName,
                                   byte[] data)
    {
        Advapi32Util.registrySetBinaryValue(hKey.getJnaHKEY(), regPath, keyName, data);
    }

    @Override
    public void setREG_DWORDValue(WinHKEY hKey,
                                  String regPath,
                                  String keyName,
                                  int value)
    {
        Advapi32Util.registrySetIntValue(hKey.getJnaHKEY(), regPath, keyName, value);
    }

    @Override
    public void setREG_QDWORDValue(WinHKEY hKey,
                                   String regPath,
                                   String keyName,
                                   long value)
    {
        Advapi32Util.registrySetLongValue(hKey.getJnaHKEY(), regPath, keyName, value);
    }

    @Override
    public void deleteRegKey(WinHKEY hKey, String regPath, String keyName)
    {
        Advapi32Util.registryDeleteValue(hKey.getJnaHKEY(), regPath, keyName);
    }

    
}
