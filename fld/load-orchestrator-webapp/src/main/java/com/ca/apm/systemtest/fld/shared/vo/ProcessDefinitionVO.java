package com.ca.apm.systemtest.fld.shared.vo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ProcessDefinitionVO {

    private String id;
    private String category;
    private String name;
    private String key;
    private String description;
    private Integer version;
    private String resourceName;
    private String deploymentId;
    private String diagramResourceName;
    private String tenantId;
    private Boolean hasStartFormKey;
    private Boolean hasGraphicalNotation;
    private Boolean isSuspended;
    private List<FormPropertyVO> formProperties;

    /**
     * @return the formProperties
     */
    public List<FormPropertyVO> getFormProperties() {
        return formProperties;
    }

    /**
     * @param formProperties the formProperties to set
     */
    public void setFormProperties(List<FormPropertyVO> formProperties) {
        this.formProperties = formProperties;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * @param version the version to set
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

    /**
     * @return the resourceName
     */
    public String getResourceName() {
        return resourceName;
    }

    /**
     * @param resourceName the resourceName to set
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * @return the deploymentId
     */
    public String getDeploymentId() {
        return deploymentId;
    }

    /**
     * @param deploymentId the deploymentId to set
     */
    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * @return the diagramResourceName
     */
    public String getDiagramResourceName() {
        return diagramResourceName;
    }

    /**
     * @param diagramResourceName the diagramResourceName to set
     */
    public void setDiagramResourceName(String diagramResourceName) {
        this.diagramResourceName = diagramResourceName;
    }

    /**
     * @return the tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @param tenantId the tenantId to set
     */
    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    /**
     * @return the hasStartFormKey
     */
    public Boolean getHasStartFormKey() {
        return hasStartFormKey;
    }

    /**
     * @param hasStartFormKey the hasStartFormKey to set
     */
    public void setHasStartFormKey(Boolean hasStartFormKey) {
        this.hasStartFormKey = hasStartFormKey;
    }

    /**
     * @return the hasGraphicalNotation
     */
    public Boolean getHasGraphicalNotation() {
        return hasGraphicalNotation;
    }

    /**
     * @param hasGraphicalNotation the hasGraphicalNotation to set
     */
    public void setHasGraphicalNotation(Boolean hasGraphicalNotation) {
        this.hasGraphicalNotation = hasGraphicalNotation;
    }

    /**
     * @return the isSuspended
     */
    public Boolean getIsSuspended() {
        return isSuspended;
    }

    /**
     * @param isSuspended the isSuspended to set
     */
    public void setIsSuspended(Boolean isSuspended) {
        this.isSuspended = isSuspended;
    }

}
