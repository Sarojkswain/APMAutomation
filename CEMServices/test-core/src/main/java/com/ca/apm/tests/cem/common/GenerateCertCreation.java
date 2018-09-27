package com.ca.apm.tests.cem.common;

import java.io.PrintWriter;

public class GenerateCertCreation {
    public static void main(String[] args) {
        String host = "cemload21";
        String[] encryption = {"aes", "camellia", "des", "des3"};
        String[] bits = {"128", "192", "256"};
        String[] bitRate = {"512", "1024", "2048", "4096", "8192"};
        // openssl genrsa -aes128 -passout pass:quality -out aes128512.key 512

        // openssl genrsa -aes128 -passout pass:quality -out jaffa1281024.key 1024
        //
        // openssl req -new -key jaffa1281024.key -out jaffa1281024.csr -passin pass:quality
        // -subj "/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a000.jamsa07-i160826"
        //
        // openssl x509 -req -days 365 -in jaffa1281024.csr -signkey jaffa1281024.key -passin
        // pass:quality -out jaffa1281024.crt
        //
        // openssl rsa -in jaffa1281024.key -passin pass:quality -out jaffa1281024wp.key
        try {
            PrintWriter writer;
            writer = new PrintWriter("C:\\CreateCerts.bat", "UTF-8");
            for (int i = 0; i < 40; i++) {
                if (i < 5) {
                    writer.println("openssl genrsa -" + encryption[0] + bits[0]
                        + " -passout pass:quality -out " + encryption[0] + bits[0] + bitRate[i % 5]
                        + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[0] + bits[0] + bitRate[i % 5]
                            + ".key -out " + encryption[0] + bits[0] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a00"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[0] + bits[0] + bitRate[i
                            % 5] + ".csr -signkey " + encryption[0] + bits[0] + bitRate[i % 5]
                            + ".key " + " -passin pass:quality  -out " + encryption[0] + bits[0]
                            + bitRate[i % 5] + ".crt ");

                    writer.println(
                        "openssl rsa -in " + encryption[0] + bits[0] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[0] + bits[0] + bitRate[i
                            % 5] + "wp.key");
                    writer.println(
                        "openssl rsa -in " + encryption[0] + bits[0] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[0] + bits[0] + bitRate[i
                            % 5] + "wp.pem");
                    writer.println("\n");

                }
                if (i >= 5 && i < 10) {
                    writer.println("openssl genrsa -" + encryption[0] + bits[1]
                        + " -passout pass:quality -out " + encryption[0] + bits[1] + bitRate[i % 5]
                        + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[0] + bits[1] + bitRate[i % 5]
                            + ".key -out " + encryption[0] + bits[1] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a00"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[0] + bits[1] + bitRate[i
                            % 5] + ".csr -signkey " + encryption[0] + bits[1] + bitRate[i % 5]
                            + ".key " + " -passin pass:quality  -out " + encryption[0] + bits[1]
                            + bitRate[i % 5] + ".crt ");

                    writer.println(
                        "openssl rsa -in " + encryption[0] + bits[1] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[0] + bits[1] + bitRate[i
                            % 5] + "wp.key");
                    writer.println(
                        "openssl rsa -in " + encryption[0] + bits[1] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[0] + bits[1] + bitRate[i
                            % 5] + "wp.pem");
                    writer.println("\n");
                }
                if (i >= 10 && i < 15) {
                    writer.println("openssl genrsa -" + encryption[0] + bits[2]
                        + " -passout pass:quality -out " + encryption[0] + bits[2] + bitRate[i % 5]
                        + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[0] + bits[2] + bitRate[i % 5]
                            + ".key -out " + encryption[0] + bits[2] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a0"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[0] + bits[2] + bitRate[i
                            % 5] + ".csr -signkey " + encryption[0] + bits[2] + bitRate[i % 5]
                            + ".key " + " -passin pass:quality  -out " + encryption[0] + bits[2]
                            + bitRate[i % 5] + ".crt ");

                    writer.println(
                        "openssl rsa -in " + encryption[0] + bits[2] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[0] + bits[2] + bitRate[i
                            % 5] + "wp.key");
                    writer.println(
                        "openssl rsa -in " + encryption[0] + bits[2] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[0] + bits[2] + bitRate[i
                            % 5] + "wp.pem");
                    writer.println("\n");
                }
                if (i >= 15 && i < 20) {
                    writer.println("openssl genrsa -" + encryption[1] + bits[0]
                        + " -passout pass:quality -out " + encryption[1] + bits[0] + bitRate[i % 5]
                        + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[1] + bits[0] + bitRate[i % 5]
                            + ".key -out " + encryption[1] + bits[0] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a00"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[1] + bits[0] + bitRate[i
                            % 5] + ".csr -signkey " + encryption[1] + bits[0] + bitRate[i % 5]
                            + ".key " + " -passin pass:quality  -out " + encryption[1] + bits[0]
                            + bitRate[i % 5] + ".crt ");

