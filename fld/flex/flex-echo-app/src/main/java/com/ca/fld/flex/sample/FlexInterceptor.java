package com.ca.fld.flex.sample;

import flex.messaging.messages.AcknowledgeMessageExt;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.MessagePerformanceInfo;
import flex.messaging.messages.MessagePerformanceUtils;
import flex.messaging.messages.RemotingMessage;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.apache.commons.math3.distribution.LogNormalDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.flex.core.MessageProcessingContext;
import org.springframework.flex.core.ResourceHandlingMessageInterceptor;

/**
 * An interceptor that catches all messages and transforms them to our needs
 * <p>
 * In our case it takes whatever outgoing message and if it's a response
 * to "echoService.echo" it replaces it with our own custom body.
 *
 * @author meler02
 */
public class FlexInterceptor implements ResourceHandlingMessageInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(FlexInterceptor.class);

    private static final double medianDelaySecs = 2;
    private static final double sigma = 1;
    private static final LogNormalDistribution logNormalDistribution
        = new LogNormalDistribution(Math.log(medianDelaySecs), sigma);

    private static double delaySecs() {
        double result;
        do {
            result = logNormalDistribution.sample();
        } while (result > 10);
        return result;
    }

    private static void delay() {
        try {
            TimeUnit.NANOSECONDS.sleep((long) (delaySecs() * 1E9));
        } catch (InterruptedException e) {
                /* empty */
        }
    }

    /**
     * @param context
     * @param inputMessage
     * @param outputMessage
     * @param ex
     */
    @Override
    public void afterCompletion(MessageProcessingContext context, Message inputMessage,
        Message outputMessage, Exception ex) {
        logger.info(inputMessage.getMessageId());
    }

    /**
     * @param context
     * @param inputMessage
     * @param outputMessage
     * @return
     */
    @Override
    public Message postProcess(MessageProcessingContext context, Message inputMessage,
        Message outputMessage) {

        if (inputMessage instanceof CommandMessage
            && ((CommandMessage) inputMessage).getOperation()
            == CommandMessage.CLIENT_PING_OPERATION) {
            // passthrough
            delay ();
            return outputMessage;
        }

        if (inputMessage instanceof RemotingMessage
            && "echoService".equals(inputMessage.getDestination())
            && "echo".equals(((RemotingMessage) inputMessage).getOperation())) {
            //
            // Replace OutMessage body
            //
            Message acknowledgeMessage = new AcknowledgeMessageExt();
            String responseBody = "Server received ";
            for (Object parameter : ((RemotingMessage) inputMessage).getParameters()) {
                responseBody += parameter;
            }
            responseBody += " at " + new Date();
            acknowledgeMessage.setBody(responseBody);
            // Create headers DSMPIO and DSMPII
            MessagePerformanceInfo mpio = new MessagePerformanceInfo();
            MessagePerformanceInfo mpii = new MessagePerformanceInfo();
            // todo add data into headers
            acknowledgeMessage.setHeader(MessagePerformanceUtils.MPI_HEADER_OUT, mpio);
            acknowledgeMessage.setHeader(MessagePerformanceUtils.MPI_HEADER_IN, mpii);
            //
            outputMessage.setBody(acknowledgeMessage);

            delay();
        }

        return outputMessage;
    }

    /**
     * @param context
     * @param inputMessage
     * @return
     */
    @Override
    public Message preProcess(MessageProcessingContext context, Message inputMessage) {
        logger.info("got message for '{}' with ID '{}' from client '{}'",
            inputMessage.getDestination(), inputMessage.getMessageId(), inputMessage.getClientId());
        return inputMessage;
    }
}
