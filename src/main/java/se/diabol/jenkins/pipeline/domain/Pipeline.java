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
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.util.PipelineUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.collect.Lists.newArrayList;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Pipeline extends AbstractItem {

    private AbstractProject firstProject;

    private List<Stage> stages;

    private String version;

    private List<Trigger> triggeredBy;
    private Set<UserInfo> contributors;

    private boolean aggregated;

    private String timestamp;

    private List<Change> changes;


    public Pipeline(String name, AbstractProject firstProject, List<Stage> stages) {
        super(name);
        this.firstProject = firstProject;
        this.stages = stages;
    }

    public Pipeline(String name,
                    AbstractProject firstProject,
                    String version,
                    String timestamp,
                    List<Trigger> triggeredBy,
                    Set<UserInfo> contributors,
                    List<Stage> stages, boolean aggregated) {
        super(name);
        this.firstProject = firstProject;
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
    public static Pipeline extractPipeline(String name, AbstractProject<?, ?> firstProject) throws PipelineException {
        return new Pipeline(name, firstProject, newArrayList(Stage.extractStages(firstProject)));
    }

    public Pipeline createPipelineAggregated(ItemGroup context) {
        List<Stage> pipelineStages = new ArrayList<Stage>();
        for (Stage stage : getStages()) {
            pipelineStages.add(stage.createAggregatedStage(context, firstProject));
        }
        return new Pipeline(getName(), firstProject, null, null, null, null, pipelineStages, true);
    }

    /**
     * Populates and return pipelines for the supplied pipeline prototype with the current status.
     *
     * @param noOfPipelines number of pipeline instances
     */
    public List<Pipeline> createPipelineLatest(int noOfPipelines, ItemGroup context) {
        List<Pipeline> result = new ArrayList<Pipeline>();
        int no = noOfPipelines;
        if (firstProject.isInQueue()) {
            String pipeLineTimestamp = PipelineUtils.formatTimestamp(firstProject.getQueueItem().getInQueueSince());
            List<Stage> pipelineStages = new ArrayList<Stage>();
            for (Stage stage : getStages()) {
                pipelineStages.add(stage.createLatestStage(context, null));
            }
            Pipeline pipelineLatest = new Pipeline(getName(), firstProject, "#" + firstProject.getNextBuildNumber(), pipeLineTimestamp,
                    Trigger.getTriggeredBy(firstProject, null), null,
                    //            Trigger.getTriggeredBy(firstBuild),
                    //            UserInfo.getContributors(firstBuild),
                    pipelineStages, false);
            //pipelineLatest.setChanges(pipelineChanges);
            result.add(pipelineLatest);
            no--;
        }


        Iterator it = firstProject.getBuilds().iterator();
        for (int i = 0; i < no && it.hasNext(); i++) {
            AbstractBuild firstBuild = (AbstractBuild) it.next();
            List<Change> pipelineChanges = Change.getChanges(firstBuild);
            String pipeLineTimestamp = PipelineUtils.formatTimestamp(firstBuild.getTimeInMillis());
            List<Stage> pipelineStages = new ArrayList<Stage>();
            for (Stage stage : getStages()) {
                pipelineStages.add(stage.createLatestStage(context, firstBuild));
            }
            Pipeline pipelineLatest = new Pipeline(getName(), firstProject, firstBuild.getDisplayName(), pipeLineTimestamp,
                                Trigger.getTriggeredBy(firstProject, firstBuild), UserInfo.getContributors(firstBuild), pipelineStages, false);
            pipelineLatest.setChanges(pipelineChanges);
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
    public List<Trigger> getTriggeredBy() {
        return triggeredBy;
    }
}
