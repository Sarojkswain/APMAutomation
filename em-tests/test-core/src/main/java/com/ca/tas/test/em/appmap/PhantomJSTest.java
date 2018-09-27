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
package com.ca.tas.test.em.appmap;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ca.apm.automation.action.flow.commandline.Execution;
import com.ca.tas.envproperty.EnvironmentPropertyContext;

public class PhantomJSTest
{
    static public Boolean execute(String resourceName, EnvironmentPropertyContext envProp, String... args) throws Exception {
        String resourcePath = exportResource(resourceName, envProp.getAsProperties());
        String commandPath = getPhantomJSPath(envProp.getRolePropertiesById("phantomjs"));
        File commandFile = new File(commandPath);
        
        List<String> phantomjsArgs = new ArrayList<>(Arrays.asList(args));
        phantomjsArgs.add(0, resourcePath);
        
        Logger log = LoggerFactory.getLogger(PhantomJSTest.class);
        Execution exec =
                new Execution.Builder(commandFile, log)
                        .workDir(commandFile.getParentFile())
                        .args(phantomjsArgs.toArray(ArrayUtils.EMPTY_STRING_ARRAY)).build();
        
        int returnCode = exec.go();
        System.out.println("Return code = " + returnCode);
        
        new File(resourcePath).delete();
        
        return returnCode == 0;
    }

    static private String getPhantomJSPath(Properties props) throws IOException {
        
        if (props != null) {
            String path = props.getProperty("phantomJSPath");
            String executable = props.getProperty("phantomJSExecutable");
            
            if (path != null && executable != null) {
                return new File(path, executable).getPath();
            }
        }

        return "C:\\automation\\deployed\\phantomjs\\phantomjs.exe";
    }

    /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
     * @throws Exception
     */
    static public String exportResource(String resourceName, Properties valuesByKey) throws Exception {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        String jarFolder;
        String outPath = null;
        TokenReplacer replacer = new TokenReplacer();
        
        try {
            stream = PhantomJSTest.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line;
            jarFolder = new File(PhantomJSTest.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParentFile().getPath().replace('\\', '/');
            outPath = new File(jarFolder, new File(resourceName).getName()).toString();
            resStreamOut = new FileOutputStream(outPath);
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(resStreamOut, "UTF-8"));
            while ((line = br.readLine()) != null) {
                bw.write(replacer.replaceTokens(line, valuesByKey));
                bw.newLine();
            }
            bw.close();
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (resStreamOut != null) {
                resStreamOut.close();
            }
        }

        return outPath;
    }
}
