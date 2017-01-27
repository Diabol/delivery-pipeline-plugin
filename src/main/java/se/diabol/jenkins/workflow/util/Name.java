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

import com.cloudbees.hudson.plugins.folder.Folder;
import hudson.model.AbstractItem;
import jenkins.branch.MultiBranchProject;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

public final class Name {

    public static String of(WorkflowRun build) {
        if (build == null) {
            return null;
        }
        if (parentIsFolder(build)) {
            return ((Folder) build.getParent().getParent()).getName() + "/job/" + build.getParent().getName();
        } else if (parentIsMultiBranch(build)) {
            return ((AbstractItem) ((MultiBranchProject)
                    build.getParent().getParent())).getName() + "/job/" + build.getParent().getName();
        } else {
            return build.getParent().getName();
        }
    }

    private static boolean parentIsMultiBranch(WorkflowRun build) {
        return build.getParent().getParent() instanceof MultiBranchProject;
    }

    private static boolean parentIsFolder(WorkflowRun build) {
        return build.getParent().getParent() instanceof Folder;
    }
}
