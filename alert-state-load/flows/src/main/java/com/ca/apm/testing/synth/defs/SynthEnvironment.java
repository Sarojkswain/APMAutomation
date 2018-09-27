/**
 * 
 */
package com.ca.apm.testing.synth.defs;

import java.util.HashSet;
import java.util.Set;

/**
 * @author keyja01
 *
 */
public class SynthEnvironment {
    private Set<WebFrontend> webServiceFrontends = new HashSet<>();
    private Set<EjbBackend> ejbBackends = new HashSet<>();

    /**
     * 
     */
    public SynthEnvironment() {
    }

    public Set<WebFrontend> getWebServiceFrontends() {
        return webServiceFrontends;
    }

    public void setWebServiceFrontends(Set<WebFrontend> webServiceFrontends) {
        this.webServiceFrontends = webServiceFrontends;
    }

    public Set<EjbBackend> getEjbBackends() {
        return ejbBackends;
    }

    public void setEjbBackends(Set<EjbBackend> ejbBackends) {
        this.ejbBackends = ejbBackends;
    }
}
