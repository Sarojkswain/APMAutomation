/**
 *
 */
package com.ca.apm.tests.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * @author Replay Solutions
 *
 */
public class ReplayMarker
{
	/**
	 *
	 * @param host - Name of the host where app server is recording
	 * @param name - Marker name
	 * @param cookie - _replay_transaction_details_ cookie value from failed transaction
	 * @return - Returns null for no error or a string object containing the error.
	 */
	public static String addMarker(String host, String name, String cookie, String port)
	{
		String replayHome = System.getenv("REPLAY_HOME");
		if (replayHome == null)
		{
			return "REPLAY_HOME environment variable is not set";
		}

		String[] replayCmd = null;

		// Use this for Linux
//		if (cookie != null)
//		{
//			replayCmd = new String[]{replayHome + File.separator + "replay", "-host", host, "-port", port, "marker", name, cookie};
//		}
//		else
//		{
//			replayCmd = new String[]{replayHome + File.separator + "replay", "-host", host, "-port", port, "marker", name};
//		}

		// Use this for Windows
		if (cookie != null)
		{
			replayCmd = new String[]{replayHome + File.separator + "replay.exe", "-host", host, "-port", port, "marker", name, cookie};
		}
		else
		{
			replayCmd = new String[]{replayHome + File.separator + "replay.exe", "-host", host, "-port", port, "marker", name};
		}

		try
		{
			String line;
			Process p = Runtime.getRuntime().exec(replayCmd);
			BufferedReader input = new BufferedReader(new InputStreamReader(
					p.getInputStream()));
			p.waitFor();
			boolean created = false;
			while ((line = input.readLine()) != null)
			{
				if (line.contains("created successfully"))
				{
					created = true;
					break;
				}
			}
			input.close();
			if (!created)
			{
				return line;
			}
		}
		catch (Throwable t)
		{
			return t.getMessage();
		}
		return null;
	}
}





