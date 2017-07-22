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
package se.diabol.jenkins.workflow.api;

import static se.diabol.jenkins.workflow.util.Util.head;

import org.jenkinsci.plugins.workflow.actions.NotExecutedNodeAction;
import org.jenkinsci.plugins.workflow.actions.TimingAction;
import org.jenkinsci.plugins.workflow.graph.FlowNode;

import java.util.List;

public class Nodes {

    private Nodes() {
    }

    public static long getStartTime(List<FlowNode> nodes) {
        if (nodes != null && !nodes.isEmpty()) {
            return TimingAction.getStartTime(nodes.get(0));
        }
        return 0;
    }

    public static boolean areRunning(List<FlowNode> nodes) {
        if (nodes != null) {
            for (FlowNode node : nodes) {
                if (node.isRunning()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean areAllExecuted(List<FlowNode> nodes) {
        for (FlowNode node : nodes) {
            if (!NotExecutedNodeAction.isExecuted(node)) {
                return false;
            }
        }
        return true;
    }

    public static boolean firstFailed(List<FlowNode> nodes) {
        FlowNode node = head(nodes);
        return node != null && node.getError() != null;
    }
}
