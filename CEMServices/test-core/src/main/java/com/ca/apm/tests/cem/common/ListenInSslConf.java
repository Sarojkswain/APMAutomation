package com.ca.apm.tests.cem.common;

import java.io.PrintWriter;

public class ListenInSslConf {
    public static void main(String[] args) {
        int port = 455;

        PrintWriter writer;

        try {

            writer = new PrintWriter("C:\\ListenToPorts.txt", "UTF-8");
            for (int i = 0; i < 40; i++) {
                writer.println("Listen " + port++);
            }
            writer.close();

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
