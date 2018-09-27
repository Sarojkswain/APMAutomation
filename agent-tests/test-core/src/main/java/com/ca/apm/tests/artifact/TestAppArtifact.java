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
  
import static com.ca.tas.artifact.IBuiltArtifact.TasExtension.WAR;
  

import com.ca.tas.artifact.IArtifactExtension;
import com.ca.tas.artifact.ITasArtifact;
import com.ca.tas.artifact.ITasArtifactFactory;
import com.ca.tas.artifact.TasArtifact;
import com.ca.tas.resolver.ITasResolver;

/**
* TestAppArtifact class represents artifact for TestApplication for JavaAgent Component
* 
* @author Aleem Ahmad (ahmal01)
* 
*/
public class TestAppArtifact implements ITasArtifactFactory {

    private final ITasResolver resolver;
    private String group_id = "com.ca.apm.coda-projects.test-tools";
    private String artifact_id = "qatestapp";
    private String classifier = "";
    
    public TestAppArtifact(ITasResolver resolver) {
           this.resolver = resolver;
    }
    
    public void setGroupID (String group_id) {
        this.group_id = group_id;
     }
        public void setArtifactID (String artifact_id) {
            this.artifact_id = artifact_id;
     }
        public void setClassifier(String classifier) {
            this.classifier = classifier;
     }
       
        @Override
        public ITasArtifact createArtifact() {
             return createArtifact(null);
        }
  
      private IArtifactExtension getExtension() {
          return WAR;
      }
  
      @Override
      public ITasArtifact createArtifact(String version) {
          
          return new TasArtifact.Builder(artifact_id)
              .version((version == null) ? resolver.getDefaultVersion() : version)
              .extension(getExtension())
              .groupId(group_id)
              .classifier(classifier)
              .build();
      }
      
 
  }