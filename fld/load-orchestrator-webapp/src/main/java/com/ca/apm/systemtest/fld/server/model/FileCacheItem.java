/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * @author KEYJA01
 *
 */
@Entity
@Table(name = "file_cache")
public class FileCacheItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "file_cache_id", nullable = false)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_accessed", nullable = false)
    private Date lastAccessed;

    @Column(name = "url", nullable = false, length = 4096)
    private String url;

    @Column(name = "file_length", nullable = false)
    private long length;

    @Column(name = "filesystem_path", nullable = false, length = 4096)
    private String filesystemPath;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getLastAccessed() {
        return lastAccessed;
    }

    public void setLastAccessed(Date lastAccessed) {
        this.lastAccessed = lastAccessed;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public String getFilesystemPath() {
        return filesystemPath;
    }

    public void setFilesystemPath(String filesystemPath) {
        this.filesystemPath = filesystemPath;
    }
    
    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer("FileCacheItem[");
        sb.append("id=")
            .append(id)
            .append(",lastAccessed=")
            .append(lastAccessed)
            .append(",url=")
            .append(url)
            .append(",length=")
            .append(length)
            .append(",path=")
            .append(filesystemPath)
            ;
        sb.append("]");
        return sb.toString();
    }
}
