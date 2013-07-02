package com.diabol.pipefitter;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.model.ItemGroup;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * Builds a pipeline by looking at the inter-job dependencies
 */
public class PipelineBuilder extends AbstractDescribableImpl<PipelineBuilder>
{
    private String firstJob;

    @DataBoundConstructor
    public PipelineBuilder(final String firstJob)
    {
        this.firstJob = firstJob;
    }
    /**
     * Descriptor.
     */
    @Extension(ordinal = 1000) // historical default behavior, so give it a higher priority
    public static class DescriptorImpl extends Descriptor<PipelineBuilder>
    {
        @Override
        public String getDisplayName()
        {
            return "Based on upstream/downstream relationship";
        }

        /**
         * Display Job List Item in the Edit View Page
         *
         * @param context
         *      What to resolve relative job names against?
         * @return ListBoxModel
         */
        // TODO: this does not handle relative path in the current context correctly
        public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context)
        {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            for (final String jobName : Hudson.getInstance().getJobNames())
            {
                options.add(jobName);
            }
            return options;
        }
    }

    public String getFirstJob() {
        return firstJob;
    }

}
