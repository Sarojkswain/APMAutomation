package com.ca.apm.es;
import java.util.ArrayList;
import java.util.List;


public class JarvisEsTraceData {

    private JarvisHeader header;
    private List<Object> body;

    public JarvisEsTraceData(JarvisHeader header) {

        this.header = header;
        this.body = new ArrayList<Object>();
    }

    public boolean addData(Object data) {

        body.add(data);
        return false;
    }

    public List<Object> getBody() {
        return body;
    }

    public JarvisHeader getHeader() {
        return header;
    }

}
