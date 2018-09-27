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

package com.ca.apm.commons.flow;

import com.ca.apm.automation.action.flow.IBuilder;

import org.apache.http.util.Args;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Execution class
 *
 * @author pojja01
 */
public class BackgroundExecution {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackgroundExecution.class);
  private static final long BUFFERED_READER_TIMEOUT = 500;
  private static final int DEFAULT_BED_IN_TIME = 10;
  public static final int EXIT_SUCCESS = 0;
  public static final int EXIT_FAILURE = -1;
  private final File workingDir;
  private final List<String> command = new LinkedList<>();
  private final List<String> args = new ArrayList<>();
  private final int bedInTime;

  private BackgroundExecution(Builder builder) {
    workingDir = builder.workingDir;
    command.add(builder.command);
    args.addAll(builder.args);
    bedInTime = builder.bedInTime;
  }

  public int go() throws CommandLineException, InterruptedException {
    LOGGER.info("Executing command");
    LOGGER.info(" Working Directory : {}", workingDir);

    if (!args.isEmpty()) {
      for (String s : args) {
        LOGGER.info(" arg : " + s);
      }
    }
    if (!args.isEmpty()) {
      command.addAll(args);
    }
    LOGGER.info(" Command : " + command);

    try {
      ProcessBuilder pb = initProcessBuilder(command).directory(workingDir).redirectErrorStream(true);
      Process subprocess = pb.start();
      long processStartTime = System.currentTimeMillis();
      LOGGER.info("Process started at " + Long.toString(processStartTime));
      try (InputStream inputStream = subprocess.getInputStream();
           InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
           BufferedReader br = new BufferedReader(inputStreamReader)) {

        while ((isAlive(subprocess) && isBeddingIn(processStartTime)) || br.ready()) {
          while (br.ready()) {
            LOGGER.info(br.readLine());
          }
          Thread.sleep(BUFFERED_READER_TIMEOUT);
        }

        if (isAlive(subprocess)) {
          LOGGER.debug("Subprocess bedded in, returning success.");
          return EXIT_SUCCESS;
        }
        else {
          LOGGER.debug("Subprocess no-longer running after %d seconds, returning failure.", (System.currentTimeMillis() - processStartTime) /1000);
          return EXIT_FAILURE;
        }

      }
    } catch (IOException e) {
      LOGGER.error("IOException", e);
      return EXIT_FAILURE;
    }
  }

  private boolean isAlive(Process p) {
    try {
      p.exitValue();
      return false;
    } catch (IllegalThreadStateException e) {
      return true;
    }
  }

  private boolean isBeddingIn(long processStartTime) {
    return System.currentTimeMillis() < (processStartTime + (bedInTime * 1000));
  }

  /**
   * Factory method
   */
  private ProcessBuilder initProcessBuilder(List<String> command) {
    return new ProcessBuilder(command);
  }

  public static class Builder implements IBuilder<BackgroundExecution> {

    private final File workingDir;
    private final String command;
    private int bedInTime = DEFAULT_BED_IN_TIME;
    private final List<String> args = new ArrayList<>();

    public Builder(File workingDir, String command) {
      this.workingDir = workingDir;
      this.command = command;
    }

    @Override
    public BackgroundExecution build() {
      BackgroundExecution execution = new BackgroundExecution(this);
      Args.notNull(execution.workingDir, "Working dir");
      Args.notNull(execution.command, "Execution command");
      return execution;
    }

    public Builder bedInTime(int value) {
      bedInTime = value;
      return this;
    }

    public Builder args(List<String> args) {
      Args.notNull(args, "Argument array");
      this.args.addAll(args);
      return this;
    }
  }
}
