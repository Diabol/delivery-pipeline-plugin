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
package se.diabol.jenkins.workflow.util;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import org.joda.time.DateTime;
import org.junit.Test;
import se.diabol.jenkins.workflow.api.Run;
import se.diabol.jenkins.workflow.api.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UtilTest {

    @Test
    public void shouldGetRunById() {
        Run run1 = createRun("1");
        Run run2 = createRun("2");
        Run run3 = createRun("3");
        Run run4 = createRun("4");
        List<Run> runs = Arrays.asList(run1, run2, run3, run4);

        assertThat(Util.getRunById(runs, 3), is(run3));
    }

    private static Run createRun(String number) {
        return new Run(number, "run" + number, "PENDING", null, null, 1000L, null);
    }

    @Test
    public void getRunByIdShouldReturnNullForEmptyList() {
        assertThat(Util.getRunById(null, 35), nullValue());
        assertThat(Util.getRunById(Collections.<Run>emptyList(), 97), nullValue());
    }

    @Test
    public void getRunByIdShouldReturnNullForUnrecognizedBuildNumber() {
        assertThat(Util.getRunById(Arrays.asList(createRun("1"), createRun("2")), 53), nullValue());
    }

    @Test
    public void shouldReturnHeadOfList() {
        List<String> list = Arrays.asList("1", "2", "3");
        assertThat(Util.head(list), is("1"));
    }

    @Test
    public void shouldReturnNullHeadForEmptyList() {
        assertThat(Util.head(null), nullValue());
        assertThat(Util.head(Collections.emptyList()), nullValue());
    }

    private static List<Stage> stageFixture(Long duration) {
        List<Stage> stages = new ArrayList<Stage>(5);
        for (int i = 1; i <= 5; i = i + 1) {
            stages.add(new Stage(
                    "2014-04-27_20-40-00",
                    "Stage" + i,
                    "SUCCESS",
                    new DateTime(System.currentTimeMillis()),
                    duration));
        }
        return stages;
    }
}
