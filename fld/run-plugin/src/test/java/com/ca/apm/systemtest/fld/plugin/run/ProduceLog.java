package com.ca.apm.systemtest.fld.plugin.run;


public class ProduceLog {

	private static final int DEFAULT_NUM_OF_LINES = 1000;
	private static final int DEFAULT_WAIT = 5;

	public static final String[] LOG_LEVELS = {
		"INFO", "DEBUG", "ERROR", "WARN"
	};

	private static final String[] FILE_NAMES = {
		"DefaultBeanDefinitionDocumentReader.java",
		"AbstractAutowireCapableBeanFactory.java",
		"DefaultSingletonBeanRegistry.java",
		"AccessControlContext.java",
		"AccessController.java",
		"AlgorithmParameterGenerator.java",
		"AlgorithmParameterGeneratorSpi.java",
		"AlgorithmParameters.java",
		"AlgorithmParametersSpi.java",
		"AllPermission.java",
		"AuthProvider.java",
		"BasicPermission.java",
		"CodeSigner.java",
		"CodeSource.java",
		"DigestInputStream.java",
		"DigestOutputStream.java",
		"GuardedObject.java",
		"Identity.java"
	};

	private static final String[] LOG_MESSAGES = {
		"Loading bean definitions",
		"Finished creating instance of bean 'MS-SQL'",
		"Creating shared instance of singleton bean 'MySQL'",
		"Creating instance of bean 'MySQL'",
		"Eagerly caching bean 'MySQL' to allow for resolving potential circular references",
		"Finished creating instance of bean 'MySQL'",
		"Creating shared instance of singleton bean 'Oracle'",
		"Creating instance of bean 'Oracle'",
		"Eagerly caching bean 'Oracle' to allow for resolving potential circular references",
		"Finished creating instance of bean 'Oracle'",
		"Creating shared instance of singleton bean 'PostgreSQL'",
		"Creating instance of bean 'PostgreSQL'",
		"Eagerly caching bean 'PostgreSQL' to allow for resolving potential circular references",
		"Finished creating instance of bean 'PostgreSQL'"
	};

	public static void main(String[] args) {
		int numOfLines = DEFAULT_NUM_OF_LINES;
		if (args.length > 0) {
			try {
				numOfLines = Integer.parseInt(args[0]);
			} catch (NumberFormatException nfe) {
				// Ignore - keep default
			}
		}

		int waitTime = DEFAULT_WAIT;
		if (args.length > 1) {
			try {
				waitTime = Integer.parseInt(args[1]);
			} catch (NumberFormatException nfe) {
				// Ignore - keep default
			}
		}

		try {
			System.out.format("Lines: %-1d Sleep: %-1d\n", numOfLines, waitTime);
			for (int i=0; i < numOfLines; i++) {
				System.out.format("%-5s | %tF %<tT.%<tL | %-40s | %4d | %-1s\n", nextRand(LOG_LEVELS), System.currentTimeMillis(), nextRand(FILE_NAMES)
						, (int) (Math.random() * 500d), nextRand(LOG_MESSAGES));
				if (waitTime > 0) {
					Thread.sleep(waitTime);
				}
			}
		} catch (InterruptedException e) {
		}
	}
	private static String nextRand(String [] possibilities) {
		return possibilities[((int) (Math.random() * 1000d)) % possibilities.length];
	}
}
