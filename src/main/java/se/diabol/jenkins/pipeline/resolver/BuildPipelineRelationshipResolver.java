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
