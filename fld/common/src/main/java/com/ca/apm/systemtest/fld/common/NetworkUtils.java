package com.ca.apm.systemtest.fld.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkUtils {
    private static Logger log = LoggerFactory.getLogger(NetworkUtils.class);

    /**
     * Gets localhost network address information.
     * 
     * @return localhost inet address 
     * @throws SocketException 
     * @throws Exception
     */
    public static InetAddress getCurrentIndetAddress() throws SocketException {
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface networkInterface = networkInterfaces.nextElement();
            Enumeration<InetAddress> networkInterfaceAddress = networkInterface.getInetAddresses();
            while (networkInterfaceAddress.hasMoreElements()) {
                InetAddress inetAddress = networkInterfaceAddress.nextElement();
                if (!inetAddress.isLinkLocalAddress() && !inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Current IP is {}", inetAddress.getHostAddress());    
                    }
                    return inetAddress;
                }
            }
        }

        String msg = "Could not determine local host IP address.";
        log.error(msg);
        throw new RuntimeException(msg);
    }

    public static boolean isServerListening(String host, int port) {
        Socket s = null;
        try {
            s = new Socket(host, port);
            log.debug("Server {}:{} is listening.", host, port);
            return true;
        } catch (Exception e) {
            log.debug("Server {}:{} is down.", host, port);
            return false;
        } finally {
            if (s != null) {
                try {
                    s.close();
                } catch (Exception e) {
                    ErrorUtils.logExceptionFmt(log, e, "Failed to close socket. Exception: {0}");
                }
            }
        }
    }

}
