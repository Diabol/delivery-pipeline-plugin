package se.diabol.pipefitter;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.ViewDescriptor;
import hudson.model.ViewGroup;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import se.diabol.pipefitter.model.Pipeline;

import java.util.List;
import java.util.logging.Logger;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineView extends AbstractPipelineView {
    private static final Logger LOGGER = Logger.getLogger(PipelineView.class.getName());

    private String title;
    private String firstJob;


    @DataBoundConstructor
    public PipelineView(String name, String title, String firstJob, ViewGroup owner) {
        super(name, owner);
        this.title = title;
        this.firstJob = firstJob;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstJob() {
        return firstJob;
    }

    public void setFirstJob(String firstJob) {
        this.firstJob = firstJob;
    }

    public List<Pipeline> getPipelines()
    {
        PipelineFactory pipelineFactory = new PipelineFactory();
        AbstractProject firstJob = Jenkins.getInstance().getItem(this.firstJob, Jenkins.getInstance(), AbstractProject.class);
        return pipelineFactory.createPipelineLatest(pipelineFactory.extractPipeline(getDisplayName(), firstJob), 3);
    }




    @Extension
    public static class DescriptorImpl extends ViewDescriptor {
        public String getDisplayName() {
            return "Pipeline View";
        }

        /**
         * Display Job List Item in the Edit View Page
         *
         * @param context What to resolve relative job names against?
         * @return ListBoxModel
         */
        public ListBoxModel doFillFirstJobItems(@AncestorInPath ItemGroup<?> context) {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            for (final AbstractProject<?, ?> p : Jenkins.getInstance().getAllItems(AbstractProject.class)) {
                options.add(p.getFullDisplayName(), p.getRelativeNameFrom(context));
            }
            return options;
        }

    }
}
