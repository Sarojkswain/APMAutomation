/*
 * Copyright (c) 2017 CA. All rights reserved.
 * 
 * This software and all information contained therein is confidential and
 * proprietary and shall not be duplicated, used, disclosed or disseminated in
 * any way except as authorized by the applicable license agreement, without
 * the express written permission of CA. All authorized reproductions must be
 * marked with this language.
 * 
 * EXCEPT AS SET FORTH IN THE APPLICABLE LICENSE AGREEMENT, TO THE EXTENT
 * PERMITTED BY APPLICABLE LAW, CA PROVIDES THIS SOFTWARE WITHOUT WARRANTY OF
 * ANY KIND, INCLUDING WITHOUT LIMITATION, ANY IMPLIED WARRANTIES OF
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.apm.systemtest.fld.hammond.imp;

import java.util.ArrayList;
import java.util.List;

import com.wily.introscope.spec.server.beans.metricdata.IMetricDataValue;

public class SliceDataValues {

    public static class SliceDataValue {
        
        private IMetricDataValue value;
        private int attribute;
        
        public SliceDataValue(IMetricDataValue value, int attribute) {
            this.value = value;
            this.attribute = attribute;
        }

        public IMetricDataValue getValue() {
            return value;
        }

        public void setValue(IMetricDataValue value) {
            this.value = value;
        }

        public int getAttribute() {
            return attribute;
        }

        public void setAttribute(int attribute) {
            this.attribute = attribute;
        }
        
    }

    private List<SliceDataValue> values;

    public SliceDataValues(List<SliceDataValue> values) {
        this.values = new ArrayList<>(values);
    }

    public List<SliceDataValue> getValues() {
        return values;
    }

    public void setValues(List<SliceDataValue> values) {
        this.values = values;
    }
    
    
}
