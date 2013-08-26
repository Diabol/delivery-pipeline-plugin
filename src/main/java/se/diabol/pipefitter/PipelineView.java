package se.diabol.pipefitter;

import se.diabol.pipefitter.model.Pipeline;
import se.diabol.pipefitter.model.status.Status;
import se.diabol.pipefitter.model.status.StatusFactory;
import hudson.Extension;
import hudson.model.*;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static com.google.common.collect.Lists.newArrayList;
import static hudson.model.Descriptor.FormException;
import static hudson.model.Result.*;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.unmodifiableCollection;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineView extends View {
    private static final Logger LOGGER = Logger.getLogger(PipelineView.class.getName());

    private String title;
    private Collection<TopLevelItem> items = newArrayList();
    private String firstJob;


    @DataBoundConstructor
    public PipelineView(String name, String title, String firstJob, ViewGroup owner) {
        super(name, owner);
        this.title = title;
        this.firstJob = firstJob;
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return unmodifiableCollection(newArrayList(items));
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return false;
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName) {
        // Replace in model
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, FormException {
        req.bindJSON(this, req.getSubmittedForm());
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return getOwner().getPrimaryView().doCreateItem(req, rsp);
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

    public Pipeline getPipeline()
    {
        PipelineFactory pipelineFactory = new PipelineFactory();
        AbstractProject firstJob = Jenkins.getInstance().getItem(this.firstJob, Jenkins.getInstance(), AbstractProject.class);
        return pipelineFactory.createPipelineLatest(pipelineFactory.extractPipeline(getDisplayName(), firstJob));

//        AbstractBuild prevBuild = null;
//        List<Stage> stages = newArrayList();
//        for (AbstractProject job : PipelineFactory.getAllDownstreamJobs(first)) {
//            PipelineProperty property = (PipelineProperty) job.getProperty(PipelineProperty.class);
//            AbstractBuild build = job.getLastBuild();
//            Task task;
//            if (stages.isEmpty() || build != null && build.equals(getDownstreamBuild(job, prevBuild))) {
//                Status status = build != null? resolveStatus(build): idle();
//                task = new Task(job.getDisplayName(), status);
//                prevBuild = build;
//            } else {
//                task = new Task(job.getDisplayName(), idle());
//                prevBuild = null;
//            }
//
//            Stage stage = new Stage(job.getDisplayName(), singletonList(task));
//            stages.add(stage);
//        }
//
//        return new Pipeline(title, stages);
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
