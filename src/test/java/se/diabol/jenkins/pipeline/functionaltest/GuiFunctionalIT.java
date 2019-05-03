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
package se.diabol.jenkins.pipeline.functionaltest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.Issue;
import org.jvnet.hudson.test.JenkinsRule;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import com.cloudbees.hudson.plugins.folder.Folder;

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.model.FreeStyleProject;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.StringParameterDefinition;
import hudson.model.View;
import hudson.plugins.view.dashboard.Dashboard;
import hudson.tasks.BuildTrigger;
import se.diabol.jenkins.pipeline.DeliveryPipelineView;

public class GuiFunctionalIT {

    private static final boolean DO_NOT_SHOW_UPSTREAM = false;
    private WebDriver webDriver;

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();
    private static final String NONE = null;

    @Before
    public void before() {
        webDriver = new ChromeDriver();
        //webDriver.
    }

    @After
    public void cleanUpWebDriver() {
        if (webDriver != null) {
            webDriver.close();
        }
    }

    @Test
    public void triggerManualBuild() throws Exception {
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Component", "A", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(specs);
        view.setAllowManualTriggers(true);

        jenkins.getInstance().rebuildDependencyGraph();

        jenkins.getInstance().addView(view);

        jenkins.buildAndAssertSuccess(projectA);

        DeliveryPipelinePage page =
                new DeliveryPipelinePage(webDriver, jenkins.getURL().toExternalForm(), "view/Pipeline");
        page.open();

        page.triggerManual("B0");
        jenkins.waitUntilNoActivity();
        assertNotNull(projectB.getLastBuild());
    }

    @Test
    public void triggerManualRebuild() throws Exception {
        final FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        final FreeStyleProject projectB = jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Component", "A", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(specs);
        view.setAllowManualTriggers(true);
        view.setAllowRebuild(true);

        jenkins.getInstance().rebuildDependencyGraph();

        jenkins.getInstance().addView(view);

        projectA.scheduleBuild(0, null);
        jenkins.waitUntilNoActivity();

        DeliveryPipelinePage page =
                new DeliveryPipelinePage(webDriver, jenkins.getURL().toExternalForm(), "view/Pipeline");
        page.open();

        page.triggerManual("B0");
        jenkins.waitUntilNoActivity();
        assertNotNull(projectB.getLastBuild());
        page.open();
        page.triggerRebuild("B0");
        jenkins.waitUntilNoActivity();

        assertEquals(2, projectB.getLastBuild().getNumber());
    }

    @Test
    public void shouldBeAbleToSetDeliveryPipelineViewAsJenkinsDefaultView() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");
        projectA.getPublishersList().add(new BuildPipelineTrigger("B", null));

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Component", "A", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(specs);
        view.setAllowManualTriggers(true);
        view.setAllowRebuild(true);

        jenkins.getInstance().rebuildDependencyGraph();

        jenkins.getInstance().addView(view);
        jenkins.getInstance().setPrimaryView(view);

        View all = jenkins.getInstance().getView("All");

        jenkins.getInstance().deleteView(all);

        projectA.scheduleBuild(0, null);
        jenkins.waitUntilNoActivity();

        NewJobPage newJobPage = new NewJobPage(webDriver, jenkins.getURL() + "view/Pipeline");
        newJobPage.open();
        Thread.sleep(2 * 1000);
        newJobPage.setJobName("NewJob");
        newJobPage.setFreeStyle();

        ConfigureJobPage configureJobPage = newJobPage.submit();
        configureJobPage.submit();

