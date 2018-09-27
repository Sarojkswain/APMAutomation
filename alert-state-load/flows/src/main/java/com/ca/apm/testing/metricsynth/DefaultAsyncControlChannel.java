/**
 * 
 */
package com.ca.apm.testing.metricsynth;

import com.wily.EDU.oswego.cs.dl.util.concurrent.Latch;
import com.wily.introscope.spec.metric.AgentMetric;
import com.wily.introscope.spec.server.beans.agent.IAsyncAgentControlChannel;
import com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter;
import com.wily.isengard.messageprimitives.ConnectionException;
import com.wily.isengard.messageprimitives.InvalidIsengardInterface;
import com.wily.isengard.messageprimitives.pipe.AAsyncMessagePipeEndpoint;
import com.wily.isengard.postoffice.PostOffice;

/**
 * @author keyja01
 *
 */
public class DefaultAsyncControlChannel extends AAsyncMessagePipeEndpoint implements IAsyncAgentControlChannel {
    private Latch latch;

    public DefaultAsyncControlChannel(Latch latch, PostOffice po) throws InvalidIsengardInterface {
        super(po, IAsyncAgentControlChannel.class);
        this.latch = latch;
    }

    /* (non-Javadoc)
     * @see com.wily.introscope.spec.server.beans.agent.IAsyncAgentControlChannel#handleAckRefreshConnection()
     */
    @Override
    public void handleAckRefreshConnection() throws ConnectionException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.wily.introscope.spec.server.beans.agent.IAsyncAgentControlChannel#setActualName(java.lang.String)
     */
    @Override
    public void setActualName(String name) throws ConnectionException {
    }

    /* (non-Javadoc)
     * @see com.wily.introscope.spec.server.beans.agent.IAsyncAgentControlChannel#setMetricShutoff(com.wily.introscope.spec.metric.AgentMetric, boolean)
     */
    @Override
    public void setMetricShutoff(AgentMetric arg0, boolean arg1) throws ConnectionException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.wily.introscope.spec.server.beans.agent.IAsyncAgentControlChannel#setReportingState(boolean)
     */
    @Override
    public void setReportingState(boolean arg0) throws ConnectionException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.wily.introscope.spec.server.beans.agent.IAsyncAgentControlChannel#startSendingData()
     */
    @Override
    public void startSendingData() throws ConnectionException {
        latch.release();
    }

    /* (non-Javadoc)
     * @see com.wily.introscope.spec.server.beans.agent.IAsyncAgentControlChannel#updateTTFilter(com.wily.introscope.spec.server.transactiontrace.ITransactionTraceFilter, boolean)
     */
    @Override
    public void updateTTFilter(ITransactionTraceFilter arg0, boolean arg1)
        throws ConnectionException {
        // TODO Auto-generated method stub

    }

}
