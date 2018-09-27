/*
 * Copyright (c) 2016 CA. All rights reserved.
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
package com.ca.apm.tests.flow;

import com.ca.apm.automation.action.flow.IFlowContext;
import com.ca.tas.builder.ExtendedBuilderBase;
import com.ca.tas.property.EnvPropSerializable;
import org.apache.http.util.Args;

import java.util.Map;

/**
 * Flow Context for installing NET StockTrader WebApp into IIS
 *
 * @author Erik Melecky (meler02@ca.com)
 */
public class CsvToXlsFlowContext implements IFlowContext, EnvPropSerializable<CsvToXlsFlowContext> {

    private final String shareDir;
    private final String csvToXlsJarPath;
    private final Map<String, String> sheetsMapping;
    private final transient CsvToXlsFlowContextSerializer envPropSerializer;
    private String templateFileName;
    private String outputFileName;
    private String heapMemory;

    protected CsvToXlsFlowContext(CsvToXlsFlowContext.Builder builder) {
        this.shareDir = builder.shareDir;
        this.csvToXlsJarPath = builder.csvToXlsJarPath;
        this.templateFileName = builder.templateFileName;
        this.outputFileName = builder.outputFileName;
        this.heapMemory = builder.heapMemory;

        this.sheetsMapping = builder.sheetsMapping;

        this.envPropSerializer = new CsvToXlsFlowContextSerializer(this);
    }

    public String getShareDir() {
        return shareDir;
    }

    public String getCsvToXlsJarPath() {
        return csvToXlsJarPath;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public String getOutputFileName() {
        return outputFileName;
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public Map<String, String> getSheetsMapping() {
        return sheetsMapping;
    }

    public String getHeapMemory() {
        return heapMemory;
    }

    public void setHeapMemory(String heapMemory) {
        this.heapMemory = heapMemory;
    }

    @Override
    public CsvToXlsFlowContext deserialize(String key, Map<String, String> serializedData) {
        return this.envPropSerializer.deserialize(key, serializedData);
    }

    @Override
    public Map<String, String> serialize(String key) {
        return this.envPropSerializer.serialize(key);
    }

    public static class Builder extends ExtendedBuilderBase<CsvToXlsFlowContext.Builder, CsvToXlsFlowContext> {

        protected String shareDir;
        protected String csvToXlsJarPath;
        protected String templateFileName;
        protected String outputFileName;
        protected String heapMemory;

        protected Map<String, String> sheetsMapping;

        public Builder() {
            this.shareDir = "c:\\share";
            this.heapMemory = "1024m";
        }

        public CsvToXlsFlowContext build() {
            CsvToXlsFlowContext context = this.getInstance();
            Args.notNull(context.shareDir, "shareDir");
            Args.notNull(context.csvToXlsJarPath, "csvToXlsJarPath");
            Args.notNull(context.templateFileName, "templateFileName");
            Args.notNull(context.outputFileName, "outputFileName");
            Args.notNull(context.heapMemory, "heapMemory");
            Args.notNull(context.sheetsMapping, "sheetsMapping");

            return context;
        }

        protected CsvToXlsFlowContext getInstance() {
            return new CsvToXlsFlowContext(this);
        }

        public CsvToXlsFlowContext.Builder shareDir(String shareDir) {
            this.shareDir = shareDir;
            return this.builder();
        }

        public CsvToXlsFlowContext.Builder csvToXlsJarPath(String csvToXlsJarPath) {
            this.csvToXlsJarPath = csvToXlsJarPath;
            return this.builder();
        }

        public CsvToXlsFlowContext.Builder templateFileName(String templateFileName) {
            this.templateFileName = templateFileName;
            return this.builder();
        }

        public CsvToXlsFlowContext.Builder outputFileName(String outputFileName) {
            this.outputFileName = outputFileName;
            return this.builder();
        }

        public CsvToXlsFlowContext.Builder heapMemory(String heapMemory) {
            this.heapMemory = heapMemory;
            return this.builder();
        }

        public CsvToXlsFlowContext.Builder sheetsMapping(Map<String, String> sheetsMapping) {
            this.sheetsMapping = sheetsMapping;
            return this.builder();
        }

        protected CsvToXlsFlowContext.Builder builder() {
            return this;
        }
    }

}