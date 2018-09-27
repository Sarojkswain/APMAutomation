/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.powerpack.sysview.tools.smfgenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

class Data {
    public byte[] prefixSmfRecord;
    public byte[] smfRecord;
    public ByteBuffer prefixBuf;
    public ByteBuffer recordBuf;
};

public class SmfRecordGenerator {
    // SMF fields use multiple encodings (typically APM data ASCII, mainframe EBCDIC)
    private static final Charset CP1047 = Charset.forName("Cp1047");
    private static final Charset UTF_8 = Charset.forName("UTF-8");
    private static final byte[] PROPAGATE_TRUE = "1".getBytes(UTF_8);
    private static final Random rand = new Random();
    
    private Map<Socket, PrintStream> clientSockets = new HashMap<Socket, PrintStream>();
    private static final Map<SmfSnippet.Type, Data> DATA = new HashMap<SmfSnippet.Type, Data>();
    
    static {
        for (SmfSnippet.Type type : SmfSnippet.Type.values()) {
            final String prefixRecord;
            final String record;
            Data data = new Data();

            switch (type) {
                case CICS:
                    prefixRecord = "cics-db2.01p.smf";
                    record = "cics-db2.01.smf";
                    break;

                case IMS:
                    prefixRecord = "ims.01p.smf";
                    record = "ims.01.smf";
                    break;
                    
                default:
                    throw new IllegalStateException("Unknown SmfSnippet type");
            }

            File pfile = null;
            File rfile = null;
            try {
                pfile = File.createTempFile(prefixRecord, null);
                rfile = File.createTempFile(record, null);

                FileUtils.copyURLToFile(SmfRecordGenerator.class.getResource(prefixRecord), pfile);
                FileUtils.copyURLToFile(SmfRecordGenerator.class.getResource(record), rfile);

                try (FileInputStream pios = new FileInputStream(pfile.getAbsolutePath());
                    FileInputStream rios = new FileInputStream(rfile.getAbsolutePath())) {
                    // read captured prefix and SMF records
                    data.prefixSmfRecord = IOUtils.toByteArray(pios);
                    data.smfRecord = IOUtils.toByteArray(rios);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (pfile != null) {
                    pfile.delete();
                }
                if (rfile != null) {
                    rfile.delete();
                }
            }

            data.prefixBuf = ByteBuffer.allocate(data.prefixSmfRecord.length);
            data.recordBuf = ByteBuffer.allocate(data.smfRecord.length);
            DATA.put(type,  data);
        }
    }

    public SmfRecordGenerator(String host, Collection<Integer> ports) throws IOException {
        connect(host, ports);
    }
    
    public synchronized void connect(String host, Collection<Integer> ports) throws UnknownHostException, IOException {
        for (int port : ports) {
            Socket socket = new Socket(host, port);
            clientSockets.put(socket, new PrintStream(socket.getOutputStream()));
        }
    }
    
    public void disconnect() {
        try {
            for (Map.Entry<Socket, PrintStream> pair : clientSockets.entrySet()) {
                final Socket socket = pair.getKey();
                final PrintStream os = pair.getValue();
                os.flush();
                os.close();
                socket.close();
            }
            clientSockets.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public synchronized void send(SmfSnippet snippet) throws IOException {
        final Data data = DATA.get(snippet.getType());

        // copy fresh SMF records to work buffers
        data.prefixBuf.position(0);
        data.prefixBuf.put(data.prefixSmfRecord);
        data.recordBuf.position(0);
        data.recordBuf.put(data.smfRecord);
        // modify them
        modify(snippet);
        // send them
        for (PrintStream os : clientSockets.values()) {
            os.write(data.prefixBuf.array());
            os.write(data.recordBuf.array());
        }
    }
    
    protected void modify(SmfSnippet snippet) {
        final Data data = DATA.get(snippet.getType());

        final ByteBuffer p = data.prefixBuf;
        final ByteBuffer r = data.recordBuf;

        long newTime;
        byte[] bytes;

        switch (snippet.getType()) {
            case CICS:
                // Transaction_Lifetime (in microseconds) - add random value
                newTime = p.getLong(0xC8) + snippet.getLifetime();
                p.putLong(0xC8, newTime);
                r.putLong(0x063A, newTime);

                // Job_Name (Server Name) - rotating values
                bytes = snippet.getJobName().getBytes(CP1047);
                putBytes(p, 0x94, bytes);
                putBytes(r, 0x019A, bytes);
                
                // Transaction_Name - change two tailing random decimal characters
                bytes = String.format("CS%02d", rand.nextInt(100)).getBytes(CP1047);
                putBytes(p, 0x9C, bytes);
                putBytes(r, 0x01DA, bytes);
                
                // TODO: The only cics template we have right now has DB2 in it, we would need a
                // TODO: separate template for a DB2-less SMF record
                // DB2 SSID (!These offsets were taken by hex-editing the template!)
                String db2Ssid;
                if (snippet.getDb2Ssid() != null) {
                    db2Ssid = snippet.getDb2Ssid();
                } else {
                    db2Ssid = "XXXX";
                }
                bytes = db2Ssid.substring(0, 4).getBytes(CP1047);
                putBytes(r, 0xED6, bytes);
                putBytes(r, 0xF06, bytes);
                putBytes(r, 0xF36, bytes);
                putBytes(r, 0xF66, bytes);
                putBytes(r, 0xF96, bytes);

                // WILY_CorID_GUID - random string
                bytes =
                    UUID.randomUUID().toString().replace("-", "").substring(0, 32).getBytes(UTF_8);
                putBytes(p, 0x14, bytes);
                putBytes(r, 0x0FCA, bytes);

                // WILY_CorID_Seq - fixed value
                bytes = String.format("%-32s", "1:1").getBytes(UTF_8);
                putBytes(p, 0x34, bytes);
                putBytes(r, 0x0FEA, bytes);

                // WILY_CorID_PropFlag - fixed value
                bytes = PROPAGATE_TRUE;
                putBytes(p, 0x10, bytes);
                putBytes(r, 0x100A, bytes);
                break;

            case IMS:
                // IMTR_CLK_Total, WPTS_Metric_Lifetime
                 newTime = p.getLong(0xC8) + snippet.getLifetime();
                p.putLong(0xC8, newTime);
                r.putLong(0x188, newTime);

                // IMTR_TRN_SubSystem
                final String subsys = snippet.getJobName();
                bytes = subsys.getBytes(CP1047);
                putBytes(r, 0x98, bytes);
                // IMTR_TRN_Jobname
                bytes = String.format("J" + subsys).getBytes(CP1047);
                putBytes(r, 0x9C, bytes);

                // IMTR_TRN_Transaction, WPTS_TranID - change two tailing random decimal characters
                bytes = String.format("GSVIMS%02d", rand.nextInt(100)).getBytes(CP1047);
                putBytes(p, 0x9C, bytes);
                putBytes(r, 0xBC, bytes);

                // based on the sample trace data this was chosen to be truncated UUID
                // IMTR_WILY_MQ_MsgID, WPTS_MQ_MsgID
                bytes = UUID.randomUUID().toString().substring(0, 24).getBytes(UTF_8);
                putBytes(p, 0x14, bytes);
                putBytes(r, 0x464, bytes);
                // IMTR_WILY_MQ_CorrelID, WPTS_MQ_CorrelID
                bytes = UUID.randomUUID().toString().substring(0, 24).getBytes(UTF_8);
                putBytes(p, 0x34, bytes);
                putBytes(r, 0x484, bytes);

                // no sequence ID in IMS record

                // WPTS_PropFlag
                bytes = PROPAGATE_TRUE;
                putBytes(p, 0x10, bytes);

                // IMTR_WILY_MQ_PropFlag, WPTS_MQ_PropFlag
                bytes = PROPAGATE_TRUE;
                putBytes(p, 0x54, bytes);
                putBytes(r, 0x4A4, bytes);
                break;
        }
    }

    private static void putBytes(ByteBuffer bb, int offset, byte[] bytes) {
        bb.position(offset);
        bb.put(bytes);
    }
}