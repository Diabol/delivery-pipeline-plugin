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
package se.diabol.jenkins.pipeline.sort;

import org.junit.Test;

import static org.junit.Assert.*;

public class BuildStartTimeComparatorTest {

    @Test
    public void shouldCompareBuildTimes() throws Exception {
        long first = 10L;
        long second = 20L;
        assertTrue(new BuildStartTimeComparator().compare(first, second) < 0);

        first = 30L;
        assertTrue(new BuildStartTimeComparator().compare(first, second) > 0);

        first = second;
        assertTrue(new BuildStartTimeComparator().compare(first, second) == 0);
    }

}