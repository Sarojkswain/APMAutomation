
package com.ca.apm.systemtest.fld.plugin.em;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.systemtest.fld.common.ErrorUtils;

class Eula {
    private static final Logger log = LoggerFactory.getLogger(Eula.class);

    private Path eulaPath;

    public Eula(Path file) {
        eulaPath = file;
    }

    public Eula acceptEula() {
        List<String> contents;
        try {
            contents = Files.readAllLines(eulaPath, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to read file license file {1}. Exception: {0}.",
                eulaPath.toFile().getAbsoluteFile().toString());
        }

        int lastIdx = contents.size() - 1;
        if (lastIdx >= 0) {
            String lastLine = contents.get(contents.size() - 1);
            String[] tokens = lastLine.split("=");
            if (tokens.length == 2 && "reject".equals(tokens[1])) {
                lastLine = tokens[0] + "=accept";
                contents.set(lastIdx, lastLine);
            }
        }

        try {
            Files.write(eulaPath, contents, StandardCharsets.ISO_8859_1);
        } catch (IOException e) {
            throw ErrorUtils.logExceptionAndWrapFmt(log, e,
                "Failed to write modified license file {1}. Exception: {0}",
                eulaPath.toFile().getAbsoluteFile());
        }

        return this;
    }
}
