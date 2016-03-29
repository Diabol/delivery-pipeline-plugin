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
package se.diabol.jenkins.pipeline.resolver;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.SubProjectsAction;
import se.diabol.jenkins.pipeline.RelationshipResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * It looks for Parameterized Trigger Plugin builders for the current project.
 * They are for some reason not registered as a dependency in Jenkins.
 */
@Extension(ordinal = 200)
public class SubProjectRelationshipResolver extends RelationshipResolver {

    @Override
    public List<AbstractProject> getDownstreamProjects(AbstractProject<?, ?>  project) {
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        for (SubProjectsAction action : Util.filter(project.getActions(), SubProjectsAction.class)) {
            for (BlockableBuildTriggerConfig config : action.getConfigs()) {
                for (AbstractProject subProject : config.getProjectList(project.getParent(), null)) {
                    result.add(subProject);
                }
            }
        }
        return result;
    }


}
