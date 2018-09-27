package com.ca.apm.tests.cem.common;

import java.io.PrintWriter;

public class GenerateVirtualHosts {
    public static void main(String[] args) {
        String[] encryption = {"aes", "camellia", "des", "des3"};
        String[] bits = {"128", "192", "256"};
        String[] bitRate = {"512", "1024", "2048", "4096", "8192"};
        int port = 455;
        String host = "cemload21";
        PrintWriter writer;

        try {

            writer = new PrintWriter("C:\\VirtualHostsData.txt", "UTF-8");
            // writer.println("The first line");
            // writer.println("The second line");

            for (int i = 0; i < 40; i++) {
                writer.println("<VirtualHost *:" + port++ + ">");
                writer.println("\n");
                writer.println("DocumentRoot \"C:/Program Files/Apache Software Foundation/Apache2.4/htdocs\"");
                if (i < 10)
                    writer.println("ServerName a00" + i + "." + host);
                else
                    writer.println("ServerName a0" + i + "." + host);

                writer.println("ServerAdmin admin@ca.com");
                writer.println("ErrorLog \"C:/Program Files/Apache Software Foundation/Apache2.4/logs/error1.log\"");
                writer.println("TransferLog \"C:/Program Files/Apache Software Foundation/Apache2.4/logs/access1.log\"");
                writer.println("#   SSL Engine Switch:");
                writer.println("#   Enable/Disable SSL for this virtual host.");
                writer.println("SSLEngine on");

                writer.println("\n");
                writer.println("        #   A self-signed (snakeoil) certificate can be created by installing");
                writer.println("        #   the ssl-cert package. See");
                writer.println("        #   /usr/share/doc/apache2.2-common/README.Debian.gz for more info.");
                writer.println("        #   If both key and certificate are stored in the same file, only the");
                writer.println("        #   SSLCertificateFile directive is needed.");
                if (i < 5) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[0] + bits[0] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[0] + bits[0] + bitRate[i % 5] + "wp.key\"");
                }
                if (i >= 5 && i < 10) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[0] + bits[1] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[0] + bits[1] + bitRate[i % 5] + "wp.key\"");
                }
                if (i >= 10 && i < 15) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[0] + bits[2] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[0] + bits[2] + bitRate[i % 5] + "wp.key\"");
                }
                if (i >= 15 && i < 20) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[1] + bits[0] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[1] + bits[0] + bitRate[i % 5] + "wp.key\"");
                }
                if (i >= 20 && i < 25) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[1] + bits[1] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[1] + bits[1] + bitRate[i % 5] + "wp.key\"");
                }
                if (i >= 25 && i < 30) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[1] + bits[2] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[1] + bits[2] + bitRate[i % 5] + "wp.key\"");
                }

                if (i >= 30 && i < 35) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[2] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[2] + bitRate[i % 5] + "wp.key\"");
                }
                //
                if (i >= 35 && i < 40) {
                    writer.println(
                        "        SSLCertificateFile \"C:/Program Files/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[3] + bitRate[i % 5] + ".crt\"");
                    writer.println(
                        "        SSLCertificateKeyFile \"C:/Progra~1/Apache Software Foundation/Apache2.4/conf/certs/"
                            + encryption[3] + bitRate[i % 5] + "wp.key\"");
                }
                writer.println("</VirtualHost>");
            }
            writer.close();

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
