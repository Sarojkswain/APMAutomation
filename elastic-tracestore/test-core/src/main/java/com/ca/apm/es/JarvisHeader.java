package com.ca.apm.es;
public class JarvisHeader {

    private String product_id;
    private String tenant_id;
    private String doc_type_id;
    private String doc_type_version;

    public JarvisHeader(String product_id, String tenant_id, String doc_type_id,
        String doc_type_version) {

        this.product_id = product_id;
        this.tenant_id = tenant_id;
        this.doc_type_id = doc_type_id;
        this.doc_type_version = doc_type_version;
    }

    public String getProduct_id() {
        return product_id;
    }

    public String getTenant_id() {
        return tenant_id;
    }

    public String getDoc_type_id() {
        return doc_type_id;
    }

    public String getDoc_type_version() {
        return doc_type_version;
    }

}
