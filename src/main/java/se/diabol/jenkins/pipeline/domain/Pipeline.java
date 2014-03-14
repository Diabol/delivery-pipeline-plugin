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
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.util.RunList;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.util.BuildUtil;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

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
        return new Pipeline(name, null, null, null, null, newArrayList(Stage.extractStages(firstProject)), false);
    }

    public Pipeline createPipelineAggregated(ItemGroup context) {
        AbstractProject firstProject = getProject(getStages().get(0).getTasks().get(0), context);
        List<Stage> pipelineStages = new ArrayList<Stage>();
        for (Stage stage : getStages()) {
            pipelineStages.add(stage.createAggregatedStage(context, firstProject));
        }
        return new Pipeline(getName(), null, null, null, null, pipelineStages, true);
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
            List<Change> pipelineChanges = Change.getChanges(firstBuild);
            String pipeLineTimestamp = PipelineUtils.formatTimestamp(firstBuild.getTimeInMillis());
            List<Stage> pipelineStages = new ArrayList<Stage>();
            for (Stage stage : getStages()) {
                pipelineStages.add(stage.createLatestStage(context, firstBuild));
            }
            Pipeline pipelineLatest = new Pipeline(getName(), firstBuild.getDisplayName(), pipeLineTimestamp,
                                Trigger.getTriggeredBy(firstBuild), UserInfo.getContributors(firstBuild), pipelineStages, false);
            pipelineLatest.setChanges(pipelineChanges);
            result.add(pipelineLatest);
        }
        return result;
    }

    private AbstractProject getProject(Task task, ItemGroup context) {
        return ProjectUtil.getProject(task.getId(), context);
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
