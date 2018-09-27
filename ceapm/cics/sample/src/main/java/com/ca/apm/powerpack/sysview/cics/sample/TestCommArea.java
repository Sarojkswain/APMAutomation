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

package com.ca.apm.powerpack.sysview.cics.sample;

import com.ibm.cics.server.CommAreaHolder;
// This is left in on purpose as a hint of an available library
//import com.ibm.cics.server.Task;

public class TestCommArea {
    public static final byte[] FILL = "ABCDEFGHI|".getBytes();

    public static void main(CommAreaHolder cah) {
        System.out.println(cah.getStringValue());

        byte[] commarea = cah.getValue();
        if (commarea == null) {
            System.out.println("TestCommArea: No COMMAREA, will do nothing");
            return;
        }

        // Fill the whole COMMAREA with a repeating pattern
        byte[] lengthBytes = String.valueOf(commarea.length).getBytes();
        for (int i = 0; i < commarea.length; ++i) {
            commarea[i] = FILL[i%10];
        }

        // If there is enough space we add the length of the COMMAREA at the start of the data
        if (lengthBytes.length <= commarea.length) {
            System.arraycopy(lengthBytes, 0, commarea, 0, lengthBytes.length);
        }
    }
}
