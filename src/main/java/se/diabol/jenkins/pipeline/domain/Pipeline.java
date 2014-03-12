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

import com.google.common.collect.ImmutableList;
import hudson.Util;
import hudson.model.*;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.status.SimpleStatus;
import se.diabol.jenkins.pipeline.domain.status.StatusFactory;
import se.diabol.jenkins.pipeline.util.BuildUtil;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;
import se.diabol.jenkins.pipeline.util.StageUtil;

import java.util.*;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.singleton;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Pipeline extends AbstractItem {
    private List<Stage> stages;

    private String version;

    private List<Trigger> triggeredBy;
    private Set<UserInfo> contributors;

    private boolean aggregated;

    private String timestamp;

    private List<Change> changes;

    public Pipeline(String name,
                    String version,
                    String timestamp,
                    List<Trigger> triggeredBy,
                    Set<UserInfo> contributors,
                    List<Stage> stages, boolean aggregated) {
        super(name);
        this.version = version;
        this.triggeredBy = triggeredBy;
        this.contributors = contributors;
        this.aggregated = aggregated;
        this.stages = ImmutableList.copyOf(stages);
        this.timestamp = timestamp;
    }

    @Exported
    public List<Stage> getStages() {
        return stages;
    }

    @Exported
    public String getVersion() {
        return version;
    }

    @Exported
    public String getTimestamp() {
        return timestamp;
    }

    @Exported
    public boolean isAggregated() {
        return aggregated;
    }

    @Exported
    public Set<UserInfo> getContributors() {
        return contributors;
    }

    @Exported
    public int getId() {
        return hashCode();
    }

    public void setChanges(List<Change> changes) {
        this.changes = changes;
    }

    @Exported
    public List<Change> getChanges() {
        return changes;
    }



    /**
     * Created a pipeline prototype for the supplied first project
     */
    public static Pipeline extractPipeline(String name, AbstractProject<?, ?> firstProject) {
        Map<String, Stage> stages = newLinkedHashMap();
        for (AbstractProject project : ProjectUtil.getAllDownstreamProjects(firstProject).values()) {
            Task task = Task.getPrototypeTask(project);
            PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
            String stageName = property != null && !isNullOrEmpty(property.getStageName())
                    ? property.getStageName() : project.getDisplayName();
            Stage stage = stages.get(stageName);
            if (stage == null) {
                stage = new Stage(stageName, Collections.<Task>emptyList(), null, null);
            }
            stages.put(stageName,
                    new Stage(stage.getName(), newArrayList(concat(stage.getTasks(), singleton(task))), null, null));
        }
        Collection<Stage> stagesResult = stages.values();

        stagesResult = StageUtil.placeStages(firstProject, stagesResult);

        return new Pipeline(name, null, null, null, null, newArrayList(stagesResult), false);
    }

    public Pipeline createPipelineAggregated(ItemGroup context) {

        AbstractProject firstProject = getProject(getStages().get(0).getTasks().get(0), context);
        List<Stage> stages = new ArrayList<Stage>();
        for (Stage stage : getStages()) {

            List<Task> tasks = new ArrayList<Task>();

            //The version build for this stage is the highest first task build
            AbstractBuild versionBuild = getHighestBuild(stage.getTasks(), firstProject, context);

            String version = null;
            if (versionBuild != null) {
                version = versionBuild.getDisplayName();
            }
            for (Task task : stage.getTasks()) {
                AbstractProject<?, ?> taskProject = getProject(task, context);
                AbstractBuild currentBuild = match(taskProject.getBuilds(), versionBuild);

                if (currentBuild != null) {
                    Status status = SimpleStatus.resolveStatus(taskProject, currentBuild);
                    String link = status.isIdle() ? task.getLink() : Util.fixNull(Jenkins.getInstance().getRootUrl()) + currentBuild.getUrl();
                    tasks.add(new Task(task.getId(), task.getName(), String.valueOf(currentBuild.getNumber()), status, link, task.isManual(), TestResult.getTestResult(currentBuild), task.getDownstreamTasks()));
                } else {
                    tasks.add(new Task(task.getId(), task.getName(), null, StatusFactory.idle(), task.getLink(), task.isManual(), null, task.getDownstreamTasks()));
                }
            }
            stages.add(new Stage(stage.getName(), tasks, stage.getDownstreamStages(), stage.getTaskConnections(), version, stage.getRow(), stage.getColumn()));
        }
        return new Pipeline(getName(), null, null, null, null, stages, true);
    }


    /**
     * Helper method
     */
    public Pipeline createPipelineLatest(ItemGroup context) {
        List<Pipeline> pipelines = createPipelineLatest(1, context);
        return !pipelines.isEmpty() ? pipelines.get(0) : null;
    }


    /**
     * Populates and return pipelines for the supplied pipeline prototype with the current status.
     *
     * @param noOfPipelines number of pipeline instances
     */
    public List<Pipeline> createPipelineLatest(int noOfPipelines, ItemGroup context) {
        Task firstTask = getStages().get(0).getTasks().get(0);
        AbstractProject firstProject = getProject(firstTask, context);

        List<Pipeline> result = new ArrayList<Pipeline>();

        Iterator it = firstProject.getBuilds().iterator();
        for (int i = 0; i < noOfPipelines && it.hasNext(); i++) {
            AbstractBuild firstBuild = (AbstractBuild) it.next();
            List<Change> changes = Change.getChanges(firstBuild);
            String timestamp = PipelineUtils.formatTimestamp(firstBuild.getTimeInMillis());
            List<Stage> stages = new ArrayList<Stage>();
            for (Stage stage : getStages()) {
                List<Task> tasks = new ArrayList<Task>();
                for (Task task : stage.getTasks()) {
                    AbstractProject<?, ?> taskProject = getProject(task, context);
                    AbstractBuild currentBuild = match(taskProject.getBuilds(), firstBuild);
                    tasks.add(Task.getTask(task, currentBuild, context));
                }
                stages.add(new Stage(stage.getName(), tasks, stage.getDownstreamStages(), stage.getTaskConnections(), null, stage.getRow(), stage.getColumn()));
            }
            Pipeline pipelineLatest = new Pipeline(getName(), firstBuild.getDisplayName(), timestamp,
                                Trigger.getTriggeredBy(firstBuild), UserInfo.getContributors(firstBuild), stages, false);
            pipelineLatest.setChanges(changes);
            result.add(pipelineLatest);
        }
        return result;
    }




    private AbstractBuild getHighestBuild(List<Task> tasks, AbstractProject firstProject, ItemGroup context) {
        int highest = -1;
        for (Task task : tasks) {
            AbstractProject project = getProject(task, context);
            AbstractBuild firstBuild = getFirstUpstreamBuild(project, firstProject);
            if (firstBuild != null && firstBuild.getNumber() > highest) {
                highest = firstBuild.getNumber();
            }
        }

        if (highest > 0) {
            return firstProject.getBuildByNumber(highest);
        } else {
            return null;
        }
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



    private AbstractProject getProject(Task task, ItemGroup context) {
        return ProjectUtil.getProject(task.getId(), context);
    }

    private AbstractBuild getFirstUpstreamBuild(AbstractProject<?, ?> project, AbstractProject<?, ?> first) {
        RunList<? extends AbstractBuild> builds = project.getBuilds();
        for (AbstractBuild build : builds) {
            AbstractBuild upstream = BuildUtil.getFirstUpstreamBuild(build, first);
            if (upstream != null && upstream.getProject().equals(first)) {
                return upstream;
            }
        }
        return null;
    }




    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(version).append(stages).toHashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof Pipeline && equalsSelf((Pipeline) o);
    }

    private boolean equalsSelf(Pipeline o) {
        return super.equals(o) && new EqualsBuilder().appendSuper(super.equals(o)).append(stages, o.stages).append(version, o.version).isEquals();
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("version", getVersion())
                .add("stages", getStages())
                .toString();
    }

    @Exported
    public List<Trigger> getTriggeredBy() {
        return triggeredBy;
    }
}
