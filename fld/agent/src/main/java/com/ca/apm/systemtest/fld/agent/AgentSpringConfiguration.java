package com.ca.apm.systemtest.fld.agent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;

import com.ca.apm.systemtest.fld.common.ErrorUtils;
import com.ca.apm.systemtest.fld.common.ProcessUtils2;

/**
 * Created by haiva01 on 5.2.2016.
 */
@Configuration
public class AgentSpringConfiguration {
    @Bean
    TimeMonitorBean timeMonitorBean() {
        return new TimeMonitorBean();
    }

    public class TimeMonitorBean {
        private final NumberFormat numberFormat = new java.text.DecimalFormat("0.00");
        private final Logger log = LoggerFactory.getLogger(TimeMonitorBean.class);

        long offset = 0;
        long delay = 0;
        String ntpHost = "";

        public synchronized long getOffset() {
            return offset;
        }

        public synchronized long getDelay() {
            return delay;
        }

        public synchronized String getNtpHost() {
            return ntpHost;
        }

        public synchronized void setValues(long offset, long delay, String ntpHost) {
            this.offset = offset;
            this.delay = delay;
            this.ntpHost = ntpHost;
        }

        /**
         * Process <code>TimeInfo</code> object and print its details.
         *
         * @param info <code>TimeInfo</code> object.
         */
        public void processResponse(TimeInfo info) {
            NtpV3Packet message = info.getMessage();
            int stratum = message.getStratum();
            String refType;
            if (stratum <= 0) {
                refType = "(Unspecified or Unavailable)";
            } else if (stratum == 1) {
                refType = "(Primary Reference; e.g., GPS)"; // GPS, radio clock, etc.
            } else {
                refType = "(Secondary Reference; e.g. via NTP or SNTP)";
            }
            // stratum should be 0..15...
            log.trace("Stratum: {} {}", stratum, refType);
            int version = message.getVersion();
            int li = message.getLeapIndicator();
            log.trace("leap={}, version={}, precision={}", li, version, message.getPrecision());

            log.trace("mode: {} ({})", message.getModeName(), message.getMode());
            int poll = message.getPoll();
            // poll value typically btwn MINPOLL (4) and MAXPOLL (14)
            log.trace("poll: {} seconds (2 ** {})", poll <= 0 ? 1 : (int) Math.pow(2, poll), poll);
            double disp = message.getRootDispersionInMillisDouble();
            log.trace("rootdelay={}, rootdispersion(ms): {}",
                numberFormat.format(message.getRootDelayInMillisDouble()),
                numberFormat.format(disp));

            int refId = message.getReferenceId();
            String refAddr = NtpUtils.getHostAddress(refId);
            String refName = null;
            if (refId != 0) {
                if (refAddr.equals("127.127.1.0")) {
                    refName = "LOCAL"; // This is the ref address for the Local Clock
                } else if (stratum >= 2) {
                    // If reference id has 127.127 prefix then it uses its own reference clock
                    // defined in the form 127.127.clock-type.unit-num (e.g. 127.127.8.0 mode 5
                    // for GENERIC DCF77 AM; see refclock.htm from the NTP software distribution.
                    if (!refAddr.startsWith("127.127")) {
                        try {
                            InetAddress addr = InetAddress.getByName(refAddr);
                            String name = addr.getHostName();
                            if (name != null && !name.equals(refAddr)) {
                                refName = name;
                            }
                        } catch (UnknownHostException e) {
                            // some stratum-2 servers sync to ref clock device but fudge stratum
                            // level higher... (e.g. 2)
                            // ref not valid host maybe it's a reference clock name?
                            // otherwise just show the ref IP address.
                            refName = NtpUtils.getReferenceClock(message);
                        }
                    }
                } else if (version >= 3 && (stratum == 0 || stratum == 1)) {
                    refName = NtpUtils.getReferenceClock(message);
                    // refname usually have at least 3 characters (e.g. GPS, WWV, LCL, etc.)
                }
                // otherwise give up on naming the beast...
            }
            if (refName != null && refName.length() > 1) {
                refAddr += " (" + refName + ")";
            }
            log.trace("Reference Identifier:\t{}", refAddr);

            TimeStamp refNtpTime = message.getReferenceTimeStamp();
            log.trace("Reference Timestamp:\t{}  {}", refNtpTime, refNtpTime.toDateString());

            // Originate Time is time request sent by client (t1)
            TimeStamp origNtpTime = message.getOriginateTimeStamp();
            log.trace("Originate Timestamp:\t{}  {}", origNtpTime, origNtpTime.toDateString());

            long destTime = info.getReturnTime();
            // Receive Time is time request received by server (t2)
            TimeStamp rcvNtpTime = message.getReceiveTimeStamp();
            log.trace("Receive Timestamp:\t{}  {}", rcvNtpTime, rcvNtpTime.toDateString());

            // Transmit time is time reply sent by server (t3)
            TimeStamp xmitNtpTime = message.getTransmitTimeStamp();
            log.trace("Transmit Timestamp:\t{}  {}", xmitNtpTime, xmitNtpTime.toDateString());

            // Destination time is time reply received by client (t4)
            TimeStamp destNtpTime = TimeStamp.getNtpTime(destTime);
            log.trace("Destination Timestamp:\t{}  {}", destNtpTime, destNtpTime.toDateString());

            info.computeDetails(); // compute offset/delay if not already done
            Long offsetValue = info.getOffset();
            Long delayValue = info.getDelay();
            String delay = (delayValue == null) ? "N/A" : delayValue.toString();
            String offset = (offsetValue == null) ? "N/A" : offsetValue.toString();

            // offset in ms
            log.debug("Roundtrip delay(ms)={}, clock offset(ms)={}", delay, offset);
            setValues(offsetValue == null ? 0 : offsetValue,
                delayValue == null ? 0 : delayValue,
                refAddr);
        }

