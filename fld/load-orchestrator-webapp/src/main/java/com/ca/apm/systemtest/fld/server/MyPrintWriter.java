/**
 * 
 */
package com.ca.apm.systemtest.fld.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Locale;

/**
 * @author keyja01
 *
 */
public class MyPrintWriter extends PrintWriter {
	
	@Override
	public void print(boolean b) {
		super.print(b);
	}
	
	@Override
	public void print(char[] s) {
		super.print(s);
	}
	
	@Override
	public void print(float f) { 
		super.print(f);
	}
	
	@Override
	public void print(char c) {
		super.print(c);
	}
	
	@Override
	public void print(double d) {
		super.print(d);
	}

	@Override
	public void print(int i) {
		super.print(i);
	}
	

	@Override
	public void print(long l) {
		super.print(l);
	}
	
	@Override
	public void print(Object obj) {
		super.print(obj);
	}
	
	@Override
	public void print(String s) {
		super.print(s);
	}
	
	@Override
	public PrintWriter printf(Locale l, String format, Object... args) {
		return super.printf(l, format, args);
	}
	
	@Override
	public PrintWriter printf(String format, Object... args) {
		return super.printf(format, args);
	}
	
	@Override
	public void println() {
		super.println();
	}
	
	@Override
	public void println(boolean x) {
		super.println(x);
	}
	
	@Override
	public void println(char x) {
		super.println(x);
	}
	
	@Override
	public void println(char[] x) {
		super.println(x);
	}
	
	@Override
	public void println(double x) {
		super.println(x);
	}
	
	@Override
	public void println(float x) {
		super.println(x);
	}
	
	@Override
	public void println(int x) {
		super.println(x);
	}
	
	@Override
	public void println(long x) {
		super.println(x);
	}
	
	@Override
	public void println(Object x) {
		super.println(x);
	}
	
	@Override
	public void println(String x) {
		super.println(x);
	}
	
	@Override
	public PrintWriter append(char c) {
		return super.append(c);
	}
	
	@Override
	public PrintWriter append(CharSequence csq) {
		return super.append(csq);
	}
	
	@Override
	public PrintWriter append(CharSequence csq, int start, int end) {
		return super.append(csq, start, end);
	}
	
	@Override
	public PrintWriter format(Locale l, String format, Object... args) {
		return super.format(l, format, args);
	}
	
	@Override
	public PrintWriter format(String format, Object... args) {
		return super.format(format, args);
	}
	
	@Override
	public void write(char[] buf) {
		super.write(buf);
	}
	
	@Override
	public void write(char[] buf, int off, int len) {
		super.write(buf, off, len);
	}
	
	@Override
	public void write(int c) {
		super.write(c);
	}
	
	@Override
	public void write(String s) {
		super.write(s);
	}
	
	@Override
	public void write(String s, int off, int len) {
		super.write(s, off, len);
	}
	
	
	/**
	 * @param out
	 */
	public MyPrintWriter(Writer out) {
		super(out);
	}

	/**
	 * @param out
	 */
	public MyPrintWriter(OutputStream out) {
		super(out);
	}

	/**
	 * @param fileName
	 * @throws FileNotFoundException
	 */
	public MyPrintWriter(String fileName) throws FileNotFoundException {
		super(fileName);
	}

	/**
	 * @param file
	 * @throws FileNotFoundException
	 */
	public MyPrintWriter(File file) throws FileNotFoundException {
		super(file);
	}

	/**
	 * @param out
	 * @param autoFlush
	 */
	public MyPrintWriter(Writer out, boolean autoFlush) {
		super(out, autoFlush);
	}

	/**
	 * @param out
	 * @param autoFlush
	 */
	public MyPrintWriter(OutputStream out, boolean autoFlush) {
		super(out, autoFlush);
	}

	/**
	 * @param fileName
	 * @param csn
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public MyPrintWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(fileName, csn);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param file
	 * @param csn
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	public MyPrintWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
		super(file, csn);
	}

}
