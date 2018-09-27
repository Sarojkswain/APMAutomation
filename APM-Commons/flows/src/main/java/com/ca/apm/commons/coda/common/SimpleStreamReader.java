package com.ca.apm.commons.coda.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SimpleStreamReader implements Runnable {

    private BufferedReader reader;
    private boolean shouldLogAllMessages = false;

    public SimpleStreamReader(InputStream is) {

        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public SimpleStreamReader(InputStream is, boolean shouldLogAllMessages) {

        this.reader = new BufferedReader(new InputStreamReader(is));
        this.shouldLogAllMessages = shouldLogAllMessages;
    }

    public void run() {

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (shouldLogAllMessages) {
                    System.out.println(line);
                }
            }
            System.out.println("closing stream...");
            reader.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