        @Scheduled(initialDelay = 1000, fixedDelay = 60 * 1000)
        public void timeSynchronizationCheck() {
            NTPUDPClient client = new NTPUDPClient();
            // We want to timeout if a response takes longer than 10 seconds
            client.setDefaultTimeout(10000);
            String hostToTry = null;
            try {
                client.open();
                hostToTry = System.getProperty(
                    "com.ca.apm.systemtest.fld.agent.TimeMonitor.NTPHost");
                if (StringUtils.isBlank(hostToTry)) {
                    // Fallback.
                    hostToTry = "isltime01.ca.com";
                }
                InetAddress hostAddr = InetAddress.getByName(hostToTry);
                log.debug("> {}/{}", hostAddr.getHostName(), hostAddr.getHostAddress());

                TimeInfo info = client.getTime(hostAddr);
                processResponse(info);
            } catch (IOException e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Exception while trying to get time from NTP server {1}. Exception: {0}",
                    hostToTry);
            } finally {
                if (client.isOpen()) {
                    client.close();
                }
            }
        }

        /**
         * Try to start w32time service every hour.
         */
        @Scheduled(initialDelay = 1000, fixedRate = 60 * 60 * 1000)
        public void startW32TimeService() {
            if (! SystemUtils.IS_OS_WINDOWS) {
                return;
            }

            log.info("Trying to start W32Time service.");
            ProcessExecutor pe
                = ProcessUtils2.newProcessExecutor().command("sc", "start", "w32time");
            try {
                StartedProcess startedProcess = pe.start();
                // Wait a bit for the "sc start w32time" command to finish. Ignore the exit code.
                // This is best effort only.
                ProcessUtils2.waitForProcess(startedProcess, 1, TimeUnit.MINUTES, true);
            } catch (IOException e) {
                ErrorUtils.logExceptionFmt(log, e,
                    "Failed to start w32time service. Exception: {0}");
            }
        }
    }
}
