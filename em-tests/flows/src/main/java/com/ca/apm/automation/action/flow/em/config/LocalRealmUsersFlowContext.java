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
package com.ca.apm.automation.action.flow.em.config;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.BuilderBase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Flow context to modify users.xml file
 *
 * Can only add new users or modify passwords for users
 * Can only add users to groups or modify their permissions
 * Can add new groups
 *
 * Does not supporting removing users or groups, but can be extended to if need be.
 */
public class LocalRealmUsersFlowContext implements IFlowContext {

    public static final String DUMMY_USER = "-------";
    private static final String DEFAULT_PASSWORD = "";

    private final Map<String, String> userPasswordMap;
    private final Map<String, Set<String>> groupUserMap;

    private String usersXMLFilePath;
    private boolean plainTextPasswords;

    public LocalRealmUsersFlowContext(Builder builder) {
        this.userPasswordMap = builder.userPasswordMap;
        this.groupUserMap = builder.groupUserMap;
        this.usersXMLFilePath = builder.emBase + builder.configDirectory + builder.usersXML;
        this.plainTextPasswords = builder.plainTextPassword;
    }

    public String getUsersXMLFilePath() {
        return usersXMLFilePath;
    }

    public Map<String, String> getUserPasswordMap() {
        return userPasswordMap;
    }

    public Map<String, Set<String>> getGroupUserMap() {
        return groupUserMap;
    }

    public boolean isPlainTextPasswords() {
        return plainTextPasswords;
    }

    public static class LinuxBuilder extends Builder {

        @Override
        protected String getPathSeparator() {
            return LINUX_SEPARATOR;
        }
    }

    public static class Builder extends BuilderBase<Builder, LocalRealmUsersFlowContext> {

        final Map<String, String> userPasswordMap = new HashMap<>();
        final Map<String, Set<String>> groupUserMap = new HashMap<>();

        String emBase = getDeployBase() + getPathSeparator() + "em" + getPathSeparator();
        String configDirectory = "config" + getPathSeparator();
        String usersXML = "users.xml";
        boolean plainTextPassword = false;

        @Override
        protected LocalRealmUsersFlowContext getInstance() {
            return build();
        }

        @Override
        protected Builder builder() {
            return this;
        }

        @Override
        public LocalRealmUsersFlowContext build() {
            for (String password : userPasswordMap.values()) {
                if (!password.equals("")) {
                    plainTextPassword = true;
                }
            }
            return new LocalRealmUsersFlowContext(this);
        }

        public Builder emBase(String emBaseDir) {
            if (!emBaseDir.endsWith(getPathSeparator())) {
                emBaseDir = emBaseDir + getPathSeparator();
            }
            emBase = emBaseDir;
            return this;
        }

        public Builder configDir(String configDirectory) {
            if (!configDirectory.endsWith(getPathSeparator())) {
                configDirectory = configDirectory + getPathSeparator();
            }
            this.configDirectory = configDirectory;
            return this;
        }

        public Builder realmXMLFile(String fileName) {
            usersXML = fileName;
            return this;
        }

        /**
         * User will be added with the given name and password
         *
         * Beware that this will set the attribute plainTextPasswords to true causing EM to
         * encrypt all passwords. When using this method, make sure to re-specify all existing
         * users with their passwords or their login won't work.
         *
         * @param name
         * @param plainTextPassword
         * @return
         */
        public Builder user(String name, String plainTextPassword) {
            addUser(name, plainTextPassword);
            return this;
        }

        /**
         * User will be added with default blank/no password
         * 
         * @param name
         * @return
         */
        public Builder user(String name) {
            addUser(name, DEFAULT_PASSWORD);
            return this;
        }

        /**
         * Group will be added without any users assigned
         * 
         * @param group
         * @return
         */
        public Builder group(String group) {
            addGroup(group, DUMMY_USER);
            return this;
        }

        /**
         * Add user to a group. If user does not exist, EM may fail to start
         * 
         * @param group
         * @param user
         * @return
         */
        public Builder group(String group, String user) {
            addGroup(group, user);
            return this;
        }

        private void addGroup(String group, String user) {
            Set<String> groupUserSet = groupUserMap.get(group);
            if (groupUserSet == null) {
                groupUserSet = new HashSet<>();
                groupUserMap.put(group, groupUserSet);
            }
            groupUserSet.add(user);
        }

        private void addUser(String name, String plainTextPassword) {
            userPasswordMap.put(name, plainTextPassword);
        }
    }
}
