package com.ca.apm.tests.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtils {
    
public static void unZipUpdate(String pathToUpdateZip, String destinationPath){
    byte[] byteBuffer = new byte[1024];

    try{
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(pathToUpdateZip));
        ZipEntry inZipEntry = inZip.getNextEntry();
        while(inZipEntry != null){
            String fileName = inZipEntry.getName();
            File unZippedFile = new File(destinationPath + File.separator + fileName);
            System.out.println("Unzipping: " + unZippedFile.getAbsoluteFile());
            if (inZipEntry.isDirectory()){
                unZippedFile.mkdirs();
            }else{
                new File(unZippedFile.getParent()).mkdirs();
                unZippedFile.createNewFile();
                FileOutputStream unZippedFileOutputStream = new FileOutputStream(unZippedFile);
                int length;
                while((length = inZip.read(byteBuffer)) > 0){
                    unZippedFileOutputStream.write(byteBuffer,0,length);
                }
                unZippedFileOutputStream.close();                       
            }
            inZipEntry = inZip.getNextEntry(); 
        }
        inZip.close();
        System.out.println("Finished Unzipping");
    }catch(IOException e){
        e.printStackTrace();
    }
 }
}