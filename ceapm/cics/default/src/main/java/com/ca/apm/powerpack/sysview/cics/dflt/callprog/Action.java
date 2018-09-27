/*
 * Copyright (c) 2016 CA. All rights reserved.
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
 * MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT WILL CA BE
 * LIABLE TO THE END USER OR ANY THIRD PARTY FOR ANY LOSS OR DAMAGE, DIRECT OR
 * INDIRECT, FROM THE USE OF THIS SOFTWARE, INCLUDING WITHOUT LIMITATION, LOST
 * PROFITS, BUSINESS INTERRUPTION, GOODWILL, OR LOST DATA, EVEN IF CA IS
 * EXPRESSLY ADVISED OF SUCH LOSS OR DAMAGE.
 */

package com.ca.apm.powerpack.sysview.cics.dflt.callprog;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.cics.server.InvalidSystemIdException;
import com.ibm.cics.server.Program;
import com.ibm.cics.server.Task;
import org.apache.commons.lang3.Validate;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * This class represent an action to be taken by a CICS program.
 * <p>The various types of actions are available through factory methods:
 * {@link #programCall(String, List, String)}, {@link #delay(int)}, {@link #abend(String)}.
 * <p>All of the different actions are within one class (instead of separate ones) on purpose, to
 * simplify the serialized json format and make it as simple for users as possible.
 */
public class Action implements Consumer<Task> {
    /** {@code Charset} used for the communication to/from executed programs */
    public static final Charset DATA_ENCODING = Charset.forName("US-ASCII");

    /**
     * Specifies the number of times the action will be executed.
     * Can be {@code null} in which case the action is executed once.
     */
    private Integer count;

    /**
     * Specifies the name of a program to execute.
     */
    private String program;

    /**
     * Specifies an explicit system to execute a program on.
     * If {@code null} the program is executed locally.
     */
    private String sysId;

    /** Collection of actions to be executed by the executed program. */
    private List<Action> subActions;

    /** Contains a delay in microseconds. */
    private Integer delay;

    /** Contains an Abend code */
    private String abend;

    /**
     * Factory for a Program Call action.
     * <p>This action calls (links to) the specified program, passing it the specified sub-actions.
     *
     * @param program Program name.
     * @param subActions List of action to be executed by the called program.
     * @param sysId System Id where the program should be executed. If {@code null} the program
     *              is executed on the local region.
     * @return New {@link Action} instance.
     */
    public static Action programCall(String program, List<Action> subActions, String sysId) {
        Validate.notEmpty(program);
        Validate.inclusiveBetween(1, 8, program.length());

        if (sysId != null) {
            Validate.notEmpty(sysId);
            Validate.inclusiveBetween(1, 4, sysId.length());
        }

        Action action = new Action();
        action.program = program;
        action.sysId = sysId;
        if (subActions != null) {
            action.subActions = subActions;
        } else {
            // We want to have the list initialized as we would otherwise have to deal with it in
            // the program execution explicitly during (de)serialization.
            action.subActions = Collections.emptyList();
        }

        return action;
    }

    /**
     * Factory for a Program Call action.
     * <p>This overload calls a local program without any sub-actions.
     *
     * @param program Program name.
     * @return New {@link Action} instance.
     */
    public static Action programCall(String program) {
        return programCall(program, null, null);
    }

    /**
     * Factory for a Delay action.
     * <p>This action will simply wait for the specified amount of time.
     *
     * @param delay Delay time in microseconds.
     * @return New {@link Action} instance.
     */
    public static Action delay(int delay) {
        Validate.inclusiveBetween(1, Integer.MAX_VALUE, delay);

        Action action = new Action();
        action.delay = delay;

        return action;
    }

    /**
     * Factory for an Abend action.
     * <p>This action will trigger an Abend with the specified code.
     *
     * @param abend Abend code to use.
     * @return New {@link Action} instance.
     */
    public static Action abend(String abend) {
        Validate.notEmpty(abend);
        Validate.inclusiveBetween(1, 4, abend.length());

        Action action = new Action();
        action.abend = abend;

        return action;
    }

    /**
     * Modifies the number of times the action is to be executed.
     *
     * @param count Number of repetitions.
     * @return The {@link Action} instance the method was called on.
     */
    public Action withRepetition(int count) {
        Validate.inclusiveBetween(1, Integer.MAX_VALUE, count);

        this.count = count;
        return this;
    }

    @Override
    public void accept(Task task) {
        final int realCount = (count == null ? 1 : count);
        for (int i = 0; i < realCount; ++i) {
            if (program != null) {
                executeProgram(task);
            } else if (delay != null) {
                executeDelay(task);
            } else if (abend != null) {
                executeAbend(task);
            }
        }
    }

    public Integer getCount() {
        return count == null ? 1 : count;
    }

    public String getProgram() {
        return program;
    }

    public String getSysId() {
        return sysId;
    }

    public List<Action> getSubActions() {
        return subActions;
    }

    public Integer getDelay() {
        return delay;
    }

    public String getAbend() {
        return abend;
    }

    public String toString() {
        if (program != null) {
            return "Program " + program + (sysId == null ? "" : "@" + sysId);
        } else if (delay != null) {
            return "Delay " + delay + "us";
        } else if (abend != null) {
            return "Abend " + abend;
        } else {
            return "NOP";
        }
    }

    private void executeProgram(Task task) {
        assert program != null;
        assert subActions != null;

        final Program target = new Program();
        target.setName(program);
        if (sysId != null) {
            try {
                target.setSysId(sysId);
            } catch (InvalidSystemIdException e) {
                task.err.println("Failed to set target SysId to '" + sysId + "': "
                    + e.getLocalizedMessage());
            }
        }

        final String targetName = target.getName() + (target.getSysId().isEmpty()
            ? "" : "@" + target.getSysId());
        task.out.println("[" + task.getProgramName() + "] * Program " + targetName);

        Gson gson = new GsonBuilder().create();
        byte[] serializedActions = gson.toJson(subActions).getBytes(DATA_ENCODING);

        try {
            target.link(serializedActions);
        } catch (Exception e) {
            task.err.println("Exception while calling '" + targetName + "': "
                + e.getLocalizedMessage());
        }
    }

    private void executeDelay(Task task) {
        assert delay != null;

        try {
            task.out.println("[" + task.getProgramName() + "] * Delay " + delay + "us");

            final long millis = delay / 1_000;
            final int nanos = Math.toIntExact((delay - millis * 1_000) * 1_000);
            Thread.sleep(millis, nanos);
        } catch (InterruptedException e) {
            task.err.println("Interrupted during Delay: " + e.getLocalizedMessage());
        }
    }

    private void executeAbend(Task task) {
        assert abend != null;

        task.out.println("[" + task.getProgramName() + "] * Abend " + abend);
        task.abend(abend);
    }
}
