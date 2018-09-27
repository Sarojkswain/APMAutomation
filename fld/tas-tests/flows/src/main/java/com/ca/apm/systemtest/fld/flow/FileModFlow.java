/*
 * Copyright (c) 2014 CA.  All rights reserved.
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

package com.ca.apm.systemtest.fld.flow;

import java.io.File;

import com.ca.apm.automation.action.core.annotations.Flow;
import com.ca.apm.automation.action.core.annotations.FlowContext;
import com.ca.apm.automation.action.flow.utility.FileModifierFlow;
import com.ca.apm.automation.action.flow.utility.FileModifierFlowContext;
import com.ca.apm.automation.utils.file.FileData;
import com.ca.apm.automation.utils.file.FileDeleteData;
import com.ca.tas.annotation.TasDocFlow;

/**
 *
 * Extend flow with file existence check (and skip if doesn't exist)
 *
 * @author filja01
 */
@Flow
@TasDocFlow(description = "Extend flow with file existence check (and skip if doesn't exist)")
public class FileModFlow extends FileModifierFlow {

    @FlowContext
    private FileModifierFlowContext context;
    
    @Override
    protected void move() {
        for (FileData moveEntry : context.getMoveData()) {
            File file = new File(moveEntry.getSource());
            if (file.exists()) {
                fileOperationFactory.create(file).move(moveEntry);
            }
        }
    }

    @Override
    protected void delete(){
        for(FileDeleteData deleteEntry : context.getDeleteData()){
            File file = new File(deleteEntry.getFile());
            if (file.exists()) {
                fileOperationFactory.create(file).delete(deleteEntry);
            }
        }
    }

    @Override
    protected void copy() {
        for (FileData moveEntry : context.getCopyData()) {
            File file = new File(moveEntry.getSource());
            if (file.exists()) {
                fileOperationFactory.create(file).copy(moveEntry);
            }
        }
    }
}
