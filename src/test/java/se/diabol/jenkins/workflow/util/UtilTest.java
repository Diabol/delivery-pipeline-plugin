package se.diabol.jenkins.workflow.util;

import com.google.api.client.util.DateTime;
import org.junit.Test;
import se.diabol.jenkins.workflow.api.Run;
import se.diabol.jenkins.workflow.api.Stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

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
        return new Run(null, number, "run" + number, "PENDING", null, null, 1000L, null);
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
            stages.add(new Stage(Collections.<String, Object>emptyMap(),
                    "2014-04-27_20-40-00",
                    "Stage" + i,
                    "SUCCESS",
                    new DateTime(System.currentTimeMillis()),
                    duration));
        }
        return stages;
    }
}
