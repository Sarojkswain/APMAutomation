/**
 * 
 */
package com.ca.apm.systemtest.fld.plugin.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

/**
 * @author keyja01
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemoteCallResult {
    
    public static final String REMOTE_INVOCATION_ID_HEADER = "remoteInvocationId";
    
	protected String callReferenceId;
	protected String processInstanceId;
	@JsonTypeInfo(use=Id.CLASS, property="@type")
	protected Object result;
	protected boolean success;
	protected String errorCode;
	protected String errorMessage;
	
	/**
	 * 
	 */
	public RemoteCallResult() {
	}

	public String getCallReferenceId() {
		return callReferenceId;
	}

	public void setCallReferenceId(String callReferenceId) {
		this.callReferenceId = callReferenceId;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	/**
     * @return the errorMessage
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * @param errorMessage the errorMessage to set
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * @return the processInstanceId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * @param processInstanceId the processInstanceId to set
     */
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    @Override
	public boolean equals(Object o2) {
		if (!(o2 instanceof RemoteCallResult)) {
			return false;
		}
		RemoteCallResult r2 = (RemoteCallResult) o2;
		if (success != r2.success) {
			return false;
		}
		if (!compareStrings(callReferenceId, r2.callReferenceId)) {
			return false;
		}
		if (!compareStrings(processInstanceId, r2.processInstanceId)) {
		    return false;
		}
		if (!compareStrings(errorCode, r2.errorCode)) {
			return false;
		}
		if ((result == null || r2.result == null) && (result != r2.result)) {
			return false;
		} else if (result.getClass().isArray() != r2.result.getClass().isArray()) {
			return false;
		} else {
			if (result.getClass().isArray()) {
				Object[] arr1 = (Object[]) result;
				Object[] arr2 = (Object[]) r2.result;
				if (arr1.length != arr2.length) {
					return false;
				}
				for (int i = 0; i < arr1.length; i++) {
					if (!arr1[i].equals(arr2[i])) {
						return false;
					}
				}
			} else {
				if (!result.equals(r2.result)) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean compareStrings(String s1, String s2) {
        if (s1 == s2)
            return true;
		if (s1 == null || s2 == null)
			return false;
		
		return s1.equals(s2);
	}

	@Override
	public int hashCode() {
		int hc = 0;
		if (callReferenceId != null) {
			hc = callReferenceId.hashCode();
		}
		if (processInstanceId != null) {
		    hc ^= processInstanceId.hashCode();
		}
		if (success) {
			hc ^= 0xFF83A1;
		} else {
			hc ^= 0x103828AF;
		}
		if (errorCode != null) {
			hc ^= errorCode.hashCode();
		}
		if (result != null) {
			if (result.getClass().isArray()) {
				Object[] arr = (Object[]) result;
				int hc2 = 999909999;
				for (Object o: arr) {
					hc2 ^= o.hashCode();
				}
				hc ^= hc2;
			} else {
				hc ^= result.hashCode();
			}
		}
		
		return hc;
	}
	
	/**
	 * Creates an 'Error' result for remote call with error code and description provided.
	 * 
	 * @param errorCode          error code
	 * @param errorMessage       error message
	 * @param callReferenceId    reference id of the remote call request
	 * @param processInstanceId  workflow process instance id
	 * @return                   error remote call result
	 */
	public static RemoteCallResult createErrorResult(String errorCode, String errorMessage, 
	                                                 String callReferenceId, String processInstanceId) {
	    //Make the success status unchangable for error results
	    RemoteCallResult result = new RemoteCallResult() {
	        public void setSuccess(boolean success) {
	        }
	        
	    };
	    result.success = false;
	    result.errorCode = errorCode;
	    result.errorMessage = errorMessage;
	    result.callReferenceId = callReferenceId;
	    result.processInstanceId = processInstanceId;
	    return result;
	}
}
