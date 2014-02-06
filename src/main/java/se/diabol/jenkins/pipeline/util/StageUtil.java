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
package se.diabol.jenkins.pipeline.util;

import hudson.model.AbstractProject;
import jenkins.model.Jenkins;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.SimpleDirectedGraph;
import se.diabol.jenkins.pipeline.model.Edge;
import se.diabol.jenkins.pipeline.model.Stage;
import se.diabol.jenkins.pipeline.model.Task;

import java.util.*;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.singleton;


public final class StageUtil {

    private StageUtil() {
    }

    public static List<Stage> placeStages(AbstractProject firstProject, Collection<Stage> stages) {
        DirectedGraph<Stage, Edge> graph = new SimpleDirectedGraph<Stage, Edge>(Edge.class);
        for (Stage stage : stages) {
            stage.setTaskConnections(getStageConnections(stage, stages));
            graph.addVertex(stage);
            List<Stage> downstreamStages = getDownstreamStages(stage, stages);
            List<String> downstreamStageNames = new ArrayList<String>();
            for (Stage downstream : downstreamStages) {
                downstreamStageNames.add(downstream.getName());
                graph.addVertex(downstream);
                graph.addEdge(stage, downstream, new Edge(stage, downstream));
            }
            stage.setDownstreamStages(downstreamStageNames);
        }

        List<List<Stage>> allPaths = findAllRunnablePaths(findStageForJob(firstProject.getRelativeNameFrom(Jenkins.getInstance()), stages), graph);
        Collections.sort(allPaths, new Comparator<List<Stage>>() {
            public int compare(List<Stage> stages1, List<Stage> stages2) {
                return stages2.size() - stages1.size();
            }
        });
        for (int row = allPaths.size() - 1; row >= 0; row--) {
            List<Stage> path = allPaths.get(row);
            for (int column = 0; column < path.size(); column++) {
                Stage stage = path.get(column);
                stage.setColumn(Math.max(stage.getColumn(), column));
                stage.setRow(row);
            }
        }

        return new ArrayList<Stage>(stages);
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

    private static Stage findStageForJob(String name, Collection<Stage> stages) {
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

}
