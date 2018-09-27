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
package com.ca.tas.test.utils;

import static java.lang.String.format;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.http.util.Args;

import com.ca.apm.automation.utils.archive.TasArchiveFactory;

/**
 * Provides common functionality to fetch various resources from external environment and provide
 * them on local file system.
 * 
 * @author Pospichal, Pavel <pospa02@ca.com>
 * 
 */
public class LocalStorage {

    private final File localStorageFolder;
    
    private TasArchiveFactory tasArchiveFactory;

    public LocalStorage(Method testMethod) {
        this(format("%s_%s", testMethod.getDeclaringClass().getSimpleName(), testMethod.getName()));
    }
    
    public LocalStorage(String folderName) {
        this.localStorageFolder = new File(folderName);
        
        // TODO: inject as it might be shared among multiple storages
        tasArchiveFactory = new TasArchiveFactory();
    }

    public String[] getFileLocations(String fileNamePattern) {
        Collection<File> filteredFiles =
                FileUtils.listFiles(localStorageFolder, new WildcardFileFilter(fileNamePattern),
                        DirectoryFileFilter.DIRECTORY);

        List<String> fileredFileLocations = new ArrayList<String>(filteredFiles.size());
        for (File filteredFile : filteredFiles) {
            fileredFileLocations.add(filteredFile.getAbsolutePath());
        }
        return fileredFileLocations.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }
    
    public void fetchResource(String resourceLocation) {
        URL resourceURLLocation;
        try {
            resourceURLLocation = new URL(resourceLocation);
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException("URL location of resource is invalid!", e);
        }
        fetchResource(resourceURLLocation);
    }
    
    public void fetchResource(URL resourceLocation) {
        Args.notNull(resourceLocation, "Target URL");
        try {
            File downloadedFile =
                    new File(this.localStorageFolder, FilenameUtils.getName(resourceLocation
                            .getFile()));
            tasArchiveFactory.createArtifact(resourceLocation).download(downloadedFile);
        } catch (Exception e) {
            throw new IllegalStateException(format("Unable to fetch external resource from %s",
                    resourceLocation), e);
        }
    }
}
