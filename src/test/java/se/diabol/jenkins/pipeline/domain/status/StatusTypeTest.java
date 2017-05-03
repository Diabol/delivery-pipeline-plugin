/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package se.diabol.jenkins.pipeline.domain.status;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StatusTypeTest {

    @Test
    public void testValueOf() {
        assertEquals(StatusType.CANCELLED, StatusType.valueOf("CANCELLED"));
        assertEquals(StatusType.DISABLED, StatusType.valueOf("DISABLED"));
        assertEquals(StatusType.FAILED, StatusType.valueOf("FAILED"));
        assertEquals(StatusType.IDLE, StatusType.valueOf("IDLE"));
        assertEquals(StatusType.QUEUED, StatusType.valueOf("QUEUED"));
        assertEquals(StatusType.RUNNING, StatusType.valueOf("RUNNING"));
        assertEquals(StatusType.SUCCESS, StatusType.valueOf("SUCCESS"));
        assertEquals(StatusType.UNSTABLE, StatusType.valueOf("UNSTABLE"));
        assertEquals(StatusType.NOT_BUILT, StatusType.valueOf("NOT_BUILT"));
        assertEquals(StatusType.PAUSED_PENDING_INPUT, StatusType.valueOf("PAUSED_PENDING_INPUT"));
    }

    @Test
    public void testValue() {
        StatusType[] values = StatusType.values();
        assertNotNull(values);
        assertEquals(10, values.length);
    }
}
