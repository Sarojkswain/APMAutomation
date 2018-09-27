package com.ca.apm.systemtest.fld.server.listener;

import java.beans.PropertyEditorSupport;

public class EventMatcherEditor extends PropertyEditorSupport {
	@Override
	public void setAsText(String text) throws IllegalArgumentException {
		String[] params = text.split("(?<!\\\\):", 7);
		EventMatcher val = new EventMatcher(valOrNull(params, 0), valOrNull(params, 1), valOrNull(params, 2)
				, valOrNull(params, 3), valOrNull(params, 4), valOrNull(params, 5), valOrNull(params, 6));
		setValue(val);
	}

	private String valOrNull(String[] params, int i) {
		if (params.length > i && params[i] != null) {
			return params[i].replaceAll("\\\\:", ":");
		}
		return null;
	}
}