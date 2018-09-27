/*******************************************************************************
 * Copyright (c) 2010, 2011 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype, Inc. - initial API and implementation
 *******************************************************************************/
package com.ca.apm.systemtest.fld.plugin.downloader.maven;

import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.RepositoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simplistic repository listener that logs events to the console.
 */
public class ConsoleRepositoryListener
    extends AbstractRepositoryListener {

    private final Logger out = LoggerFactory.getLogger("maven.repository");

    public ConsoleRepositoryListener() {
    }

    public void artifactDeployed(RepositoryEvent event) {
        out.info("Deployed {} to {}", event.getArtifact(), event.getRepository());
    }

    public void artifactDeploying(RepositoryEvent event) {
        out.info("Deploying {} to {}", event.getArtifact(), event.getRepository());
    }

    public void artifactDescriptorInvalid(RepositoryEvent event) {
        out.info("Invalid artifact descriptor for {}: {}", event.getArtifact(),
            event.getException().getMessage());
    }

    public void artifactDescriptorMissing(RepositoryEvent event) {
        out.info("Missing artifact descriptor for {}", event.getArtifact());
    }

    public void artifactInstalled(RepositoryEvent event) {
        out.info("Installed {} to {}", event.getArtifact(), event.getFile());
    }

    public void artifactInstalling(RepositoryEvent event) {
        out.info("Installing {} to {}", event.getArtifact(), event.getFile());
    }

    public void artifactResolved(RepositoryEvent event) {
        out.info("Resolved artifact {} from {}", event.getArtifact(), event.getRepository());
    }

    public void artifactDownloading(RepositoryEvent event) {
        out.info("Downloading artifact {} from {}", event.getArtifact(), event.getRepository());
    }

    public void artifactDownloaded(RepositoryEvent event) {
        out.info("Downloaded artifact {} from {}", event.getArtifact(), event.getRepository());
    }

    public void artifactResolving(RepositoryEvent event) {
        out.info("Resolving artifact {}", event.getArtifact());
    }

    public void metadataDeployed(RepositoryEvent event) {
        out.info("Deployed {} to {}", event.getMetadata(), event.getRepository());
    }

    public void metadataDeploying(RepositoryEvent event) {
        out.info("Deploying {} to {}", event.getMetadata(), event.getRepository());
    }

    public void metadataInstalled(RepositoryEvent event) {
        out.info("Installed {} to {}", event.getMetadata(), event.getFile());
    }

    public void metadataInstalling(RepositoryEvent event) {
        out.info("Installing {} to {}", event.getMetadata(), event.getFile());
    }

    public void metadataInvalid(RepositoryEvent event) {
        out.info("Invalid metadata {}", event.getMetadata());
    }

    public void metadataResolved(RepositoryEvent event) {
        out.info("Resolved metadata {} from {}", event.getMetadata(), event.getRepository());
    }

    public void metadataResolving(RepositoryEvent event) {
        out.info("Resolving metadata {} from {}", event.getMetadata(), event.getRepository());
    }

}
