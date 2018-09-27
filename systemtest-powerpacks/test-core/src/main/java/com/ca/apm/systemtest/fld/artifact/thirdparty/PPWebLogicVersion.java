package com.ca.apm.systemtest.fld.artifact.thirdparty;

import com.ca.tas.artifact.IThirdPartyArtifact;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;

/**
 * @Author rsssa02
 */
public enum PPWebLogicVersion implements IThirdPartyArtifact {
        v1036x86w("10.3.6", "32bit", "exe");
        //v1035x86("10.3", "", "");

        private final DefaultArtifact artifact;
        private final String version;
        private final String classifier;
        private final String type;


        PPWebLogicVersion(String version, String classifier, String type) {
            this.version = version;
            this.classifier = classifier;
            this.type = type;
            this.artifact = new DefaultArtifact("com.ca.apm.binaries", "weblogic", this.classifier, this.type, this.version);
        }

        public Artifact getArtifact() {
            return new DefaultArtifact(this.artifact.getGroupId(), this.artifact.getArtifactId(), this.artifact.getClassifier(), this.artifact.getExtension(), this.artifact.getVersion());
        }

        public String getFilename() {
            return String.format("%s-%s-%s.%s", "weblogic", this.version, this.classifier, this.type);
        }

}
