/**
 * 
 */
package com.ca.apm.systemtest.fld.flow.controller.service;

import com.ca.apm.systemtest.fld.flow.controller.vo.LoadsStatusesVO;

/**
 * @author keyja01
 *
 */
public interface FldControllerService {

    LoadsStatusesVO getLoadStatuses();

    void startLoad(String loadId);

    void forceStartLoad(String loadId);

    void stopLoad(String loadId);

    void forceStopLoad(String loadId);
}
