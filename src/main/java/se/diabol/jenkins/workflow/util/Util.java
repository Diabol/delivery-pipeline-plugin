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

import org.jenkinsci.plugins.workflow.graph.FlowNode;
import se.diabol.jenkins.workflow.api.Run;
import se.diabol.jenkins.workflow.step.TaskAction;

import java.util.ArrayList;
import java.util.List;

public final class Util {

    private Util() {
    }

    public static List<FlowNode> getTaskNodes(List<FlowNode> stageNodes) {
        List<FlowNode> result = new ArrayList<FlowNode>();
        for (FlowNode sortedNode : stageNodes) {
            if (isTaskNode(sortedNode)) {
                result.add(sortedNode);
            }
        }
        return result;
    }

    public static Run getRunById(List<Run> runs, int buildNumber) {
        if (runs == null || runs.isEmpty()) {
            return null;
        }
        String id = "" + buildNumber;
        for (Run run : runs) {
            if (id.equals(run.id)) {
                return run;
            }
        }
        return null;
    }

    public static <T> T head(List<T> list) {
        if (list != null && !list.isEmpty()) {
            return list.get(0);
        } else {
            return null;
        }
    }

    private static boolean isTaskNode(FlowNode flowNode) {
        return flowNode.getAction(TaskAction.class) != null;
    }

}
