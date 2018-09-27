package com.ca.apm.es;
import java.util.Collection;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_EMPTY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EsTraceData {

    private EsTraceSummaryData summary;
    Collection<EsTraceComponentData> components;

    public EsTraceData(EsTraceSummaryData summary, Collection<EsTraceComponentData> components) {

        this.summary = summary;
        this.components = components;
    }

    public void setComponents(Collection<EsTraceComponentData> components) {
        this.components = components;
    }

    public void setSummary(EsTraceSummaryData summary) {
        this.summary = summary;
    }

    public Collection<EsTraceComponentData> getComponents() {
        return components;
    }

    public EsTraceSummaryData getSummary() {

        return summary;
    }
}
