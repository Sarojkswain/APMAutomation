/*
 * Copyright (c) 2016 CA.  All rights reserved.
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

package com.ca.apm.tests.artifact;

import org.eclipse.aether.artifact.Artifact;

import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.IBuiltArtifact.TasExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.IThirdPartyArtifact;
import com.ca.tas.artifact.TasArtifact;

/**
 * NodeJsRunTimeVersionArtifact class
 *
 * @author Jan Pojer, sinka08
 */
public enum NodeJSRuntimeVersionArtifact implements IThirdPartyArtifact {
	
	LINUXx64v6_8_1("nodejs", "6.8.1", "linux-x64", TasExtension.TAR_GZ),
	LINUXx64v5_8_0("nodejs", "5.8.0", "linux-x64", TasExtension.TAR_GZ),
	LINUXx64v4_6_0("nodejs", "4.6.0", "linux-x64", TasExtension.TAR_GZ),
	LINUXx64v4_3_1("nodejs", "4.3.1", "linux-x64", TasExtension.TAR_GZ), 
	LINUXx64v0_12_2("nodejs", "0.12.2",	"linux-x64", TasExtension.TAR_GZ), 
	WINDOWSx64v0_12_2("nodejs", "0.12.2", "win-x64", TasExtension.EXE), 
	LINUXx64v0_10_28("nodejs", "0.10.28", "linux-x64", TasExtension.TAR_GZ);

	/** Constant <code>GROUP_ID="com.ca.apm.libs"</code> */
	private static final String GROUP_ID = "com.ca.apm.libs";
	private final ITasArtifact tasArtifact;

	NodeJSRuntimeVersionArtifact(String artifactId, String version, String classifier,
	        final String type) {
		this(artifactId, version, classifier, new IArtifactExtension() {

			@Override
			public String getValue() {
				return type;
			}
		});
	}

	NodeJSRuntimeVersionArtifact(String artifactId, String version, String classifier,
	        IArtifactExtension type) {
		tasArtifact = new TasArtifact.Builder(artifactId).groupId(GROUP_ID).version(version)
		        .extension(type).classifier(classifier).build();
	}

	/** {@inheritDoc} */
	@Override
	public String getFilename() {
		Artifact artifact = getArtifact();
		return String.format("%s-%s-%s.%s", artifact.getArtifactId(), artifact.getVersion(),
		        artifact.getClassifier(), artifact.getExtension());
	}

	/** {@inheritDoc} */
	@Override
	public Artifact getArtifact() {
		return tasArtifact.getArtifact();
	}
}
