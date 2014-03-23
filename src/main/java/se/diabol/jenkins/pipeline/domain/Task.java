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

import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;
import se.diabol.jenkins.pipeline.util.BuildUtil;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;
import static se.diabol.jenkins.pipeline.domain.status.StatusFactory.disabled;
import static se.diabol.jenkins.pipeline.domain.status.StatusFactory.idle;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Task extends AbstractItem {
    private final String id;
    private final String link;
    private final TestResult testResult;
    private final Status status;
    private final boolean manual;
    private final String buildId;
    private final List<String> downstreamTasks;

    public Task(String id, String name, Status status, String link, List<String> downstreamTasks) {
        super(name);
        this.id = id;
        this.link = link;
        this.testResult = null;
        this.status = status;
        this.manual = false;
        this.buildId = null;
        this.downstreamTasks = downstreamTasks;
    }


    public Task(Task task, String buildId, Status status, String link, boolean manual,
                    TestResult testResult) {
        super(task.getName());
        this.id = task.id;
        this.downstreamTasks = task.getDownstreamTasks();
        this.buildId = buildId;
        this.status = status;
        this.link = link;
        this.manual = manual;
        this.testResult = testResult;
    }

    @Exported
    public boolean isManual() {
        return manual;
    }

    @Exported
    public String getBuildId() {
        return buildId;
    }

    @Exported
    public String getId() {
        return id;
    }

    @Exported
    public String getLink() {
        return link;
    }

    @Exported
    @SuppressWarnings("unused")
    public TestResult getTestResult() {
        return testResult;
    }

    @Exported
    public Status getStatus() {
        return status;
    }

    @Exported
    public List<String> getDownstreamTasks() {
        return downstreamTasks;
    }

    public static Task getPrototypeTask(AbstractProject project) {
        PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
        String taskName = property != null && !isNullOrEmpty(property.getTaskName())
                ? property.getTaskName() : project.getDisplayName();
        Status status = project.isDisabled() ? disabled() : idle();
        List<AbstractProject> downstreams = ProjectUtil.getDownstreamProjects(project);
        List<String> downStreamTasks = new ArrayList<String>();
        for (AbstractProject downstreamProject : downstreams) {
            downStreamTasks.add(downstreamProject.getRelativeNameFrom(Jenkins.getInstance()));
        }

        return new Task(project.getRelativeNameFrom(Jenkins.getInstance()), taskName, status,
                Util.fixNull(Jenkins.getInstance().getRootUrl()) + project.getUrl(), downStreamTasks);
    }

    public Task getLatestTask(ItemGroup context, AbstractBuild firstBuild) {
        AbstractProject<?, ?> project = getProject(this, context);
        AbstractBuild build = match(project.getBuilds(), firstBuild);

        Status status = SimpleStatus.resolveStatus(project, build);
        String link = build == null || status.isIdle() || status.isQueued() ? this.getLink() : Util.fixNull(Jenkins.getInstance().getRootUrl()) + build.getUrl();
        String buildId = build == null || status.isIdle() || status.isQueued() ? null : String.valueOf(build.getNumber());
        return new Task(this, buildId, status, link, this.isManual(), TestResult.getTestResult(build));
    }

    public Task getAggregatedTask(AbstractBuild versionBuild, ItemGroup context) {
        AbstractProject<?, ?> taskProject = getProject(this, context);
        AbstractBuild currentBuild = match(taskProject.getBuilds(), versionBuild);
        if (currentBuild != null) {
            Status status = SimpleStatus.resolveStatus(taskProject, currentBuild);
            String link = Util.fixNull(Jenkins.getInstance().getRootUrl()) + currentBuild.getUrl();
            return new Task(this, String.valueOf(currentBuild.getNumber()), status, link, this.isManual(), TestResult.getTestResult(currentBuild));
        } else {
            return new Task(this, null, StatusFactory.idle(), this.getLink(), this.isManual(), null);
        }
    }

    private AbstractProject getProject(Task task, ItemGroup context) {
        return ProjectUtil.getProject(task.getId(), context);
    }


    /**
     * Returns the build for a projects that has been triggered by the supplied upstream project.
     */
    private AbstractBuild match(RunList<? extends AbstractBuild> runList, AbstractBuild firstBuild) {
        if (firstBuild != null) {
            for (AbstractBuild currentBuild : runList) {
                if (firstBuild.equals(BuildUtil.getFirstUpstreamBuild(currentBuild, firstBuild.getProject()))) {
                    return currentBuild;
                }
            }
        }
        return null;
    }


    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("status", getStatus())
                .toString();
    }
}
