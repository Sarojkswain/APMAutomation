/**
 * 
 */
package com.ca.apm.testing.metricsynth;

import java.util.ArrayList;
import java.util.List;

import com.wily.introscope.spec.metric.AgentMetricData;
import com.wily.introscope.spec.metric.BadlyFormedNameException;
import com.wily.introscope.spec.metric.CompressingAgentMetricDataSet2;
import com.wily.introscope.spec.metric.Frequency;
import com.wily.introscope.spec.metric.Metric;
import com.wily.introscope.spec.metric.MetricTypes;
import com.wily.introscope.spec.server.beans.agent.ICompressedTimesliceData;
import com.wily.introscope.spec.server.beans.metricdata.IMetricDataValue;
import com.wily.introscope.stat.timeslice.StringTimeslicedValue;

/**
 * @author keyja01
 *
 */
public class DefaultMetricDataFactory implements MetricFactory {


    /* (non-Javadoc)
     * @see com.ca.apm.testing.fakeagent.MetricFactory#generateMetricData()
     */
    @Override
    public AgentMetricData[] generateMetricData() throws BadlyFormedNameException {
        AgentMetricData[] data = new AgentMetricData[1];
        
        Metric m = Metric.getMetric("SuperDomain|keyja01|AndyProcess|AndyAgent|Foo|Bar:String Constant Metric", MetricTypes.kStringConstant);
        List<IMetricDataValue> list = new ArrayList<>();
        StringTimeslicedValue sm = new StringTimeslicedValue(MetricTypes.kStringConstant, 0, 0, null, "This is a string value");
        list.add(sm);
        
        data[0] = new AgentMetricData(m.getAgentMetric(), Frequency.kDefaultAgentFrequency, sm);
        
        return data;
    }

    @Override
    public ICompressedTimesliceData generateCompressedMetricData() throws BadlyFormedNameException {
        return new CompressingAgentMetricDataSet2(generateMetricData());
    }

    @Override
    public void setActiveProfile(String profileName) {
        // not used in this implementation
    }

}
