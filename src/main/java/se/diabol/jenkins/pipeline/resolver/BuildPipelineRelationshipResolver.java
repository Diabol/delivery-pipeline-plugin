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

import au.com.centrumsystems.hudson.plugin.buildpipeline.trigger.BuildPipelineTrigger;
import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Items;
import se.diabol.jenkins.pipeline.RelationshipResolver;

import java.util.ArrayList;
import java.util.List;

@Extension(ordinal = 201)
public class BuildPipelineRelationshipResolver extends RelationshipResolver {

    @Override
    public List<AbstractProject> getDownstreamProjects(AbstractProject<?, ?> project) {

        List<AbstractProject> result = new ArrayList<>();

        for (BuildPipelineTrigger buildPipelineTrigger: Util.filter(project.getPublishersList(), BuildPipelineTrigger.class)) {
            String downstreamProjectNames = buildPipelineTrigger.getDownstreamProjectNames();

            for (final Object o : Items.fromNameList(project.getParent(), downstreamProjectNames, AbstractProject.class)) {
                final AbstractProject downstream = (AbstractProject) o;

                if (project != downstream) {
                    result.add(downstream);
                }
            }
        }

        return result;
    }
}
