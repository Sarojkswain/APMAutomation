/**
 * 
 */
package com.ca.apm.systemtest.fld.workflow;

import org.activiti.engine.form.AbstractFormType;

/**
 * Custom form type used to validate usage of non-standard form types in the FLD workflows
 * @author KEYJA01
 *
 */
@SuppressWarnings("serial")
public class CustomFormType extends AbstractFormType {

	/**
	 * 
	 */
	public CustomFormType() {
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.form.FormType#getName()
	 */
	@Override
	public String getName() {
		return "custom";
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.form.AbstractFormType#convertFormValueToModelValue(java.lang.String)
	 */
	@Override
	public Object convertFormValueToModelValue(String formValue) {
		return formValue;
	}

	/* (non-Javadoc)
	 * @see org.activiti.engine.form.AbstractFormType#convertModelValueToFormValue(java.lang.Object)
	 */
	@Override
	public String convertModelValueToFormValue(Object modelValue) {
		if (modelValue == null) {
			return null;
		}
		return modelValue.toString();
	}

}
