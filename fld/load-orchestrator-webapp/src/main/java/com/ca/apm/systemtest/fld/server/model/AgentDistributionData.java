/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * @author keyja01
 *
 */
@Entity
@Table(name="agent_distributions_data")
public class AgentDistributionData {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="agent_dist_data_id", nullable=false)
    private Long id;
    
    @Basic(fetch=FetchType.LAZY)
    @Lob
    private byte[] zipData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public byte[] getZipData() {
        return zipData;
    }

    public void setZipData(byte[] zipData) {
        this.zipData = zipData;
    }
}
