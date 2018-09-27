package com.ca.apm.tests.cem.common;

import java.io.PrintWriter;


public class CurlUrlsScript {

    /**
     * @param args
     */
    public static void main(String[] args) {
        int port = 455;

        String host = "cemload21";
        PrintWriter writer;
        try {
            writer = new PrintWriter("C:\\CreateCurlCommands.sh", "UTF-8");

            for (int i = 0; i < 40; i++) {
                if (i < 10) {
                    System.out.println(
                        "curl --insecure https://a00" + i + "." + host + ":" + (port++)
                            + "/index1.html");
                    writer.println("curl --insecure https://a00" + i + "." + host + ":" + (port++)
                        + "/index1.html");
                } else {
                    System.out.println(
                        "curl --insecure https://a0" + i + "." + host + ":" + (port++)
                            + "/index1.html");
                    writer.println("curl --insecure https://a0" + i + "." + host + ":" + (port++)
                        + "/index1.html");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
