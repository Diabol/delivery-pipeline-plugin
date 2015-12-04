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
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.util.RunList;
import jenkins.model.Jenkins;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.PipelineProperty;
import se.diabol.jenkins.pipeline.domain.task.Task;
import se.diabol.jenkins.pipeline.util.BuildUtil;
import se.diabol.jenkins.pipeline.util.PipelineUtils;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import javax.annotation.CheckForNull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Objects.toStringHelper;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newLinkedHashMap;
import static java.util.Collections.singleton;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Stage extends AbstractItem {
    private List<Task> tasks;

    private String version;
    private int row;
    private int column;
    private Map<String, List<String>> taskConnections;
    private List<String> downstreamStages;
    private List<Long> downstreamStageIds;
    private long id;

    public Stage(String name, List<Task> tasks) {
        super(name);
        this.tasks = ImmutableList.copyOf(tasks);
        this.id = PipelineUtils.getRandom();
    }

    private Stage(Stage stage, List<Task> tasks, String version, long id) {
        this(stage.getName(), tasks, stage.getDownstreamStages(), stage.getDownstreamStageIds(), stage.getTaskConnections(), version,
                stage.getRow(), stage.getColumn(), id);
    }

    private Stage(String name, List<Task> tasks, List<String> downstreamStages, List<Long> downstreamStageIds, Map<String,
            List<String>> taskConnections, String version, int row, int column, long id) {
        super(name);
        this.tasks = tasks;
        this.version = version;
        this.row = row;
        this.column = column;
        this.downstreamStages = downstreamStages;
        this.taskConnections = taskConnections;
        this.downstreamStageIds = downstreamStageIds;
        this.id = id;
    }

    @Exported
    public List<Task> getTasks() {
        return tasks;
    }

    @Exported
    public String getVersion() {
        return version;
    }

    @Exported
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    @Exported
    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    @Exported
    public List<String> getDownstreamStages() {
        return downstreamStages;
    }

    public void setDownstreamStages(List<String> downstreamStages) {
        this.downstreamStages = downstreamStages;
    }

    @Exported
    public Map<String, List<String>> getTaskConnections() {
        return taskConnections;
    }

    @Exported
    public long getId() {
        return id;
    }

    @Exported
    public List<Long> getDownstreamStageIds() {
        return downstreamStageIds;
    }

    public void setDownstreamStageIds(List<Long> downstreamStageIds) {
        this.downstreamStageIds = downstreamStageIds;
    }

    public void setTaskConnections(Map<String, List<String>> taskConnections) {
        this.taskConnections = taskConnections;
    }

    public static Stage getPrototypeStage(String name, List<Task> tasks) {
        return new Stage(name, tasks);
    }

    public static List<Stage> extractStages(AbstractProject firstProject, AbstractProject lastProject) throws PipelineException {
        Map<String, Stage> stages = newLinkedHashMap();
        for (AbstractProject project : ProjectUtil.getAllDownstreamProjects(firstProject, lastProject).values()) {
            Task task = Task.getPrototypeTask(project, project.getFullName().equals(firstProject.getFullName()));
            /* if current project is last we need clean downStreamTasks*/
            if (lastProject != null && project.getFullName().equals(lastProject.getFullName())) {
                task.getDownstreamTasks().clear();
            }

            PipelineProperty property = (PipelineProperty) project.getProperty(PipelineProperty.class);
            if (property == null && project.getParent() instanceof AbstractProject) {
                property = (PipelineProperty) ((AbstractProject) project.getParent()).getProperty(PipelineProperty.class);
            }
            String stageName = property != null && !isNullOrEmpty(property.getStageName())
                    ? property.getStageName() : project.getDisplayName();
            Stage stage = stages.get(stageName);
            if (stage == null) {
                stage = Stage.getPrototypeStage(stageName, Collections.<Task>emptyList());
            }
            stages.put(stageName,
                    Stage.getPrototypeStage(stage.getName(), newArrayList(concat(stage.getTasks(), singleton(task)))));
        }
        Collection<Stage> stagesResult = stages.values();

        return Stage.placeStages(firstProject, stagesResult);
    }


    public Stage createAggregatedStage(ItemGroup context, AbstractProject firstProject) {
        List<Task> stageTasks = new ArrayList<Task>();

        //The version build for this stage is the highest first task build
        AbstractBuild versionBuild = getHighestBuild(getTasks(), firstProject, context);

        String stageVersion = null;
        if (versionBuild != null) {
            stageVersion = versionBuild.getDisplayName();
        }
        for (Task task : getTasks()) {
            stageTasks.add(task.getAggregatedTask(versionBuild, context));
        }
        return new Stage(this, stageTasks, stageVersion, id);
    }


    public Stage createLatestStage(ItemGroup context, AbstractBuild firstBuild) {
        List<Task> stageTasks = new ArrayList<Task>();
        for (Task task : getTasks()) {
            stageTasks.add(task.getLatestTask(context, firstBuild));
        }
        return new Stage(this, stageTasks, null, id);

    }


    public static List<Stage> placeStages(AbstractProject firstProject, Collection<Stage> stages) throws PipelineException {
        DirectedGraph<Stage, Edge> graph = new SimpleDirectedGraph<Stage, Edge>(new StageEdgeFactory());
        for (Stage stage : stages) {
            stage.setTaskConnections(getStageConnections(stage, stages));
            graph.addVertex(stage);
            List<Stage> downstreamStages = getDownstreamStages(stage, stages);
            List<String> downstreamStageNames = new ArrayList<String>();
            List<Long> downstreamStageIds = new ArrayList<Long>();
            for (Stage downstream : downstreamStages) {
                downstreamStageNames.add(downstream.getName());
                downstreamStageIds.add(downstream.getId());
                graph.addVertex(downstream);
                graph.addEdge(stage, downstream, new Edge(stage, downstream));
            }
            stage.setDownstreamStages(downstreamStageNames);
            stage.setDownstreamStageIds(downstreamStageIds);

        }

        CycleDetector<Stage, Edge> cycleDetector = new CycleDetector<Stage, Edge>(graph);
        if (cycleDetector.detectCycles()) {
            Set<Stage> stageSet = cycleDetector.findCycles();
            StringBuilder message = new StringBuilder("Circular dependencies between stages: ");
            for (Stage stage : stageSet) {
                message.append(stage.getName()).append(" ");
            }
            throw new PipelineException(message.toString());
        }


        List<List<Stage>> allPaths = findAllRunnablePaths(findStageForJob(firstProject.getRelativeNameFrom(Jenkins.getInstance()), stages), graph);
        Collections.sort(allPaths, new Comparator<List<Stage>>() {
            public int compare(List<Stage> stages1, List<Stage> stages2) {
                return stages2.size() - stages1.size();
            }
        });
        
        //for keeping track of which row has an available column
        final Map<Integer,Integer> columnRowMap = Maps.newHashMap();
        final List<Stage> processedStages = Lists.newArrayList();
        
        for (int row = 0; row < allPaths.size(); row++) {
            List<Stage> path = allPaths.get(row);            
            for (int column = 0; column < path.size(); column++) {
                Stage stage = path.get(column);
                
                //skip processed stage since the row/column has already been set
                if (!processedStages.contains(stage)) {
	                stage.setColumn(Math.max(stage.getColumn(), column));
	                
	                final int effectiveColumn = stage.getColumn();
	                
	                final Integer previousRowForThisColumn = columnRowMap.get(effectiveColumn);
	                //set it to 0 if no previous setting is set; if found, previous value + 1
	                final int currentRowForThisColumn = previousRowForThisColumn == null ? 0 : previousRowForThisColumn + 1;
	                //update/set row number in the columnRowMap for this effective column
	            	columnRowMap.put(effectiveColumn, currentRowForThisColumn);
	
	            	stage.setRow(currentRowForThisColumn);
	            	
	            	processedStages.add(stage);
                }
            }
        }
        
        List<Stage> result = new ArrayList<Stage>(stages);

        sortByRowsCols(result);

        return result;
    }

    private static Map<String, List<String>> getStageConnections(Stage stage, Collection<Stage> stages) {
        Map<String, List<String>> result = new HashMap<String, List<String>>();
        for (int i = 0; i < stage.getTasks().size(); i++) {
            Task task = stage.getTasks().get(i);
            for (int j = 0; j < task.getDownstreamTasks().size(); j++) {
                String downstreamTask = task.getDownstreamTasks().get(j);
                Stage target = findStageForJob(downstreamTask, stages);
                if (!stage.equals(target)) {
                    if (result.get(task.getId()) == null) {
                        result.put(task.getId(), new ArrayList<String>(singleton(downstreamTask)));
                    } else {
                        result.get(task.getId()).add(downstreamTask);
                    }
                }
            }
        }
        return result;
    }

    private static List<List<Stage>> findAllRunnablePaths(Stage start, DirectedGraph<Stage, Edge> graph) {
        List<List<Stage>> paths = new LinkedList<List<Stage>>();
        if (graph.outDegreeOf(start) == 0) {
            List<Stage> path = new LinkedList<Stage>();
            path.add(start);
            paths.add(path);
        } else {
            for (Edge edge : graph.outgoingEdgesOf(start)) {
                List<List<Stage>> allPathsFromTarget = findAllRunnablePaths(edge.getTarget(), graph);
                for (List<Stage> path : allPathsFromTarget) {
                    path.add(0, start);
                }
                paths.addAll(allPathsFromTarget);
            }
        }
        return paths;
    }

    protected static void sortByRowsCols(List<Stage> stages) {
        Collections.sort(stages, new Comparator<Stage>() {
            @Override
            public int compare(Stage stage1, Stage stage2) {
                int result = Integer.valueOf(stage1.getRow()).compareTo(stage2.getRow());
                if (result == 0) {
                    return Integer.valueOf(stage1.getColumn()).compareTo(stage2.getColumn());
                } else {
                    return result;
                }
            }
        });
    }


    private static List<Stage> getDownstreamStages(Stage stage, Collection<Stage> stages) {
        List<Stage> result = newArrayList();
        for (int i = 0; i < stage.getTasks().size(); i++) {
            Task task = stage.getTasks().get(i);
            for (int j = 0; j < task.getDownstreamTasks().size(); j++) {
                String jobName = task.getDownstreamTasks().get(j);
                Stage target = findStageForJob(jobName, stages);
                if (target != null && !target.getName().equals(stage.getName())) {
                    result.add(target);
                }
            }
        }
        return result;
    }

    @CheckForNull
    protected static Stage findStageForJob(String name, Collection<Stage> stages) {
        for (Stage stage : stages) {
            for (int j = 0; j < stage.getTasks().size(); j++) {
                Task task = stage.getTasks().get(j);
                if (task.getId().equals(name)) {
                    return stage;
                }
            }
        }
        return null;

    }

    @CheckForNull
    private AbstractBuild getHighestBuild(List<Task> tasks, AbstractProject firstProject, ItemGroup context) {
        int highest = -1;
        for (Task task : tasks) {
            AbstractProject project = ProjectUtil.getProject(task.getId(), context);
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

    @CheckForNull
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
    public String toString() {
        return toStringHelper(this)
                .add("name", getName())
                .add("version", getVersion())
                .add("tasks", getTasks())
                .toString();
    }
}
