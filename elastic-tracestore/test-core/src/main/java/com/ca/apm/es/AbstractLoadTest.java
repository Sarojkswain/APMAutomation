package com.ca.apm.es;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

/**
 * Created by venpr05 on 2/21/2017.
 */
public abstract class AbstractLoadTest {

    int bulkCount;
    int tCount;
    long duration;
    String esHost;
    FakeDataGenerator gen;
    String indexName;
    String mappingResource;
    ObjectMapper mapper;
    RestTemplate restClient;
    GatheringCounter counter;

    public AbstractLoadTest(int bulkCount, int tCount, long duration, String esHost,
        String indexName, String mappingResource) {

        this.bulkCount = bulkCount;
        this.tCount = tCount;
        this.duration = duration;
        this.esHost = esHost;
        this.indexName = indexName;
        this.mappingResource = mappingResource;
        gen = new FakeDataGenerator();
        mapper = new ObjectMapper();
        restClient = new RestTemplate();
        counter = new GatheringCounter();
    }

    public void loadAndStore_SpringRest() throws IOException, InterruptedException {

        final long start = System.currentTimeMillis();

        Runnable r = new Runnable() {
            public void run() {

                long end = start + duration;
                String url = "http://" + esHost + ":9200/" + indexName + "/detail/_bulk";
                HttpHeaders headers = new HttpHeaders();
                headers.set("Accept", "application/json");

                while (System.currentTimeMillis() < end) {
                    try {
                        String payload = createPayload();
                        HttpEntity<String> entity = new HttpEntity<String>(payload, headers);
                        long before = System.currentTimeMillis();
                        restClient.exchange(url, HttpMethod.POST, entity, String.class);
                        long after = System.currentTimeMillis();
                        counter.record(after - before);
                    } catch (Exception e) {
                        System.out.println("Exiting stuff: " + e.getMessage());
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };

        List<Thread> threads = new ArrayList<Thread>();
        for (int i = 0; i < tCount; i++) {
            Thread t = new Thread(r);
            t.start();
            threads.add(t);
        }

        // wait for all
        for (Thread t : threads) {
            t.join();
        }
        System.out.println("Bulk Count: " + bulkCount);
        System.out.println("Thread Count: " + tCount);
        System.out.println("Bulks stored: " + counter.getCount());
        System.out.println("Average time to store one bulk (ms): " + counter.getAvg());
        System.out.println("Min time to store one bulk (ms): " + counter.getMin());
        System.out.println("Max time to store one bulk (ms): " + counter.getMax());
        System.out.println("Total time (ms): " + (System.currentTimeMillis() - start));
        System.out.println("Index Size Stats: " + getIndexStats());

    }

    private String getIndexStats() {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        String response = "";
        try {
            ResponseEntity<String> responseEntity =
                restClient.getForEntity(
                    "http://" + esHost + ":9200/" + indexName + "/_stats/store", String.class);
            response = responseEntity.getBody();
        } catch (Exception e) {
            System.out.println("Something wrong " + e.getMessage());
        }
        return response;
    }

    private String createPayload() throws JsonProcessingException {

        String preDataPayload = "{\"index\":{}}";

        String bulkPayload = "";
        for (int i = 0; i < bulkCount; i++) {

            bulkPayload += preDataPayload;
            bulkPayload += "\n";
            bulkPayload += formSingleDocumentData();
            bulkPayload += "\n";
        }
        return bulkPayload;
    }

    abstract String formSingleDocumentData() throws JsonProcessingException;

    public void createIndex() throws IOException {

        // read mapping, drop index and create it again
        URL resource = Resources.getResource(mappingResource);
        String tData = Resources.toString(resource, Charset.forName("UTF-8"));

        try {
            // TODO: very careful
            restClient.delete("http://" + esHost + ":9200/_all");
        } catch (Exception e) {
            // silent ignore if delete fails
        }
        restClient.put("http://" + esHost + ":9200/" + indexName, tData);
    }
}
