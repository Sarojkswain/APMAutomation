package com.ca.apm.es;
import java.io.IOException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple ID generator based on Twitter
 * Flake ID. Logic is very simplified here.
 * We can enhance it with third-party or server
 * based IDs later if needed.
 * 
 * Time + Machine/Node + Sequence
 * 
 * Inspired from ES itself.
 * 
 */
public class TraceFlakeIdGenerator {

    private static final SecureRandom javaSecureRandom = new SecureRandom();

    private static final byte[] secureMacAddr = getSecureMacAddress();

    private final AtomicInteger seqNum = new AtomicInteger(javaSecureRandom.nextInt());
    private long lastTimestamp;

    public String getNextTraceId() throws IOException {

        final int nextSeq = seqNum.incrementAndGet() & 0xffffff;
        long timestamp = System.currentTimeMillis();
        synchronized (this) {

            timestamp = Math.max(timestamp, lastTimestamp);
            if (nextSeq == 0) {
                ++timestamp;
            }
            lastTimestamp = timestamp;
        }
        final byte[] uuidBytes = new byte[15];

        // 6 bytes from timestamp into first 6 bytes of uuid
        putLong(uuidBytes, timestamp, 0, 6);

        // 6 bytes from mac
        System.arraycopy(secureMacAddr, 0, uuidBytes, 6, secureMacAddr.length);

        // remaining from sequence
        putLong(uuidBytes, nextSeq, 12, 3);

        // TODO: make sure uuid is full, check String constructor
        byte[] encoded = Base64.getUrlEncoder().encode(uuidBytes);
        return new String(encoded, 0, encoded.length, Charset.forName("UTF-8"));
    }

    private static void putLong(byte[] uuidBytes, long toUse, int fromPos, int numBytes) {
        for (int i = 0; i < numBytes; ++i) {
            uuidBytes[fromPos + numBytes - i - 1] = (byte) (toUse >>> (i * 8));
        }
    }

    @SuppressWarnings("null")
    private static byte[] getSecureMacAddress() {

        byte[] secureMacAddr = null;
        byte[] macAddr = null;

        try {
            Enumeration<NetworkInterface> netInterfaces = NetworkInterface.getNetworkInterfaces();
            if (netInterfaces != null) {

                while (netInterfaces.hasMoreElements()) {

                    NetworkInterface netInterface = netInterfaces.nextElement();
                    if (!netInterface.isLoopback()) {
                        byte[] addr = netInterface.getHardwareAddress();
                        if (isValidMac(addr)) {

                            macAddr = addr;
                            break;
                        }
                    }
                }
            }
        } catch (SocketException e) {

            // cannot get list of interfaces - use dummy
        }
        if (!isValidMac(macAddr)) {

            // random not-real mac
            macAddr = new byte[6];
            javaSecureRandom.nextBytes(macAddr);
            macAddr[0] |= (byte) 0x01;
        }
        secureMacAddr = new byte[6];
        javaSecureRandom.nextBytes(secureMacAddr);
        for (int i = 0; i < 6; ++i) {
            secureMacAddr[i] ^= macAddr[i];
        }

        return secureMacAddr;
    }

    private static boolean isValidMac(byte[] addr) {
        if (addr == null || addr.length != 6) {
            return false;
        }
        for (byte b : addr) {
            if (b != 0x00) {
                return true;
            }
        }
        return false;
    }
}
