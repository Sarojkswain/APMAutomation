/**
 * 
 */
package com.ca.apm.tests.role;

import java.net.MalformedURLException;
import java.net.URL;

import com.ca.tas.repository.YumRepository;

/**
 * @author zheji01@ca.com
 *
 */
public enum MongoDBYumRepository
    implements YumRepository
{
	v32("MongoDB-3.2", "https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/3.2/x86_64/", "MongoDB 3.2 Server", false),
    v30("MongoDB-3.0", "https://repo.mongodb.org/yum/redhat/$releasever/mongodb-org/3.0/x86_64/", "MongoDB 3.0 Server", false),
    v26("MongoDB-2.6", "http://downloads-distro.mongodb.org/repo/redhat/os/x86_64/", "MongoDB 2.6 Server", false);

    private final String repoId;
    private final URL repoUrl;
    private final String name;
    private final boolean gpgCheck;

    MongoDBYumRepository(String repoId, String repoUrl, String name, boolean gpgCheck) {
        this.repoId = repoId;
        this.repoUrl = createUrl(repoUrl);
        this.name = name;
        this.gpgCheck = gpgCheck;
    }

    private URL createUrl(String repoUrl) {
        try {
            return new URL(repoUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getRepoId() {
        return repoId;
    }

    @Override
    public boolean isGpgCheck() {
        return gpgCheck;
    }

    @Override
    public URL getBaseUrl() {
        return repoUrl;
    }
    
}
