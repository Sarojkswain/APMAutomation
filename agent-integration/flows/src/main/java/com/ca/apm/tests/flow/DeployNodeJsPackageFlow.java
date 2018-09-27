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

package com.ca.apm.tests.flow;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.FlowBase;
import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.apm.automation.utils.archive.Archive;
import com.ca.apm.automation.utils.archive.DownloadableTasArchive;

/**
 * DeployNodeJsPackageFlow class provides logic to deploy a nodejs package
 *
 */
@Flow
public class DeployNodeJsPackageFlow extends FlowBase {
	private static final Logger LOGGER = LoggerFactory.getLogger(DeployNodeJsPackageFlow.class);

	@FlowContext
	protected DeployNodeJSPackageFlowContext context;

	@Override
	public void run() throws Exception {
		LOGGER.info("Starting NodeJS package deployment");
		deployPackage();
		LOGGER.info("Deployment completed successfully");
	}

	protected void deployPackage() throws IOException, URISyntaxException {
		if (context.getPackageName() != null) {
			deployPublishedPackage();
		} else {
			deployArchivedPackage();
		}
	}

	protected void deployPublishedPackage() throws IOException {
		String fqPackageName = context.version() != null ? String.format("%s@%s",
		        context.getPackageName(), context.version()) : context.getPackageName();		
		LOGGER.info("Deploying published package: {}", fqPackageName);
		
		setNpmCache();

		// npm install <package>
		List<String> arguments = Arrays.asList("install", fqPackageName);
		performNpmCommand(arguments);
	}

	protected void deployArchivedPackage() throws IOException, URISyntaxException {
		Archive tarArchive = archiveFactory.createArchive(context.getInstallerUrl());
		String packageName = tarArchive.getArchiveFile().getName();

		LOGGER.info("Installing NodeJS package: {}", packageName);

		File installerTgdirDir = new File(context.getInstallerTgDir());

		if (tarArchive instanceof DownloadableTasArchive) {
			DownloadableTasArchive downTarAchive = (DownloadableTasArchive) tarArchive;
			File localTarFile = new File(installerTgdirDir, packageName);
			downTarAchive.download(localTarFile);
			installPackage(localTarFile);
		} else {
			throw new IllegalStateException(
			        "archive was expected to be of type DownloadableTasArchive"
			                + " rather than type: " + tarArchive.getClass().getSimpleName());
		}

		// delete the installer files
		FileUtils.forceDelete(installerTgdirDir);
	}

	protected void installPackage(File npmPackage) throws IOException {
	    
		setNpmCache();
		List<String> arguments = Arrays.asList("install", npmPackage.getCanonicalPath());
		performNpmCommand(arguments);
	}

	protected void setNpmCache() throws IOException {
		List<String> arguments = new LinkedList<>();
		File cache = new File(context.getInstallerTgDir(), "cache");
		FileUtils.forceMkdir(cache);
		arguments.addAll(0,
		        Arrays.asList("config", "set", "cache", cache.getCanonicalPath(), "--global"));
		performNpmCommand(arguments);
	}

	protected void performNpmCommand(List<String> arguments) {
		LOGGER.debug("npm command args: " + arguments);

		Map<String, String> execEnvironment = new HashMap<>();
		execEnvironment.put("HOME", context.getInstallDir());
		execEnvironment.put("USER", "root");
		File workingDir = new File(context.getInstallDir());
		workingDir.mkdir();
		String executablePath;

		if (SystemUtils.IS_OS_WINDOWS) {
			// For windows, npm archive is unpacked into InstallDir.
			executablePath = context.getNodeJsHomeDir() + File.separator + "npm";
		} else {
			executablePath = context.getNodeJsHomeDir() + File.separator + "bin" + File.separator
			        + "npm";
		}

		Execution exec = new Execution.Builder(executablePath, LOGGER).workDir(workingDir)
		        .args(arguments.toArray(new String[arguments.size()])).environment(execEnvironment)
		        .build();
		perform(exec);
	}

	protected void perform(Execution exec) {
		try {

			int resultCode = -1;
			int attempts = 0;
			int maxAttempts = 5;
			// This command is known to sporadically fail, but succeed later.
			// Until we figure out what goes wrong, lets retry up to 5 times
			// with 5 second pause.
			while (resultCode != 0 && attempts < maxAttempts) {
				resultCode = exec.go();
				attempts++;
				Thread.sleep(5000);
			}

			if (resultCode != 0) {
				throw new IllegalStateException("execution failed with code: " + resultCode);
			}
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
