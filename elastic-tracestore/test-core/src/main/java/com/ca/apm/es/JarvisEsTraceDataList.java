package com.ca.apm.es;
import java.util.Arrays;
import java.util.List;

public class JarvisEsTraceDataList {

    private List<Object> documents;

    public JarvisEsTraceDataList(Object data) {
        documents = Arrays.<Object>asList(data);
    }

    public List<Object> getDocuments() {
        return documents;
    }
}
