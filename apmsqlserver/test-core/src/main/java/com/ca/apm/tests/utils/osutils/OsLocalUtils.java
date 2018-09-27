package com.ca.apm.tests.utils.osutils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.util.Os;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Contains utility methods for use on the same server which runs test (
 * multi-server scenarios are not supported here )
 */
public class OsLocalUtils {
    private static final Logger log = LoggerFactory.getLogger(OsLocalUtils.class);


    private static List<String> runCmd(String cmd) throws Exception {
        List<String> results = new ArrayList<String>();
        log.info("About to run command " + cmd);
        ProcessBuilder pb;
        if (Os.isFamily(Os.FAMILY_WINDOWS)) {
            pb = new ProcessBuilder("cmd.exe", "/C", cmd);
        } else {
            pb = new ProcessBuilder("bash", "-c", cmd);
        }
        pb.redirectErrorStream(true);
        Process subprocess = pb.start();
        InputStream inputStream = subprocess.getInputStream();
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(inputStreamReader);
        String line;
        while ((line = br.readLine()) != null) {
            log.info("" + line);
            results.add(line);
        }

        return results;
    }

    /**
     * This method checks system ports with netstat and returns list of ports with ESTABLISHED
     * status.
     * 
     * @see https://technet.microsoft.com/en-gb/library/bb490947.aspx
     * @see https://tools.ietf.org/html/rfc793
     *      Example of result of netstat command:
     *      Protocol | Local Address | Remote Address | State
     *      TCP 127.0.0.1:4728 127.0.0.1:59196 ESTABLISHED
     * @return
     */
    public static Set<Integer> getEnabledPorts() throws Exception {
        Set<Integer> ports = new HashSet<Integer>();
        String cmd = "";
        cmd =
            Os.isFamily(Os.FAMILY_WINDOWS)
                ? "netstat -np TCP | find \"ESTABLISHED\""
                : "netstat --numeric-ports |grep \"ESTABLISHED\"";
        List<String> results = runCmd(cmd);
        if (results != null && !results.isEmpty()) {
            for (String line : results) {
                int start = line.indexOf(":");
                int end = line.indexOf(" ", start);
                int port = Integer.valueOf(line.substring(start + 1, end));
                ports.add(port);
            }
        }
        return ports;
    }


    /**
     * Method returns current IPv4.
     * This method is useful when you need to get system's net address.
     * On linux systems you can't use getHostAddress out of the box when you don't have properly
     * configured addresses in /etc/hosts - by default it would return 127.0.1.1.
     * 
     */
    public static InetAddress getCurrentIp() throws Exception {

        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) networkInterfaces.nextElement();
            Enumeration<InetAddress> nias = ni.getInetAddresses();
            while (nias.hasMoreElements()) {
                InetAddress ia = (InetAddress) nias.nextElement();
                if (!ia.isLinkLocalAddress() && !ia.isLoopbackAddress()
                    && ia instanceof Inet4Address) {
                    log.info("Current IP is " + ia.getHostAddress());
                    return ia;
                }
            }
        }

        throw new Exception("No IP address found.");
    }


}