                    writer.println(
                        "openssl rsa -in " + encryption[1] + bits[0] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[1] + bits[0] + bitRate[i
                            % 5] + "wp.key");
                    writer.println(
                        "openssl rsa -in " + encryption[1] + bits[0] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[1] + bits[0] + bitRate[i
                            % 5] + "wp.pem");
                    writer.println("\n");
                }
                if (i >= 20 && i < 25) {
                    writer.println("openssl genrsa -" + encryption[1] + bits[1]
                        + " -passout pass:quality -out " + encryption[1] + bits[1] + bitRate[i % 5]
                        + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[1] + bits[1] + bitRate[i % 5]
                            + ".key -out " + encryption[1] + bits[1] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a00"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[1] + bits[1] + bitRate[i
                            % 5] + ".csr -signkey " + encryption[1] + bits[1] + bitRate[i % 5]
                            + ".key " + " -passin pass:quality  -out " + encryption[1] + bits[1]
                            + bitRate[i % 5] + ".crt ");

                    writer.println(
                        "openssl rsa -in " + encryption[1] + bits[1] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[1] + bits[1] + bitRate[i
                            % 5] + "wp.key");
                    writer.println(
                        "openssl rsa -in " + encryption[1] + bits[1] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[1] + bits[1] + bitRate[i
                            % 5] + "wp.pem");
                    writer.println("\n");
                }
                if (i >= 25 && i < 30) {
                    writer.println("openssl genrsa -" + encryption[1] + bits[2]
                        + " -passout pass:quality -out " + encryption[1] + bits[2] + bitRate[i % 5]
                        + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[1] + bits[2] + bitRate[i % 5]
                            + ".key -out " + encryption[1] + bits[2] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a0"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[1] + bits[2] + bitRate[i
                            % 5] + ".csr -signkey " + encryption[1] + bits[2] + bitRate[i % 5]
                            + ".key " + " -passin pass:quality  -out " + encryption[1] + bits[2]
                            + bitRate[i % 5] + ".crt ");

                    writer.println(
                        "openssl rsa -in " + encryption[1] + bits[2] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[1] + bits[2] + bitRate[i
                            % 5] + "wp.key");

                    writer.println(
                        "openssl rsa -in " + encryption[1] + bits[2] + bitRate[i % 5] + ".key"
                            + " -passin pass:quality -out " + encryption[1] + bits[2] + bitRate[i
                            % 5] + "wp.pem");

                    writer.println("\n");
                }

                if (i >= 30 && i < 35) {
                    writer.println(
                        "openssl genrsa -" + encryption[2] + " -passout pass:quality -out "
                            + encryption[2] + bitRate[i % 5] + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[2] + bitRate[i % 5] + ".key -out "
                            + encryption[2] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a0"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[2] + bitRate[i % 5]
                            + ".csr -signkey " + encryption[2] + bitRate[i % 5] + ".key "
                            + " -passin pass:quality  -out " + encryption[2] + bitRate[i % 5]
                            + ".crt ");

                    writer.println("openssl rsa -in " + encryption[2] + bitRate[i % 5] + ".key"
                        + " -passin pass:quality -out " + encryption[2] + bitRate[i % 5]
                        + "wp.key");
                    writer.println("openssl rsa -in " + encryption[2] + bitRate[i % 5] + ".key"
                        + " -passin pass:quality -out " + encryption[2] + bitRate[i % 5]
                        + "wp.pem");
                    writer.println("\n");
                }

                if (i >= 35 && i < 40) {
                    writer.println(
                        "openssl genrsa -" + encryption[3] + " -passout pass:quality -out "
                            + encryption[3] + bitRate[i % 5] + ".key " + bitRate[i % 5]);
                    writer.println(
                        "openssl req -new -key " + encryption[3] + bitRate[i % 5] + ".key -out "
                            + encryption[3] + bitRate[i % 5]
                            + ".csr -passin pass:quality -subj \"/C=IN/ST=TS/L=HYD/O=CA/OU=AOM/CN=a0"
                            + i + "." + host + "\"");

                    writer.println(
                        "openssl x509 -req -days 365 -in " + encryption[3] + bitRate[i % 5]
                            + ".csr -signkey " + encryption[3] + bitRate[i % 5] + ".key "
                            + " -passin pass:quality  -out " + encryption[3] + bitRate[i % 5]
                            + ".crt ");

                    writer.println("openssl rsa -in " + encryption[3] + bitRate[i % 5] + ".key"
                        + " -passin pass:quality -out " + encryption[3] + bitRate[i % 5]
                        + "wp.key");
                    writer.println("openssl rsa -in " + encryption[3] + bitRate[i % 5] + ".key"
                        + " -passin pass:quality -out " + encryption[3] + bitRate[i % 5]
                        + "wp.pem");

                    writer.println("\n");
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
