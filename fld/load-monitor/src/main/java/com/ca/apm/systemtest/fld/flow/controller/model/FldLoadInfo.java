package com.ca.apm.systemtest.fld.flow.controller.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Persistent information about particular FLD load. 
 * Used to accumulate a bulk update on loads start, stop, failure events.
 * 
 * @author Alexander Sinyushkin (sinal04@ca.com)
 *
 */
@Entity
@Table(name="fld_load_info")
public class FldLoadInfo {

    @EmbeddedId
    private FldLoadInfoId id;
    
    @Column(name = "timestamp", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp;

    @Embeddable
    public static class FldLoadInfoId implements Serializable {
        private static final long serialVersionUID = 4784616569993415325L;

        @Column(name = "load_name", nullable = true)
        private String loadName;
        
        @Column(name = "status", nullable = true)
        private String status;

        public FldLoadInfoId() {
        }

        public FldLoadInfoId(String loadName, String status) {
            this.loadName = loadName;
            this.status = status;
        }

        /**
         * @return the loadName
         */
        public String getLoadName() {
            return loadName;
        }

        /**
         * @return the status
         */
        public String getStatus() {
            return status;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#hashCode()
         */
        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((loadName == null) ? 0 : loadName.hashCode());
            result = prime * result + ((status == null) ? 0 : status.hashCode());
            return result;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#equals(java.lang.Object)
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            FldLoadInfoId other = (FldLoadInfoId) obj;
            if (loadName == null) {
                if (other.loadName != null) return false;
            } else if (!loadName.equals(other.loadName)) return false;
            if (status == null) {
                if (other.status != null) return false;
            } else if (!status.equals(other.status)) return false;
            return true;
        }

        @Override
        public String toString() {
            return "FldLoadInfoId [loadName=" + loadName + ", status=" + status + "]";
        }
        
    }

    
    public FldLoadInfo() {
    }

    public FldLoadInfo(FldLoadInfoId id, Date timestamp) {
        this.id = id;
        this.timestamp = timestamp;
    }

    public FldLoadInfo(String loadName, String status, Date timestamp) {
        this.id = new FldLoadInfoId(loadName, status);
        this.timestamp = timestamp;
    }

    public FldLoadInfoId getId() {
        return id;
    }

    public void setId(FldLoadInfoId id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FldLoadInfo [id=" + id + ", timestamp=" + timestamp + "]";
    }

    
}
