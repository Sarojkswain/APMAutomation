/*
 * Copyright (c) 2015 CA. All rights reserved.
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

package com.ca.apm.automation.utils.mainframe.sysview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;

import com.ca.apm.automation.utils.mainframe.sysview.Sysview.Rc;

public class RcTest {

    @Test
    public void testFromValue() {
        for (Rc defined : Rc.values()) {
            Rc returned = Rc.fromValue(defined.getValue());
            assertEquals(defined, returned);
        }
        
        for (int rawValue : Arrays.asList(-999, -1, 999)) {
            try {
                Rc.fromValue(rawValue);
                fail();
            } catch (IllegalArgumentException expectedException) {
                // expected
            } catch (Exception unexpectedException) {
                fail();
            }
        }
    }

    @Test
    public void testGetValue() {
        for (Rc defined : Rc.values()) {
            int rawValue = defined.getValue();
            Rc returned = Rc.fromValue(rawValue);
            assertEquals(defined, returned);
            assertEquals(defined.getValue(), returned.getValue());
        }
    }

    @Test
    public void testGetOkValues() {
        Collection<Rc> okValues = Rc.getOkValues();
        assertNotNull(okValues);
        assertFalse(okValues.isEmpty());

        for (Rc defined : Rc.values()) {
            if (okValues.contains(defined)) {
                assertTrue(defined.isOk());
            } else {
                assertFalse(defined.isOk());
            }
        }
    }

    @Test
    public void testIsOk() {
        assertTrue(Rc.OK_NOMSG.isOk());
        assertTrue(Rc.OK_INFO_MSG.isOk());
        assertTrue(Rc.OK_ACTION_MSG.isOk());
        assertFalse(Rc.WARN.isOk());
        assertFalse(Rc.ERROR.isOk());
        assertFalse(Rc.TERM.isOk());
    }
}
