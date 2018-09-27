package com.ca.apm.commons.coda.common;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ByteToZipFileConverter {
	
	public File createZipFileFromByteArray(byte[] a_byteArray, File a_zipFile) throws IOException,FileNotFoundException{
		if (a_zipFile != null ) {
			if (a_byteArray.length > 0) {
				FileOutputStream fs;
				fs = new FileOutputStream(a_zipFile);
				fs.write(a_byteArray);
				fs.close();
		}
		System.out.println("Exported to file:  " + a_zipFile.getName());
		}
		return a_zipFile;

	}

}
