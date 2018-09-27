package com.ca.apm.systemtest.fld.agent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

public class Agent {

	public static void main(String[] args) throws InterruptedException {
		File f1 = new File("first.txt");
		File f2 = new File("second.txt");
		File f3 = new File("third.txt");
		if (!f1.exists()) {
			createFile(f1);
			exit(50);
		} else if (!f2.exists()) {
			createFile(f2);
			exit(100);
		} else if (!f3.exists()) {
			createFile(f3);
			System.out.println("Exception exit");
			throw new RuntimeException("Simulated runtime exception");
		}
		System.out.println("Normal exit");
	}

	private static void exit(int exitCode) {
		System.out.println("Exiting with code " + exitCode);
		System.exit(exitCode);
	}

	private static void createFile(File f) {
		System.out.println("Creating file " + f.getName());
		try (Writer out = new FileWriter(f)) {
			out.write("Current time is: " + new Date());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
