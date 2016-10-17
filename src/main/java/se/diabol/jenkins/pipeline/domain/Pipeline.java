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

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;

import com.google.common.collect.ImmutableList;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.Result;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.domain.task.Task;
import se.diabol.jenkins.pipeline.util.PipelineUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Pipeline extends AbstractItem {

    private final AbstractProject firstProject;
    private final AbstractProject lastProject;

    private final List<Stage> stages;

    private String version;

    private List<TriggerCause> triggeredBy;
    private Set<UserInfo> contributors;

    private boolean aggregated;

    private String timestamp;

    private List<Change> changes;

    private int commits;

    private long totalBuildTime;

    private Map<String, Task> allTasks = null;

    public Pipeline(String name, AbstractProject firstProject, AbstractProject lastProject, List<Stage> stages) {
        super(name);
        this.firstProject = firstProject;
        this.lastProject = lastProject;
        this.stages = stages;
    }

    public Pipeline(String name,
                    AbstractProject firstProject,
                    AbstractProject lastProject,
                    String version,
                    String timestamp,
                    List<TriggerCause> triggeredBy,
                    Set<UserInfo> contributors,
                    List<Stage> stages,
                    boolean aggregated) {
        super(name);
        this.firstProject = firstProject;
        this.lastProject = lastProject;
        this.version = version;
        this.triggeredBy = triggeredBy;
        this.contributors = contributors;
        this.aggregated = aggregated;
        this.stages = ImmutableList.copyOf(stages);
        this.timestamp = timestamp;
    }

    public AbstractProject getFirstProject() {
        return this.firstProject;
    }

    public AbstractProject getLastProject() {
        return this.lastProject;
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

    public void setCommits(int commits) {
        this.commits = commits;
    }

    @Exported
    public long getTotalBuildTime() {
        return totalBuildTime;
    }

    @Exported
    public int getCommits() {
        return commits;
    }

    public void calculateTotalBuildTime() {
        if (stages.size() == 0) {
            this.totalBuildTime = 0L;
        } else {
            List<Route> allRoutes = new ArrayList<Route>();
            calculatePipelineRoutes(getStages().get(0).getTasks().get(0), null, allRoutes);
            long maxTime = 0L;
            for (Route route : allRoutes) {
                long buildTime = route.getTotalBuildTime();
                if (buildTime > maxTime) {
                    maxTime = buildTime;
                }
            }
            this.totalBuildTime = maxTime;
        }
    }

    private Route createRouteAndCopyTasks(final Route route, Task task) {
        Route currentRoute = new Route();
        if (route != null) {
            currentRoute.setTasks(newArrayList(route.getTasks()));
        }
        currentRoute.addTask(task);
        return currentRoute;
    }

    void calculatePipelineRoutes(Task task, final Route route, List<Route> allRoutes) {
        if (task.getDownstreamTasks() != null && task.getDownstreamTasks().size() > 0) {
            for (String downstreamTaskName: task.getDownstreamTasks()) {
                // assume each task only appears once in the pipeline
                Route currentRoute = createRouteAndCopyTasks(route, task);
                calculatePipelineRoutes(getTaskFromName(downstreamTaskName), currentRoute, allRoutes);
            }
        } else {
            Route currentRoute = createRouteAndCopyTasks(route, task);
            allRoutes.add(currentRoute);
        }
    }

    private Task getTaskFromName(String taskName) {
        if (allTasks == null) {
            allTasks = new HashMap<String, Task>();
            for (Stage stage : stages) {
                for (Task task : stage.getTasks()) {
                    allTasks.put(task.getId(), task);
                }
            }
        }
        return allTasks.get(taskName);
    }

    /**
     * Created a pipeline prototype for the supplied first project.
     */
    public static Pipeline extractPipeline(String name, AbstractProject<?, ?> firstProject,
                                           AbstractProject<?, ?> lastProject) throws PipelineException {
        return new Pipeline(name, firstProject, lastProject,
                newArrayList(Stage.extractStages(firstProject, lastProject)));
    }

    public static Pipeline extractPipeline(String name, AbstractProject<?, ?> firstProject) throws PipelineException {
        return new Pipeline(name, firstProject, null, newArrayList(Stage.extractStages(firstProject, null)));
    }

    Pipeline createPipelineAggregatedWithoutChangesShown(ItemGroup context) {
        return createPipelineAggregated(context, false);
    }

    Pipeline createPipelineAggregatedWithChangesShown(ItemGroup context) {
        return createPipelineAggregated(context, true);
    }

    public Pipeline createPipelineAggregated(ItemGroup context, boolean showAggregatedChanges) {
        List<Stage> pipelineStages = new ArrayList<Stage>();
        for (Stage stage : getStages()) {
            pipelineStages.add(stage.createAggregatedStage(context, firstProject));
        }

        if (showAggregatedChanges) {
            setAggregatedChanges(context, pipelineStages);
        }

        return new Pipeline(getName(), firstProject, lastProject, null, null, null, null, pipelineStages, true);
    }

    void setAggregatedChanges(ItemGroup context, List<Stage> pipelineStages) {
        // We use size() - 1 because last stage's changelog can't be calculated against next stage (no such)
        for (int i = 0; i < pipelineStages.size() - 1; i++) {
            Stage stage = pipelineStages.get(i);
            Stage nextStage = pipelineStages.get(i + 1);

            final AbstractBuild nextBuild = nextStage.getHighestBuild(firstProject, context, Result.SUCCESS);

            Set<Change> changes = newHashSet();

            AbstractBuild build = stage.getHighestBuild(firstProject, context, Result.SUCCESS);
            for (; build != null && build != nextBuild; build = build.getPreviousBuild()) {
                changes.addAll(Change.getChanges(build));
            }

            stage.setChanges(changes);
        }
    }

    /**
     * Populates and return pipelines for the supplied pipeline prototype with the current status.
     *
     * @param noOfPipelines number of pipeline instances
     */
    public List<Pipeline> createPipelineLatest(int noOfPipelines,
                                               ItemGroup context,
                                               boolean pagingEnabled,
                                               boolean showChanges,
                                               Component component) {
        List<Pipeline> result = new ArrayList<Pipeline>();
        int no = noOfPipelines;
        if (firstProject.isInQueue()) {
            String pipeLineTimestamp = PipelineUtils.formatTimestamp(firstProject.getQueueItem().getInQueueSince());
            List<Stage> pipelineStages = new ArrayList<Stage>();
            for (Stage stage : getStages()) {
                pipelineStages.add(stage.createLatestStage(context, null));
            }
            Pipeline pipelineLatest = new Pipeline(getName(), firstProject, lastProject, "#"
                    + firstProject.getNextBuildNumber(), pipeLineTimestamp,
                    TriggerCause.getTriggeredBy(firstProject, null), null, pipelineStages, false);
            result.add(pipelineLatest);
            no--;
        }
        int totalNoOfPipelines = firstProject.getBuilds().size();
        component.setTotalNoOfPipelines(totalNoOfPipelines);
        int startIndex = 0;
        int retrieveSize = noOfPipelines;
        if (pagingEnabled && !component.isFullScreenView()) {
            startIndex = (component.getCurrentPage() - 1) * noOfPipelines;
            retrieveSize = Math.min(totalNoOfPipelines - ((component.getCurrentPage() - 1) * noOfPipelines),
                    noOfPipelines);
        }

        Iterator it = firstProject.getBuilds().listIterator(startIndex);
        for (int i = startIndex; i < (startIndex + retrieveSize) && it.hasNext(); i++) {
            AbstractBuild firstBuild = (AbstractBuild) it.next();
            List<Change> pipelineChanges = Change.getChanges(firstBuild);
            Set<UserInfo> contributors = showChanges ? UserInfo.getContributors(pipelineChanges) : null;

            String pipeLineTimestamp = PipelineUtils.formatTimestamp(firstBuild.getTimeInMillis());
            List<Stage> pipelineStages = new ArrayList<Stage>();
            for (Stage stage : getStages()) {
                pipelineStages.add(stage.createLatestStage(context, firstBuild));
            }
            Pipeline pipelineLatest = new Pipeline(getName(), firstProject, lastProject, firstBuild.getDisplayName(),
                    pipeLineTimestamp, TriggerCause.getTriggeredBy(firstProject, firstBuild),
                    contributors, pipelineStages, false);
            if (showChanges) {
                pipelineLatest.setChanges(pipelineChanges);
            }
            pipelineLatest.setCommits(pipelineChanges.size());
            pipelineLatest.calculateTotalBuildTime();
            result.add(pipelineLatest);
        }
        return result;
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
    public List<TriggerCause> getTriggeredBy() {
        return triggeredBy;
    }
}
