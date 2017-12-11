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
package se.diabol.jenkins.workflow.model;

import org.junit.Test;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.*;

public class ProgressTest {

    @Test
    public void shouldCalculateProgressWhenHalfwayThroughEstimatedDuration() {
        long buildTimestamp = System.currentTimeMillis() - 10000L;
        long estimatedDuration = 20000L;
        int progress = Progress.calculate(buildTimestamp, estimatedDuration);
        assertThat(progress, greaterThanOrEqualTo(50));
        assertThat(progress, lessThan(60));
    }

    @Test
    public void shouldCalculateProgressWhenExceedingEstimatedDuration() {
        long buildTimestamp = System.currentTimeMillis() - 11000L;
        long estimatedDuration = 5000L;
        int progress = Progress.calculate(buildTimestamp, estimatedDuration);
        assertThat(progress, greaterThan(100));
        assertThat(progress, greaterThanOrEqualTo(200));
    }
}
