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
package se.diabol.jenkins.pipeline.domain;

import com.google.common.collect.Lists;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class StageTest {

    @Test
    @Bug(22211)
    public void testSortByRowsCols() {
        List<Stage> stages = new ArrayList<Stage>();
        Stage stage1 = new Stage("1", Collections.<Task>emptyList());
        stage1.setRow(3);
        stage1.setColumn(2);
        Stage stage2 = new Stage("2", Collections.<Task>emptyList());
        stage2.setRow(3);
        stage2.setColumn(3);
        Stage stage3 = new Stage("3", Collections.<Task>emptyList());
        stage3.setRow(1);
        stage3.setColumn(1);
        Stage stage4 = new Stage("4", Collections.<Task>emptyList());
        stage4.setRow(2);
        stage4.setColumn(1);
        Stage stage5 = new Stage("5", Collections.<Task>emptyList());
        stage5.setRow(1);
        stage5.setColumn(2);

        stages.add(stage1);
        stages.add(stage2);
        stages.add(stage3);
        stages.add(stage4);
        stages.add(stage5);

        Stage.sortByRowsCols(stages);

        assertEquals("3", stages.get(0).getName());
        assertEquals("5", stages.get(1).getName());
        assertEquals("4", stages.get(2).getName());
        assertEquals("1", stages.get(3).getName());
        assertEquals("2", stages.get(4).getName());


    }

    @Test
    public void testFindStageForJob() {
        Task task1 = new Task("build", "Build", StatusFactory.idle(), null, Collections.<String>emptyList());
        List<Stage> stages = Lists.newArrayList(new Stage("QA", Lists.newArrayList(task1)));
        assertNull(Stage.findStageForJob("nofind", stages));
        assertNotNull(Stage.findStageForJob("build", stages));
    }
}
