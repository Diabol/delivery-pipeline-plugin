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
import hudson.model.AbstractProject;
import hudson.model.DependencyGraph;
import hudson.plugins.parameterizedtrigger.BlockableBuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.BuildTrigger;
import hudson.plugins.parameterizedtrigger.BuildTriggerConfig;
import hudson.plugins.parameterizedtrigger.TriggerBuilder;
import hudson.plugins.promoted_builds.JobPropertyImpl;
import hudson.plugins.promoted_builds.PromotionProcess;
import hudson.tasks.BuildStep;
import jenkins.model.DependencyDeclarer;
import se.diabol.jenkins.pipeline.RelationshipResolver;

import java.util.ArrayList;
import java.util.List;

@Extension(optional = true, ordinal = 150)
public class PromotedBuildRelationshipResolver extends RelationshipResolver {

    // Force a classloading error plugin isn't available
    @SuppressWarnings("UnusedDeclaration")
    public static final Class CLASS = PromotionProcess.class;

    @Override
    public List<AbstractProject> getDownstreamProjects(AbstractProject<?, ?> project) {
        DependencyGraph graph = new DependencyGraph();
        List<AbstractProject> result = new ArrayList<AbstractProject>();
        JobPropertyImpl property = project.getProperty(JobPropertyImpl.class);
        if (property != null) {
            List<PromotionProcess> promotionProcesses = property.getActiveItems();
            for (PromotionProcess promotionProcess : promotionProcesses) {

                List<BuildStep> buildSteps = promotionProcess.getBuildSteps();

                for (BuildStep buildStep : buildSteps) {
                    if (buildStep instanceof DependencyDeclarer) {
                        ((DependencyDeclarer) buildStep).buildDependencyGraph(promotionProcess, graph);
                    }
                }
                result.addAll(graph.getDownstream(promotionProcess));
                for (BuildStep buildStep : buildSteps) {
                    if (buildStep instanceof BuildTrigger) {
                        BuildTrigger buildTrigger = (BuildTrigger) buildStep;
                        List<BuildTriggerConfig> configs = buildTrigger.getConfigs();
                        for (BuildTriggerConfig config : configs) {
                            result.addAll(config.getProjectList(project.getParent(), null));
                        }
                    }

                    if (buildStep instanceof TriggerBuilder) {
                        TriggerBuilder triggerBuilder = (TriggerBuilder) buildStep;
                        List<BlockableBuildTriggerConfig> configs = triggerBuilder.getConfigs();
                        for (BlockableBuildTriggerConfig config : configs) {
                            result.addAll(config.getProjectList(project.getParent(), null));

                        }


                    }

                }
            }

        }
        return result;
    }
}
