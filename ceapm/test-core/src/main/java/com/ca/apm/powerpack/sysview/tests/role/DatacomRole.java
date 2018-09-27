/*
 * Copyright (c) 2016 CA. All rights reserved.
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

package com.ca.apm.powerpack.sysview.tests.role;

import java.util.Collections;
import java.util.Map;


/**
 * Datacom role.
 */
public class DatacomRole extends MvsTasksRole {

    protected DatacomRole(Builder builder) {
        super(builder);
    }

    /**
     * Builder holding all properties to instantiate {@link DatacomRole}.
     *
     * <p>By default the built role only verifies the deployment, if you wish to deploy it call the
     * {@link #deployRole()} method.
     */
    public static class Builder extends MvsTasksRole.Builder {

        /**
         * Constructor.
         *
         * @param config Known Datacom configuration.
         */
        public Builder(DatacomConfig config) {
            super(config.getRoleId(), config.getTasks());
            onlyVerify = true;
        }

        /**
         * Do not just verify the role, deploy it.
         *
         * @return Builder instance the method was called on.
         */
        public Builder deployRole() {
            onlyVerify = false;
            return this;
        }
    }

    /**
     * Known Datacom configurations.
     */
    public enum DatacomConfig {
        PATLAMUF("WILYZPOA", "PATLAMUF");

        private static final String DATACOM_ROLE_ID = "datacomRole";
        private final Map<String, String> tasks;

        /**
         * Constructor.
         *
         * @param job Job JCL.
         * @param task Started task name.
         */
        DatacomConfig(String job, String task) {
            tasks = Collections.singletonMap(job, task);
        }

        public String getRoleId() {
            return DATACOM_ROLE_ID;
        }

        public Map<String, String> getTasks() {
            return tasks;
        }

        @Override
        public String toString() {
            return "DATACOM " + name();
        }
    }
}
