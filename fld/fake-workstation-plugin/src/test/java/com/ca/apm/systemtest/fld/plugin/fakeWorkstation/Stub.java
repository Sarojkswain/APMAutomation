/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.fakeWorkstation;

/**
 * Simple stub fake workstation class.  Used in generated fakeWorkstation.jar to avoid downloading
 * the actual fakeWorkstation
 * @author keyja01
 *
 */
public class Stub {
    public static void main(String[] args) {
        for (String s: args) {
            System.out.println("Argument: " + s);
        }
        Object obj = new Object();
        synchronized (obj) {
            try {
                obj.wait(1500L);
            } catch (Exception e) {
                
            }
        }
        System.out.println("done");
    }
}
