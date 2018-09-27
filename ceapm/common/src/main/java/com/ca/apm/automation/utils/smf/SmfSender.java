/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 *
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.automation.utils.smf;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Provides ability to send a raw SMF record to multiple CE-APM Agents on a single host.
 */
public class SmfSender implements AutoCloseable {
    /**
     * Set of open socket connections.
     */
    private Map<Socket, PrintStream> clientSockets = new HashMap<>();

    /**
     * Constructor.
     *
     * @param host Host to connect to.
     * @param ports Ports to connect to.
     * @throws IOException If any of the connections fail.
     */
    public SmfSender(String host, Collection<Integer> ports) throws IOException {
        connect(host, ports);
    }

    /**
     * Initializes client connections.
     *
     * @param host Host to connect to.
     * @param ports Ports to connect to.
     * @throws IOException If any of the connections fail.
     */
    public synchronized void connect(String host, Collection<Integer> ports)
        throws IOException {
        for (int port : ports) {
            Socket socket = new Socket(host, port);
            clientSockets.put(socket, new PrintStream(socket.getOutputStream()));
        }
    }

    /**
     * Disconnects all the active connections held by the instance.
     * Any call made to {@link #send(SmfData)} after disconnecting (and before reconnecting) will
     * have no effect.
     */
    public synchronized void disconnect() {
        for (Map.Entry<Socket, PrintStream> pair : clientSockets.entrySet()) {
            try {
                final Socket socket = pair.getKey();
                final PrintStream os = pair.getValue();
                os.flush();
                os.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientSockets.clear();
    }

    /**
     * Send SMF data to all active connections.
     *
     * @param smf SMF data to send.
     * @throws IOException If sending data over any of the sockets fails.
     */
    public synchronized void send(SmfData smf) throws IOException {
        for (PrintStream os : clientSockets.values()) {
            os.write(smf.getData());
        }
    }

    @Override
    public void close() throws Exception {
        disconnect();
    }
}
