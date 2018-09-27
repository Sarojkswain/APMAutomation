/**
 * 
 */
package com.ca.apm.systemtest.fld.server;

import java.text.MessageFormat;

import org.activiti.engine.ProcessEngine;
import org.apache.commons.dbcp2.BasicDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Place-holder class used to ensure that the process engine is cleanly closed when we shutdow our spring context.
 * @author keyja01
 *
 */
public class ProcessEngineHolder implements DisposableBean {
    private Logger log = LoggerFactory.getLogger(ProcessEngineHolder.class);

	@Autowired
	private ProcessEngine processEngine;
	private boolean closeDatabasePool = false;
	private Object dataSource;

	/**
	 * 
	 */
	public ProcessEngineHolder() {
	}

	/* (non-Javadoc)
	 * @see org.springframework.beans.factory.DisposableBean#destroy()
	 */
	@Override
	public void destroy() throws Exception {
		if (processEngine != null) {
			try {
				processEngine.close();
			} catch (Exception e) {
				// do nothing - just trying to clean up if possible
                final String msg = MessageFormat.format(
                    "Failed to close process engine. Exception: {0}", e.getMessage());
                log.error(msg, e);
            }
		}
		if (closeDatabasePool & dataSource != null && dataSource instanceof BasicDataSource) {
			BasicDataSource src = (BasicDataSource) dataSource;
			try {
				src.close();
			} catch (Exception e) {
                final String msg = MessageFormat.format(
                    "Failed to close data source. Exception: {0}", e.getMessage());
                log.error(msg, e);
            }
		}
	}

	public boolean isCloseDatabasePool() {
		return closeDatabasePool;
	}

	public void setCloseDatabasePool(boolean closeDatabasePool) {
		this.closeDatabasePool = closeDatabasePool;
	}
	
	public void setDataSource(Object dataSource) {
		this.dataSource = dataSource;
	}
}
