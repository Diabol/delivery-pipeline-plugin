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

import hudson.matrix.Axis;
import hudson.matrix.AxisList;
import hudson.matrix.MatrixConfiguration;
import hudson.matrix.MatrixProject;

import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Bug;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.WithoutJenkins;

import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;
import se.diabol.jenkins.pipeline.domain.task.Task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class StageTest {
    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    @Bug(22211)
    @WithoutJenkins
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

    /**
     *  A --> B --> C --> D --> E --> F --> G
     *           |     |    |
     *           |     -> H -
     *           |
     *           -> I --> J --> L --> M --> N
     *                 |    |
     *                 -> K -
     *
     */
    @Test
    @WithoutJenkins
    public void testSortByRowsCols2() {
        List<Stage> stages = new ArrayList<Stage>();
        Stage stageA = new Stage("A", Collections.<Task>emptyList());
        stageA.setRow(0);
        stageA.setColumn(0);
        Stage stageB = new Stage("B", Collections.<Task>emptyList());
        stageB.setRow(0);
        stageB.setColumn(1);
        Stage stageC = new Stage("C", Collections.<Task>emptyList());
        stageC.setRow(0);
        stageC.setColumn(2);
        Stage stageD = new Stage("D", Collections.<Task>emptyList());
        stageD.setRow(0);
        stageD.setColumn(3);
        Stage stageE = new Stage("E", Collections.<Task>emptyList());
        stageE.setRow(0);
        stageE.setColumn(4);
        Stage stageF = new Stage("F", Collections.<Task>emptyList());
        stageF.setRow(0);
        stageF.setColumn(5);
        Stage stageG = new Stage("G", Collections.<Task>emptyList());
        stageG.setRow(0);
        stageG.setColumn(6);
        Stage stageH = new Stage("H", Collections.<Task>emptyList());
        stageH.setRow(1);
        stageH.setColumn(3);
        Stage stageI = new Stage("I", Collections.<Task>emptyList());
        stageI.setRow(2);
        stageI.setColumn(2);
        Stage stageJ = new Stage("J", Collections.<Task>emptyList());
        stageJ.setRow(2);
        stageJ.setColumn(3);
        Stage stageK = new Stage("K", Collections.<Task>emptyList());
        stageK.setRow(3);
        stageK.setColumn(3);
        Stage stageL = new Stage("L", Collections.<Task>emptyList());
        stageL.setRow(2);
        stageL.setColumn(4);
        Stage stageM = new Stage("M", Collections.<Task>emptyList());
        stageM.setRow(2);
        stageM.setColumn(5);
        Stage stageN = new Stage("N", Collections.<Task>emptyList());
        stageN.setRow(2);
        stageN.setColumn(6);

        stages.add(stageA);
        stages.add(stageB);
        stages.add(stageF);
        stages.add(stageJ);
        stages.add(stageK);
        stages.add(stageL);
        stages.add(stageM);
        stages.add(stageN);
        stages.add(stageG);
        stages.add(stageH);
        stages.add(stageI);
        stages.add(stageC);
        stages.add(stageD);
        stages.add(stageE);



        Stage.sortByRowsCols(stages);

        assertEquals("A", stages.get(0).getName());
        assertEquals("B", stages.get(1).getName());
        assertEquals("C", stages.get(2).getName());
        assertEquals("D", stages.get(3).getName());
        assertEquals("E", stages.get(4).getName());
        assertEquals("F", stages.get(5).getName());
        assertEquals("G", stages.get(6).getName());
        assertEquals("H", stages.get(7).getName());
        assertEquals("I", stages.get(8).getName());
        assertEquals("J", stages.get(9).getName());
        assertEquals("L", stages.get(10).getName());
        assertEquals("M", stages.get(11).getName());
        assertEquals("N", stages.get(12).getName());
        assertEquals("K", stages.get(13).getName());


    }


    @Test
    @WithoutJenkins
    public void testFindStageForJob() {
        Task task1 = new Task(null, "build", "Build", StatusFactory.idle(), null, null, Collections.<String>emptyList(), true, "description");
        List<Stage> stages = Lists.newArrayList(new Stage("QA", Lists.newArrayList(task1)));
        assertNull(Stage.findStageForJob("nofind", stages));
        assertNotNull(Stage.findStageForJob("build", stages));
    }

    @Test
    @Bug(22654)
    public void testStageNameForMultiConfiguration() throws Exception {
        MatrixProject project = jenkins.createMatrixProject("Multi");
        project.setAxes(new AxisList(new Axis("axis", "foo", "bar")));
        project.addProperty(new PipelineProperty("task", "stage", ""));

        Collection<MatrixConfiguration> configurations = project.getActiveConfigurations();

        for (MatrixConfiguration configuration : configurations) {
            List<Stage> stages = Stage.extractStages(configuration, null);
            assertEquals(1, stages.size());
            Stage stage = stages.get(0);
            assertEquals("stage", stage.getName());

        }

    }
}
