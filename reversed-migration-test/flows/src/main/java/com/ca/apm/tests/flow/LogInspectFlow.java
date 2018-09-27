package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.tas.annotation.TasDocFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Flow
@TasDocFlow(
        description = "Flow inspects given log (text) file for given set of ignores and accepted keywords."
)public class LogInspectFlow extends FlowBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogInspectFlow.class);
    @FlowContext
    private LogInspectFlowContext context;

    public LogInspectFlow() {
    }

    public void run() throws Exception {
        FileReader fileReader = null;
        BufferedReader br = null;

        List<Pattern> ignoredPatterns = new ArrayList<Pattern>();
        for(String ignored: context.getIgnoredRegexps()) {
            ignoredPatterns.add(Pattern.compile(ignored));
        }

        List<Pattern> acceptedPatterns = new ArrayList<Pattern>();
        for(String accepted: context.getAcceptedRegexps()) {
            acceptedPatterns.add(Pattern.compile(accepted));
        }

        try {
            fileReader = new FileReader(context.getLogFile());
            br = new BufferedReader(fileReader);

            for(;;) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                // if line contains ignored keyword, then
                boolean ignoredFound = false;
                for(Pattern ignored: ignoredPatterns) {
                    if (ignored.matcher(line).find()) {
                        ignoredFound = true;
                        LOGGER.info("Found ignore pattern "+ignored+" in file "+context.getLogFile()+", line: "+line);
                        break;
                    }
                }
                if (ignoredFound) {
                    continue;
                }

                for(Pattern accepted: acceptedPatterns) {
                    if (accepted.matcher(line).find()) {
                        LOGGER.info("Found accepted pattern "+accepted+" in file "+context.getLogFile()+", line: "+line);
                        throw new RuntimeException("Pattern "+accepted+" found in "+context.getLogFile()+", line: "+line);
                    }
                }
            }
        } finally {
            if (br!=null) {
                br.close();
            }
            if (fileReader!=null) {
                fileReader.close();
            }
        }
    }
}
