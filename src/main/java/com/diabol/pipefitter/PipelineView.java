package com.diabol.pipefitter;

import com.diabol.pipefitter.model.Pipeline;
import com.diabol.pipefitter.model.Stage;
import com.diabol.pipefitter.model.Status;
import com.diabol.pipefitter.model.Task;
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

import static com.beust.jcommander.internal.Lists.newArrayList;
import static hudson.model.Descriptor.FormException;
import static java.util.Collections.singletonList;
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

    public Pipeline getPipeline() {
        AbstractProject first = Jenkins.getInstance().getItem(firstJob, Jenkins.getInstance(), AbstractProject.class);
        AbstractBuild prevBuild = null;

        List<Stage> stages = newArrayList();
        boolean isFirst = true;
        for (AbstractProject job : getAllDownstreamJobs(first)) {
            AbstractBuild build = job.getLastBuild();
            Task task;
            if (isFirst || build.equals(getDownstreamBuild(job, prevBuild))) {
                Status status = resolveStatus(build);
                if (status == Status.RUNNING) {
                    task = new Task(job.getDisplayName(), status, (int) Math.round((double) (System.currentTimeMillis() - build.getTimestamp().getTimeInMillis()) / build.getEstimatedDuration() * 100.0));
                } else {
                    task = new Task(job.getDisplayName(), status, 100);
                }
                prevBuild = build;
            } else {
                task = new Task(job.getDisplayName(), Status.NOTRUNNED, 0);
                prevBuild = null;
            }



            Stage stage = new Stage(job.getDisplayName(), singletonList(task));
            stages.add(stage);
            isFirst = false;
        }

        return new Pipeline(title, stages);
    }

    private List<AbstractProject> getAllDownstreamJobs(AbstractProject first) {
        List<AbstractProject> jobs = newArrayList();
        jobs.add(first);

        List<AbstractProject> downstreamProjects = first.getDownstreamProjects();
        for (AbstractProject project : downstreamProjects) {
            jobs.addAll(getAllDownstreamJobs(project));
        }

        return jobs;
    }


    private Status resolveStatus(AbstractBuild build) {
        Status status = Status.UNKNOWN;
        if (build != null) {
            if (build.isBuilding()) {
                status = Status.RUNNING;
            } else {
                if (build.getResult().equals(Result.ABORTED)) {
                    status = Status.CANCELLED;
                }

                if (build.getResult().equals(Result.SUCCESS)) {
                    status = Status.SUCCESS;
                }

                if (build.getResult().equals(Result.FAILURE)) {
                    status = Status.FAILED;
                }

                if (build.getResult().equals(Result.UNSTABLE)) {
                    status = Status.UNSTABLE;
                }
            }

        }
        return status;
    }

    public static AbstractBuild<?, ?> getDownstreamBuild(final AbstractProject<?, ?> downstreamProject,
                                                         final AbstractBuild<?, ?> upstreamBuild) {
        if ((downstreamProject != null) && (upstreamBuild != null)) {
            final List<AbstractBuild<?, ?>> downstreamBuilds = (List<AbstractBuild<?, ?>>) downstreamProject.getBuilds();
            for (final AbstractBuild<?, ?> innerBuild : downstreamBuilds) {
                for (final CauseAction action : innerBuild.getActions(CauseAction.class)) {
                    for (final Cause cause : action.getCauses()) {
                        if (cause instanceof Cause.UpstreamCause) {
                            final Cause.UpstreamCause upstreamCause = (Cause.UpstreamCause) cause;
                            if (upstreamCause.getUpstreamProject().equals(upstreamBuild.getProject().getFullName())
                                    && (upstreamCause.getUpstreamBuild() == upstreamBuild.getNumber())) {
                                return innerBuild;
                            }
                        }
                    }
                }
            }
        }
        return null;
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
