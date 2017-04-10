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

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import se.diabol.jenkins.pipeline.util.ProjectUtil;

import java.util.List;

public class DownstreamPipeline extends Pipeline {

    public DownstreamPipeline(String name,
                              AbstractProject firstProject,
                              AbstractProject lastProject,
                              List<Stage> stages) {
        super(name, firstProject, lastProject, stages);
    }

    @Override
    public List<Pipeline> createPipelineLatest(int noOfPipelines,
                                               ItemGroup context,
                                               boolean pagingEnabled,
                                               boolean showChanges,
                                               Component component) throws PipelineException {
        List<AbstractProject> firstProjects = ProjectUtil.getStartUpstreams(getFirstProject());
        List<AbstractBuild> builds = resolveBuilds(firstProjects);

        //TODO check if in queue

        int totalNoOfPipelines = builds.size();
        component.setTotalNoOfPipelines(totalNoOfPipelines);
        int startIndex = getStartIndex(component, pagingEnabled, noOfPipelines);
        int retrieveSize = calculateRetreiveSize(component, pagingEnabled, noOfPipelines, totalNoOfPipelines);

        return getPipelines(builds.listIterator(startIndex), context, startIndex, retrieveSize, showChanges);
    }

    protected static int getStartIndex(Component component, boolean pagingEnabled, int noOfPipelines) {
        int startIndex = 0;
        if (pagingEnabled && !component.isFullScreenView()) {
            startIndex = (component.getCurrentPage() - 1) * noOfPipelines;
        }
        return startIndex;
    }

    protected static int calculateRetreiveSize(Component component,
                                               boolean pagingEnabled,
                                               int noOfPipelines,
                                               int totalNoOfPipelines) {
        int retrieveSize = noOfPipelines;
        if (pagingEnabled && !component.isFullScreenView()) {
            retrieveSize = Math.min(totalNoOfPipelines - ((component.getCurrentPage() - 1) * noOfPipelines),
                    noOfPipelines);
        }
        return retrieveSize;
    }

    @Override
    public boolean showUpstream() {
        return true;
    }
}
