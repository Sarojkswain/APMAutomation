package com.ca.apm.tests.utility;

import java.io.*;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarWriter {
	
	protected String m_source;
	protected String m_outputFile;
	protected String m_manifestVersion;

	public static final String DEFAULT_MANIFEST_VERSION = "1.0";
	
	public JarWriter(String a_source, String a_outputFile) {
		this(a_source, a_outputFile, DEFAULT_MANIFEST_VERSION);
	}

	public JarWriter(String a_source, String a_outputFile, String a_manifestVersion) {
		m_source = a_source;
		m_outputFile = a_outputFile;
		m_manifestVersion = a_manifestVersion;
	}

	public void run() throws IOException
	{
	  Manifest manifest = new Manifest();
	  manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, m_manifestVersion);
	  JarOutputStream target = new JarOutputStream(new FileOutputStream(m_outputFile), manifest);
	  int originalSourcePathLength = (new File(m_source).getAbsolutePath()).length();
	  add(originalSourcePathLength, new File(m_source), target);
	  target.close();
	}

	private void add(int originalSourcePathLength, File source, JarOutputStream target) throws IOException
	{
	  BufferedInputStream in = null;
	  try
	  {
	    if (source.isDirectory())
	    {
	      String name = source.getPath().replace("\\", "/");
	      name = name.substring(originalSourcePathLength);
	      if (!name.isEmpty())
	      {
	        if (!name.endsWith("/"))
	          name += "/";
	        if (name.startsWith("/")) {
	        	name = name.substring(1);
	        }
	        JarEntry entry = new JarEntry(name);
	        entry.setTime(source.lastModified());
	        target.putNextEntry(entry);
	        target.closeEntry();
	      }
	      for (File nestedFile: source.listFiles())
	        add(originalSourcePathLength, nestedFile, target);
	      return;
	    }
	    String entryPath = source.getPath().substring(originalSourcePathLength).replace("\\", "/");
	    if (entryPath.startsWith("/")) {
	    	entryPath = entryPath.substring(1);
	    }
	    JarEntry entry = new JarEntry(entryPath);
	    entry.setTime(source.lastModified());
	    target.putNextEntry(entry);
	    in = new BufferedInputStream(new FileInputStream(source));

	    byte[] buffer = new byte[1024];
	    while (true)
	    {
	      int count = in.read(buffer);
	      if (count == -1)
	        break;
	      target.write(buffer, 0, count);
	    }
	    target.closeEntry();
	  }
	  finally
	  {
	    if (in != null)
	      in.close();
	  }
	}
}
