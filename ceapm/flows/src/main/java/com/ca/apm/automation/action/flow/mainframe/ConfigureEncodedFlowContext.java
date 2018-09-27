/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.automation.action.flow.mainframe;

import org.apache.http.util.Args;

import com.ca.apm.automation.action.flow.utility.ConfigureFlowContext;

/**
 * Context for the {@link ConfigureEncodedFlow} flow.
 */
public class ConfigureEncodedFlowContext extends ConfigureFlowContext {

    private final String encoding;

    /**
     * Constructor.
     *
     * @param builder Builder.
     */
    protected ConfigureEncodedFlowContext(Builder builder) {
        super(builder);
        encoding = builder.encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    public static class Builder extends ConfigureFlowContext.Builder {
        private String encoding = "ISO-8859-1";


        /**
         * Set encoding that will be used to read and write the properties from/to files.
         *
         * @param encoding Encoding to use.
         * @return Builder instance the method was called on.
         */
        public Builder encoding(String encoding) {
            Args.notEmpty(encoding, "encoding");
            this.encoding = encoding;
            return builder();
        }

        @Override
        public ConfigureEncodedFlowContext build() {
            return getInstance();
        }

        protected Builder builder() {
            return this;
        }

        protected ConfigureEncodedFlowContext getInstance() {
            return new ConfigureEncodedFlowContext(this);
        }
    }
}