        assertNotNull(jenkins.getInstance().getItemByFullName("NewJob"));
    }

    @Test
    public void testTriggerNewParameterizedPipeline() throws Exception {

        FreeStyleProject start = jenkins.createFreeStyleProject("Start");
        start.addProperty(new ParametersDefinitionProperty(
                new StringParameterDefinition("key2", "value2")
                ));
        jenkins.createFreeStyleProject("End");
        start.getPublishersList().add(new BuildTrigger("End", true));

        jenkins.getInstance().rebuildDependencyGraph();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Component", "Start", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(specs);
        view.setAllowPipelineStart(true);

        jenkins.getInstance().addView(view);

        DeliveryPipelinePage page =
                new DeliveryPipelinePage(webDriver, jenkins.getURL().toExternalForm(), "view/Pipeline");
        page.open();
        page.triggerNewParameterizedPipelineBuild("0");

        jenkins.waitUntilNoActivity();

        assertNotNull(start.getLastBuild());
    }

    @Test
    public void testTriggerNewPipeline() throws Exception {

        FreeStyleProject start = jenkins.createFreeStyleProject("Start");
        jenkins.createFreeStyleProject("End");

        start.getPublishersList().add(new BuildTrigger("End", true));

        jenkins.getInstance().rebuildDependencyGraph();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Component", "Start", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(specs);
        view.setAllowPipelineStart(true);

        jenkins.getInstance().addView(view);

        DeliveryPipelinePage page =
                new DeliveryPipelinePage(webDriver, jenkins.getURL().toExternalForm(), "view/Pipeline");
        page.open();
        page.triggerNewPipelineBuild("0");

        jenkins.waitUntilNoActivity();

        assertNotNull(start.getLastBuild());
    }

    @Test
    public void testTriggerNewPipelineFolders() throws Exception {
        Folder folder =  jenkins.getInstance().createProject(Folder.class, "Folder");
        assertNotNull(folder);

        FreeStyleProject start = jenkins.createFreeStyleProject("Start");
        folder.createProject(FreeStyleProject.class, "End");

        start.getPublishersList().add(new BuildTrigger("End", true));

        jenkins.getInstance().rebuildDependencyGraph();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Component", "Start", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(specs);
        view.setAllowPipelineStart(true);

        folder.addView(view);

        DeliveryPipelinePage page =
                new DeliveryPipelinePage(webDriver, jenkins.getURL().toExternalForm(), "job/Folder/view/Pipeline");
        page.open();
        page.triggerNewPipelineBuild("0");

        jenkins.waitUntilNoActivity();

        assertNotNull(start.getLastBuild());
    }

    @Test
    @Issue("JENKINS-39856")
    public void jsPlumbShouldNotLeakMemoryOnDeliveryPipelinePage() throws Exception {
        FreeStyleProject start = jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");

        start.getPublishersList().add(new BuildTrigger("B", true));

        jenkins.getInstance().rebuildDependencyGraph();

        DeliveryPipelineView view = new DeliveryPipelineView("Pipeline");
        List<DeliveryPipelineView.ComponentSpec> specs = new ArrayList<>();
        specs.add(new DeliveryPipelineView.ComponentSpec("Component", "A", NONE, DO_NOT_SHOW_UPSTREAM));
        view.setComponentSpecs(specs);
        view.setAllowPipelineStart(true);

        jenkins.getInstance().addView(view);

        DeliveryPipelinePage page =
                new DeliveryPipelinePage(webDriver, jenkins.getURL().toExternalForm(), "view/Pipeline");
        page.open();
        String result = page.getJsPlumbUtilityVariable();

        jenkins.waitUntilNoActivity();

        assertNotNull(result);
        assertEquals("1", result);
    }

    @Test
    @Issue("JENKINS-39856")
    public void jsPlumbShouldNotLeakMemoryOnDeliveryPipelineDashboardPortlet() throws Exception {
        FreeStyleProject projectA = jenkins.createFreeStyleProject("A");
        jenkins.createFreeStyleProject("B");

        projectA.getPublishersList().add(new BuildTrigger("B", true));

        FreeStyleProject projectC = jenkins.createFreeStyleProject("C");
        jenkins.createFreeStyleProject("D");

        projectC.getPublishersList().add(new BuildTrigger("D", true));

        jenkins.getInstance().rebuildDependencyGraph();

        Dashboard view = new Dashboard("Dashboard");
        jenkins.getInstance().addView(view);
        view.updateByXml(new StreamSource(GuiFunctionalIT.class.getResourceAsStream(
                "/se/diabol/jenkins/pipeline/functionaltest/GuiFunctionalIT/DashboardViewPage.xml")));
        view.save();

        DeliveryPipelinePage page =
                new DeliveryPipelinePage(webDriver, jenkins.getURL().toExternalForm(), "view/Dashboard");
        page.open();
        String result = page.getJsPlumbUtilityVariable();

        jenkins.waitUntilNoActivity();

        assertNotNull(result);
        assertEquals("2", result);
    }

}
