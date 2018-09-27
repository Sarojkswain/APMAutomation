
package com.ca.apm.systemtest.fld.server.model;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Entity implementation class for Entity: LoggerMonitorValue.
 *
 */
@Entity
@Table(name = "fld_memory_monitor")
public class MemoryMonitorValue implements Serializable {

    private static final long serialVersionUID = -8198468274219103178L;

    @Id
    @Column(name = "id", nullable = false, length = 50)
    private String id;

    @Column(name = "description", nullable = false, length = 4096)
    private String description;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    // , columnDefinition = "BLOB NOT NULL"
    @Column(name = "image")
    private byte[] image;



    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

}
