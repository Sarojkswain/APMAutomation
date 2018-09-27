/**
 * 
 */
package com.ca.apm.systemtest.fld.server.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * @author KEYJA01
 *
 */
@Entity
@Inheritance
@DiscriminatorColumn(name="item_type", length=8)
@Table(name="dashboard_config_item")
public abstract class ConfigItem {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="config_item_id", nullable=false)
	private Long id;
	
	/**
	 * The value of the name attribute in &lt;activiti:formProperty&gt; in the workflow definition
	 */
	@Column(name="name", nullable=false, length=255)
	private String name;
	
	/**
	 * The value of the id attribute in &lt;activiti:formProperty&gt; in the workflow definition
	 */
	@Column(name="form_id", nullable=false, length=128)
	private String formId;
	
	@Column(name="required", nullable=true)
	private Boolean required;

	/**
	 * 
	 */
	public ConfigItem() {
		// TODO Auto-generated constructor stub
	}
	
	public ConfigItem(String name, String formId) {
		this.name = name;
		this.formId = formId;
	}
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getFormId() {
		return formId;
	}

	public void setFormId(String formId) {
		this.formId = formId;
	}

	public Boolean isRequired() {
        return required;
    }

	/**
	 * Says if this configuration parameter must have a non-null value or not.
	 * Calling this method is equivalent to calling {@link ConfigItem#isRequired()}.
	 * 
	 * <p/> 
	 * Do not remove this method! 
	 * Dozer does not copy the value of the <code>required</code> field without this getter.
	 * 
	 * @return <code>true</code> (if required), otherwise <code>false</code>
	 */
	public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object obj) {
        if (!equalsIgnoringValue(obj)) {
            return false;
        }
        
        ConfigItem other = (ConfigItem) obj;
        if (getValue() == null) {
            if (other.getValue() != null) {
                return false;
            }
        } else if (!getValue().equals(other.getValue())) {
            return false;
        }
        
        return true;
    }

    /**
     * Compares two config items ignoring the content of the value field. 
     * 
     * <p/>
     * 
     * When uploading newer versions of the same workflow for detecting any config parameter 
     * changes use rather this method not to lose any previous value changes.  
     * 
     * @param   obj
     * @return <code>true</code> if equals, otherwise <code>false</code>
     * 
     */
    public boolean equalsIgnoringValue(Object obj) {
        if (this == obj) {
            return true;
        }
        
        if (obj == null) { 
            return false;
        }
        
        if (getClass() != obj.getClass()) {
            return false;
        }
        
        ConfigItem other = (ConfigItem) obj;
        if (formId == null) {
            if (other.formId != null) {
                return false;
            }
        } else if (!formId.equals(other.formId)) {
            return false;
        }
        
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        
        if (required == null) {
            if (other.required != null) {
                return false;
            }
        } else if (!required.equals(other.required)) {
            return false;
        }

        //Do not compare values as usually we'd like to leave 
        //any dashboard config parameter value modifications 
        //from the previous version. 
        
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "ConfigItem [id=" + id + ", name=" + name + ", formId=" + formId + ", required="
            + required + "]";
    }

    public abstract Object getValue();
    
    @Transient
    public abstract ConfigItemType getItemType();

}
