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
package se.diabol.jenkins.workflow.util;

import com.cloudbees.workflow.flownode.FlowNodeUtil;
import com.cloudbees.workflow.rest.external.StageNodeExt;
import java.util.ArrayList;
import java.util.List;
import org.jenkinsci.plugins.workflow.graph.FlowNode;
import se.diabol.jenkins.workflow.step.TaskAction;

public class Util {


    public static List<FlowNode> getTaskNodes(List<FlowNode> stageNodes) {
        List<FlowNode> result = new ArrayList<FlowNode>();
        for (int i = 0; i < stageNodes.size(); i++) {
            FlowNode sortedNode = stageNodes.get(i);
            if (isTaskNode(sortedNode)) {
                result.add(sortedNode);
            }
        }
        return result;
    }

    public static boolean isTaskNode(FlowNode flowNode) {
        return flowNode.getAction(TaskAction.class) != null;
    }

    public static List<FlowNode> getTaskNodes(List<FlowNode> stageNodes, FlowNode node) {
        List<FlowNode> nodes = new ArrayList<FlowNode>();

        int taskStartNodeIndex =  stageNodes.indexOf(node);

        if (isTaskNode(node)) {
            // Starting at the node after the supplied node, add all sorted nodes up to the
            // next stage (or the end of the workflow)...
            taskStartNodeIndex++;
            for (int i = taskStartNodeIndex; i < stageNodes.size(); i++) {
                FlowNode sortedNode = stageNodes.get(i);
                if (isTaskNode(sortedNode)) {
                    break;
                }
                nodes.add(sortedNode);
            }
        }

        return nodes;
    }





}
