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
package se.diabol.jenkins.pipeline.domain.task;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.ItemGroup;
import jenkins.model.Jenkins;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.AbstractItem;
import se.diabol.jenkins.pipeline.domain.results.StaticAnalysisResult;
import se.diabol.jenkins.pipeline.domain.results.TestResult;
import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
import se.diabol.jenkins.pipeline.domain.status.Status;
import se.diabol.jenkins.pipeline.token.TokenUtils;
import se.diabol.jenkins.pipeline.util.BuildUtil;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.base.Strings.isNullOrEmpty;
import static se.diabol.jenkins.pipeline.domain.status.StatusFactory.disabled;
import static se.diabol.jenkins.pipeline.domain.status.StatusFactory.idle;
import static com.google.common.base.Objects.toStringHelper;


@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Task extends AbstractItem {
    private final String id;
    private final String link;
    private final List<TestResult> testResults;
    private final List<StaticAnalysisResult> staticAnalysisResults;
    private final Status status;
    private final ManualStep manual;
    private final String buildId;
    private final List<String> downstreamTasks;
    private final boolean initial;
    private final String description;
    private final AbstractProject project;

    public Task(AbstractProject project, String id, String name, Status status, String link,
            ManualStep manual, List<String> downstreamTasks, boolean initial,
            String description) {
        super(name);
        this.id = id;
        this.link = link;
        this.testResults = null;
        this.staticAnalysisResults = null;
        this.status = status;
        this.manual = manual;
        this.buildId = null;
        this.downstreamTasks = downstreamTasks;
        this.initial = initial;
        this.description = description;
        this.project = project;
    }

    public Task(Task task, String taskName, String buildId, Status status, String link, ManualStep manual,
            List<TestResult> testResults, List<StaticAnalysisResult> staticAnalysisResults,
            String description) {
        super(taskName);
        this.id = task.id;
        this.link = link;
        this.testResults = testResults;
        this.staticAnalysisResults = staticAnalysisResults;
        this.status = status;
        this.manual = manual;
        this.buildId = buildId;
        this.downstreamTasks = task.getDownstreamTasks();
        this.initial = task.isInitial();
        this.description = description;
        this.project = task.project;
    }

    @Exported
    public ManualStep getManualStep() {
        return manual;
    }

    @Exported
    public boolean isManual() {
        return manual != null;
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
    public List<TestResult> getTestResults() {
        return testResults;
    }

    @Exported
    public List<StaticAnalysisResult> getStaticAnalysisResults() {
        return staticAnalysisResults;
    }

    @Exported
    public Status getStatus() {
        return status;
    }

    @Exported
    public List<String> getDownstreamTasks() {
        return downstreamTasks;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    @Exported
    public boolean isRebuildable() {
        if (initial) {
            return false;
        }
        if (status.isRunning() || status.isIdle() || status.isNotBuilt() || status.isQueued() || status.isDisabled()) {
            return false;
        } else {
            return project.hasPermission(Item.BUILD);
        }
    }

    public boolean isInitial() {
        return initial;
    }

    public static Task getPrototypeTask(AbstractProject project, boolean initial) {
        PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
        String taskName = property != null && !isNullOrEmpty(property.getTaskName())
                ? property.getTaskName() : project.getDisplayName();

        if (property == null && project.getParent() instanceof AbstractProject) {
            property = (PipelineProperty) ((AbstractProject) project.getParent()).getProperty(PipelineProperty.class);
            taskName = property != null && !isNullOrEmpty(property.getTaskName())
                    ? property.getTaskName() + " " + project.getName() : project.getDisplayName();
        }

        String descriptionTemplate = property != null && !isNullOrEmpty(property.getDescriptionTemplate())
                ? property.getDescriptionTemplate() : "";

        Status status = project.isDisabled() ? disabled() : idle();
        List<AbstractProject> downStreams = ProjectUtil.getDownstreamProjects(project);
        List<String> downStreamTasks = new ArrayList<String>();
        for (AbstractProject downstreamProject : downStreams) {
            downStreamTasks.add(downstreamProject.getRelativeNameFrom(Jenkins.getInstance()));
        }
        return new Task(project, project.getRelativeNameFrom(Jenkins.getInstance()), taskName, status,
                project.getUrl(), ManualStep.resolveManualStep(project), downStreamTasks, initial, descriptionTemplate);
    }

    public Task getLatestTask(ItemGroup context, AbstractBuild firstBuild) {
        AbstractProject<?, ?> project = getProject(this, context);
        AbstractBuild<?, ?> build = null;
        if (!ProjectUtil.isQueued(project, firstBuild)) {
            build = BuildUtil.match(project.getBuilds(), firstBuild);
        }

        final Status taskStatus = SimpleStatus.resolveStatus(project, build, firstBuild);
        final ManualStep manualStep = ManualStep.getManualStepLatest(project, build, firstBuild);

        return new Task(this,
                        resolveTaskName(project, getExpandedName(build)),
                        resolveBuildId(taskStatus, build),
                        taskStatus,
                        resolveTaskLink(taskStatus, build),
                        manualStep,
                        TestResult.getResults(build),
                        StaticAnalysisResult.getResults(build),
                        getBuildDescription(build));
    }

    public Task getAggregatedTask(AbstractBuild versionBuild, ItemGroup context) {
        AbstractProject<?, ?> taskProject = getProject(this, context);
        AbstractBuild<?, ?> build = BuildUtil.match(taskProject.getBuilds(), versionBuild);

        final Status taskStatus = SimpleStatus.resolveStatus(taskProject, build, null);
        final ManualStep manualStep = this.getManualStep();

        return new Task(this,
                        resolveTaskName(project, getExpandedName(build)),
                        resolveBuildId(taskStatus, build),
                        taskStatus,
                        resolveTaskLink(taskStatus, build),
                        manualStep,
                        TestResult.getResults(build),
                        StaticAnalysisResult.getResults(build),
                        getBuildDescription(build));
    }

    private String getBuildDescription(AbstractBuild<?, ?> build) {
        return TokenUtils.decodedTemplate(build, resolveBuildDescription(build));
    }

    private String resolveTaskName(AbstractProject<?, ?> project, String name) {
        return (TokenUtils.stringIsNotEmpty(name) ? name : project.getDisplayName());
    }

    private String getExpandedName(AbstractBuild<?, ?> build) {
        return TokenUtils.decodedTemplate(build, this.getName());
    }

    private String resolveTaskLink(Status taskStatus, AbstractBuild build) {
        String taskLink = this.getLink();
        if (build != null && !taskStatus.isIdle() && !taskStatus.isQueued()) {
            if (taskStatus.isRunning()) {
                taskLink = build.getUrl() + "console";
            } else {
                taskLink = build.getUrl();
            }
        }
        return taskLink;
    }

    private String resolveBuildId(Status taskStatus, AbstractBuild build) {
        String taskBuildId = null;
        if (build != null && !taskStatus.isIdle() && !taskStatus.isQueued()) {
            taskBuildId = String.valueOf(build.getNumber());
        }
        return taskBuildId;
    }

    private String resolveBuildDescription(AbstractBuild build) {
        String buildDescription = this.getDescription();
        if (isNullOrEmpty(buildDescription) && build != null) {
            buildDescription = build.getDescription();
        }
        return buildDescription;
    }

    private AbstractProject getProject(Task task, ItemGroup context) {
        return ProjectUtil.getProject(task.getId(), context);
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", id)
                .add("link", link)
                .add("testResults", testResults)
                .add("staticAnalysisResults", staticAnalysisResults)
                .add("status", status)
                .add("manual", manual)
                .add("buildId", buildId)
                .add("downstreamTasks", downstreamTasks).toString();
    }
}
