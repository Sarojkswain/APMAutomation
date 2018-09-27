package com.ca.apm.siteminder;

import java.util.Set;

import com.ca.apm.automation.action.responsefile.ResponseFile;
import com.ca.apm.automation.action.responsefile.Triplet;
import com.ca.apm.automation.action.responsefile.writer.IResponseFileWriter;
import com.ca.apm.automation.action.responsefile.writer.TextFileWriter;

public class AUIResponseFile extends ResponseFile

{

    private final IResponseFileWriter writer = new TextFileWriter("%s%s=%s");
    private final Set<Triplet> installResponseFileData;

    public AUIResponseFile(Set<Triplet> installResponseFileData) {
        this.installResponseFileData = installResponseFileData;
    }

    @Override
    public IResponseFileWriter getWriter() {
        return writer;
    }

    @Override
    protected Set<Triplet> getInstallResponseFileData() {
        return installResponseFileData;
    }

}
