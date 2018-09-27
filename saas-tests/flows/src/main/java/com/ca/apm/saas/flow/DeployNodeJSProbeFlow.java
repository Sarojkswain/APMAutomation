/*
 * Copyright (c) 2015 CA.  All rights reserved.
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

package com.ca.apm.saas.flow;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.utils.archive.Archive;
import com.ca.apm.automation.utils.archive.DownloadableTasArchive;

/**
 * DeployNodeJSProbeFlow class provides logic to deploy nodejs probe
 *
 */
@Flow
public class DeployNodeJSProbeFlow extends DeployNodeJsPackageFlow {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeployNodeJSProbeFlow.class);

	protected void deployArchivedPackage() throws IOException, URISyntaxException {
		File installerTgdirDir = new File(context.getInstallerTgDir());
		Archive tarArchive = archiveFactory.createArchive(context.getInstallerUrl());
		String packageName = tarArchive.getArchiveFile().getName();

		LOGGER.info("Installing NodeJS Probe: {}", packageName);

		// TODO is this still valid?
		// since probe is not currently packages using 'npm package' command,
		// it is not advisable to use tar archive for installation
		boolean useTarFile = false;

		if (useTarFile && (tarArchive instanceof DownloadableTasArchive)) {
			DownloadableTasArchive downTarAchive = (DownloadableTasArchive) tarArchive;
			File localTarFile = new File(installerTgdirDir, packageName);
			downTarAchive.download(localTarFile);
			installPackage(localTarFile);
		} else {
			tarArchive.unpack(installerTgdirDir);
			File probePackage = new File(installerTgdirDir + File.separator + "package");

			// now install
			installPackage(probePackage);
		}

		// delete the installer files
		FileUtils.forceDelete(installerTgdirDir);
	}
}