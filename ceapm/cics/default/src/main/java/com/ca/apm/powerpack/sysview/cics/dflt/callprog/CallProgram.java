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
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.MalformedJsonException;
import com.ibm.cics.server.CommAreaHolder;
import com.ibm.cics.server.Task;

import java.util.List;

/**
 * This program expects a {@code List<Action>} object serialized into json in
 * {@link Action#DATA_ENCODING} encoding. The list can be empty. All the actions are executed
 * synchronously.
 */
public class CallProgram {
    public static void main(CommAreaHolder cah) {
        Task task = Task.getTask();
        if (task == null) {
            System.err.println("Unable to obtain task object");
            return;
        }

        if (cah.getValue() == null) {
            task.err.println("No COMMAREA provided");
            return;
        }

        task.out.println("[" + task.getProgramName() + "]");
        String actionsData = new String(cah.getValue(), Action.DATA_ENCODING);

        List<Action> actions = null;
        try {
            // This is to make the program more resilient to random trailing data in the commarea
            final int start = actionsData.indexOf('[');
            final int end = actionsData.lastIndexOf(']');
            if (start == -1 || end == -1) {
                throw new MalformedJsonException("Missing array in data");
            }

            actionsData = actionsData.substring(start, end + 1);

            Gson gson = new GsonBuilder().create();
            actions = gson.fromJson(actionsData, new TypeToken<List<Action>>() {}.getType());
        } catch (Exception e) {
            task.out.println("Caught exception while parsing data: " + e.getLocalizedMessage());
            task.out.println("Data: " + actionsData);
        }

        if (actions != null) {
            actions.forEach(a -> a.accept(task));
        }
    }
}
