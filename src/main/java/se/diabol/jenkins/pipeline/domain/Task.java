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
import jenkins.model.Jenkins;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
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

    public Task(String id, String name, String buildId, Status status, String link, boolean manual,
                TestResult testResult, List<String> downstreamTasks) {
        super(name);
        this.id = id;
        this.link = link;
        this.testResult = testResult;
        this.status = status;
        this.manual = manual;
        this.buildId = buildId;
        this.downstreamTasks = downstreamTasks;
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

        return new Task(project.getRelativeNameFrom(Jenkins.getInstance()), taskName, null, status,
                Util.fixNull(Jenkins.getInstance().getRootUrl()) + project.getUrl(), false, null, downStreamTasks);
    }

    public static Task getTask(Task task, AbstractBuild build, ItemGroup context) {
        AbstractProject project = ProjectUtil.getProject(task.getId(), context);
        Status status = SimpleStatus.resolveStatus(project, build);
        String link = build == null || status.isIdle() || status.isQueued() ? task.getLink() : Util.fixNull(Jenkins.getInstance().getRootUrl()) + build.getUrl();
        String buildId = build == null || status.isIdle() || status.isQueued() ? null : String.valueOf(build.getNumber());
        return new Task(task.getId(), task.getName(), buildId, status, link, task.isManual(), TestResult.getTestResult(build),
                task.getDownstreamTasks());
    }


    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(status).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Task && equalsSelf((Task) obj);
    }

    private boolean equalsSelf(Task o) {
        return new EqualsBuilder().append(id, o.id).append(status, o.status).appendSuper(super.equals(o)).isEquals();
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
