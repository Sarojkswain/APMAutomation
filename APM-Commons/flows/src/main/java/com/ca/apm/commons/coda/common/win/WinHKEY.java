package com.ca.apm.commons.coda.common.win;

import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;


public enum WinHKEY {
    HKEY_CLASSES_ROOT(WinReg.HKEY_CLASSES_ROOT),
    HKEY_CURRENT_USER(WinReg.HKEY_CURRENT_USER),
    HKEY_LOCAL_MACHINE(WinReg.HKEY_LOCAL_MACHINE),
    HKEY_USERS(WinReg.HKEY_USERS),
    HKEY_PERFORMANCE_DATA(WinReg.HKEY_PERFORMANCE_DATA),
    HKEY_PERFORMANCE_TEXT(WinReg.HKEY_PERFORMANCE_TEXT),
    HKEY_PERFORMANCE_NLSTEXT(WinReg.HKEY_PERFORMANCE_NLSTEXT),
    HKEY_CURRENT_CONFIG(WinReg.HKEY_CURRENT_CONFIG),
    HKEY_DYN_DATA(WinReg.HKEY_DYN_DATA);
    
    private HKEY myJnaHKEY;
    
    private WinHKEY(HKEY jnaHKEY) {
        this.myJnaHKEY = jnaHKEY;
    }
    
    protected HKEY getJnaHKEY() {
        return this.myJnaHKEY;
    }
    
}
