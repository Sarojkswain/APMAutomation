package com.ca.apm.tests.utils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ca.apm.tests.config.BaseAppConfig;

public class CommonUtilsTest {
	private static String CONFIG_FILE_PATH = "/testdata/IntroscopeCollectorAgent.profile";

	@Test
	public void testUnexpectedErrorsListed() {
		try {
			List<String> errors = getErrorsList("/logs/UnexpectedErrors.txt");
			assertTrue(errors != null && errors.size() > 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void testExpectedErrorsNotListed() {
		try {
			List<String> errors = getErrorsList("/logs/ExpectedErrorsOnly.txt");
			assertTrue(errors != null && errors.size() == 0);
		} catch (Exception e) {
			e.printStackTrace();
			fail();
		}
	}

	public List<String> getErrorsList(String logFilePath) throws Exception {
		URL url = getClass().getResource(CONFIG_FILE_PATH);

		File file = new File(url.getFile());
		if (file.exists()) {
			String home = file.getParent();
			String logPath = new File(home, logFilePath).getCanonicalPath();
			DummyConfig config = new DummyConfig(file.getParent(), file.getCanonicalPath(), logPath);
			List<String> expectedErrorMessages = Arrays.asList(".*Socket closed.*",
			        ".*ARF Connection socket closed.*",
			        ".*IsengardServerConnectionManager.*Failed to disconnect agent bridge.*");
			List<String> errorRegexs = Arrays.asList("\\[ERROR\\]");

			List<String> errors = CommonUtils.findUnexpectedMessagesInLog(config, errorRegexs,
			        expectedErrorMessages);
			return errors;

		}

		return null;
	}

	public static class DummyConfig extends BaseAppConfig {
		private String logPath;

		public DummyConfig(String home, String configPath, String logPath) {
			super(home, configPath);
			this.logPath = logPath;
		}

		@Override
		public String getLogPath() {
			return logPath;
		}

		@Override
        public String getProperty(String key) {
	        // TODO Auto-generated method stub
	        return null;
        }

	}
}
