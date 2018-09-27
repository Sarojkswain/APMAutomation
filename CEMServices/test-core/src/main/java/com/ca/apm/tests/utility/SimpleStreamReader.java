package com.ca.apm.tests.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class SimpleStreamReader implements Runnable {

    private BufferedReader reader;
    private boolean shouldLogAllMessages = false;

    // pattern string of agent log messages. Example for defining multi-pattern
    // based string- ".*FirstPattern.*|.*SecondPattern.*"
    private String agentLogPattern = ".*IntroscopeAgent.*";

    public SimpleStreamReader(InputStream is) {

        this.reader = new BufferedReader(new InputStreamReader(is));
    }

    public SimpleStreamReader(InputStream is, boolean shouldLogAllMessages) {

        this.reader = new BufferedReader(new InputStreamReader(is));
        this.shouldLogAllMessages = shouldLogAllMessages;
    }

    public void run() {

        try {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (shouldLogAllMessages || !line.matches(agentLogPattern)) {
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
