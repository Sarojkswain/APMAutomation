package com.ca.apm.tests.test;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

//
// import java.io.BufferedReader;
// import java.io.File;
// import java.io.IOException;
// import java.util.*;
// import java.net.URI;
// import java.nio.file.*;
// import java.io.InputStreamReader;
//
// import javax.xml.parsers.DocumentBuilderFactory;
// import javax.xml.transform.Transformer;
// import javax.xml.transform.TransformerFactory;
// import javax.xml.transform.dom.DOMSource;
// import javax.xml.transform.stream.StreamResult;
// import javax.xml.xpath.XPath;
// import javax.xml.xpath.XPathConstants;
// import javax.xml.xpath.XPathFactory;
//
// import org.w3c.dom.Document;
// import org.w3c.dom.NodeList;
//
// import com.ca.apm.commons.common.XMLFileUtil;
// import com.jcraft.jsch.ChannelExec;
// import com.jcraft.jsch.JSch;
// import com.jcraft.jsch.Session;
//
public class Test {

    public static void main(String[] args) throws URISyntaxException {
        // // TODO Auto-generated method stub
        // String adminLoginurlwithQuery = "http://jamsa07-cembat1:7011"
        // + "/medrec/loginAdmin.action"
        // + "\\?userName=\"User1\"\\&groupId=\"Group1\"";
        //
        // runScriptOnUnix("tas-cz-ne9", "root",
        // "Lister@123", "/jamsa07/execUrl.sh " + adminLoginurlwithQuery +" 10");
        //
        // File[] files = new
        // File("C:\\Program Files\\CA APM\\Introscope10.3.0.6\\product\\enterprisemanager\\plugins\\").listFiles();
        //
        // for(File file:files)
        // {
        // if
        // (file.toString().toLowerCase().contains("com.wily.apm.tess")&&!file.toString().toLowerCase().contains("com.wily.apm.tess.nl1"))
        // {
        // // if (file.toString().toLowerCase().contains("com.wily.apm.tess")) {
        // System.out.println(file.toString());
        // break;
        // }
        // }
        // getTessSecurityFile();
        // updateTessSecurityFile();
        // updateJarFile();

        // Map<String, String> env = new HashMap<>();
        // env.put("create", "true");
        // // locate file system by using the syntax
        // // defined in java.net.JarURLConnection
        // URI uri =
        // URI.create("jar:file:/Progra~1/CAAPM~1/Introscope10.3.0.6/product/enterprisemanager/plugins/com.wily.apm.tess_10.3.0.jar");
        // //URI uri = new
        // URI("C:/Progra~1/CAAPM~1/Introscope10.3.0.6/product/enterprisemanager/plugins/com.wily.apm.tess_10.3.0.jar");
        //
        //
        //
        //
        // try (FileSystem zipfs = FileSystems.newFileSystem(uri, env)) {
        // Path externalTxtFile =
        // Paths.get("C:\\TEST_JAMSA\\WebContent\\WEB-INF\\tess-security.xml");
        // Path pathInZipfile = zipfs.getPath("/WebContent/WEB-INF/tess-security.xml");
        // // copy a file into the zip file
        // Files.copy(externalTxtFile, pathInZipfile, StandardCopyOption.REPLACE_EXISTING);
        // } catch (Exception e) {
        // System.out.println("WASTED");
        // }



//        try {
//            JarFile jarFile =
//                new JarFile(
//                    new File(
//                        "C:/Progra~1/CAAPM~1/Introscope10.3.0.6/product/enterprisemanager/plugins/com.wily.apm.tess_10.3.0.jar"));
//            new Test()
//                .copyToNewJar(
//                    jarFile,
//                    new File(
//                        "C:\\Progra~1\\CAAPM~1\\Introscope10.3.0.6\\product\\enterprisemanager\\plugins\\com.wily.apm.tess_10.3.0_2.jar"),
//                    "C:\\TEST_JAMSA\\WebContent\\WEB-INF\\tess-security.xml", "tess-security.xml");
//
//        } catch (IOException e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }


        File f1 = new File("C:\\TEST_JAMSA\\WebContent\\WEB-INF\\tess-security.xml", "tess-security.xml");
        try {
            BufferedReader b1 = new BufferedReader(new FileReader(f1));
            String str;
            if(b1.ready())
            {
                while((str =b1.readLine())!=null)
                {
                    if(str.contains("<bean autowire=\"default\" class=\"com.timestock.tess.services.security.AccessPolicy\" dependency-check=\"default\" lazy-init=\"default\">"))
                    {
                        
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    /**
     * Replaces the old content.xml with the modified version after saving.
     * Copies all content from the JAR file expect the content.xml to a temporary file, adds the
     * modified content.xml to the temporary file, deletes the old JAR file and renames the
     * temporary
     * file with the name of the old JAR file
     * 
     * @param oldFile contains the presentation before saving
     * @param newFile an empty temporary file (=the new jar file)
     * @param f the file which should be added to the new archive (=the new content.xml)
     */
    public void copyToNewJar(JarFile oldFile, File newFile, String f, String fileToReplace) {

        try {
            InputStream jarIs = null;
            FileInputStream fileIs = new FileInputStream(f);
            Enumeration<JarEntry> entries = oldFile.entries();
            JarOutputStream newJarOs = new JarOutputStream(new FileOutputStream(newFile.getName()));
            int read;
            byte buffer[] = new byte[1024];

            while (entries.hasMoreElements()) {
                JarEntry entry = (JarEntry) entries.nextElement();
                String name = entry.getName();
                System.out.println("name: " + name);

                // ignore the old content.xml
                if (name.equals(fileToReplace)) {
                    continue;
                }
                System.out.println(oldFile.getName());
                jarIs = oldFile.getInputStream(entry);

                newJarOs.putNextEntry(entry);
                while ((read = jarIs.read(buffer)) != -1) {
                    newJarOs.write(buffer, 0, read);
                }
            }
            // add the new content.xml
            String f1=f.split("WebContent")[1];
            System.out.println("Before " +f1);
            f1="WebContent"+f1;
            System.out.println("After " +f1);
            JarEntry entry = new JarEntry(f1);
            newJarOs.putNextEntry(entry);
            while ((read = fileIs.read(buffer)) != -1) {
                newJarOs.write(buffer, 0, read);
            }
            fileIs.close();
            newJarOs.close();
            if (jarIs != null) {
                jarIs.close();
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // close and delete the old jar file, and rename the temporary file
        try {
            oldFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        File origFile = new File(oldFile.getName());
        origFile.delete();
        newFile.renameTo(origFile);

    }
}
