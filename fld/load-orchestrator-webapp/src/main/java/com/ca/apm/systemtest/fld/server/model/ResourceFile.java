package com.ca.apm.systemtest.fld.server.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

/**
 * Represents a resource file from BAR
 * @author ZUNPA01
 *
 */
@Entity
@Table(name="resource")
public class ResourceFile implements Serializable {
    private static final long serialVersionUID = 264967815168287201L;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    @Column(name="resource_id", nullable=false)
    private Long id;

    @Column(name="name", nullable=false)
    private String name;

    @Lob
    @Column(name="content", nullable=true)
    private byte[] content;

    public Long getId() {
        return (id);
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return (name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return (content);
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

}
