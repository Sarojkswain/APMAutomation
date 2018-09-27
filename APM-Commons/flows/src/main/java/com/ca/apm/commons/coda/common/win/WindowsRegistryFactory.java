package com.ca.apm.commons.coda.common.win;


public class WindowsRegistryFactory
{

    private static final WindowsRegistryFactory INSTANCE = new WindowsRegistryFactory();
    
    private WindowsRegistryFactory() {
        
    }
    
    public WindowsRegistry getWindowsRegistryImpl() {
        return new WindowsRegistryJNAImpl();
    }
    
    public static WindowsRegistryFactory getInstance() {
        return INSTANCE;
    }
}
