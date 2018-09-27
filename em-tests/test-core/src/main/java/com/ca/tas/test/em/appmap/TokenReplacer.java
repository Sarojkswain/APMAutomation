/*
 * Copyright (c) 2014 CA.  All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */
package com.ca.tas.test.em.appmap;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TokenReplacer {

    private Pattern tokenPattern;

    public TokenReplacer() {
        tokenPattern = Pattern.compile("\\$\\{([^}]+)\\}");
    }

    public String replaceTokens(String text, Properties valuesByKey) {
        if (valuesByKey == null) {
            return text;
        }
        StringBuilder output = new StringBuilder();
        Matcher tokenMatcher = tokenPattern.matcher(text);

        int cursor = 0;
        while (tokenMatcher.find()) {
            // A token is defined as a sequence of the format "${...}".
            // A key is defined as the content between the brackets.
            int tokenStart = tokenMatcher.start();
            int tokenEnd = tokenMatcher.end();
            int keyStart = tokenMatcher.start(1);
            int keyEnd = tokenMatcher.end(1);

            output.append(text.substring(cursor, tokenStart));

            String token = text.substring(tokenStart, tokenEnd);
            String key = text.substring(keyStart, keyEnd);

            if (valuesByKey.containsKey(key)) {
                String value = valuesByKey.getProperty(key);
                output.append(value);
            } else {
                output.append(token);
            }

            cursor = tokenEnd;
        }
        output.append(text.substring(cursor));

        return output.toString();
    }
}