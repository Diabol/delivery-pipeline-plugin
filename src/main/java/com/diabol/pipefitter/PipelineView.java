package com.diabol.pipefitter;

import com.google.inject.Inject;
import hudson.Extension;
import hudson.model.*;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static hudson.model.Descriptor.FormException;
import static java.util.Collections.unmodifiableCollection;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineView extends View
{
    private static final Logger LOGGER = Logger.getLogger(PipelineView.class.getName());
    private int refreshFrequency = 3;
    private PipelineBuilder pipelineBuilder;
    private String viewTitle;
    private String numberOfBuilds;
    private Collection<TopLevelItem> items = newArrayList();

    @Inject
    @DataBoundConstructor
    public PipelineView(final String name, final String viewTitle,
                        final PipelineBuilder pipelineBuilder,
                        final String numberOfBuilds, final int refreshFrequency)
    {
        super(name, Hudson.getInstance());
        this.viewTitle = viewTitle;
        this.pipelineBuilder = pipelineBuilder;
        this.numberOfBuilds = numberOfBuilds;
        this.refreshFrequency = refreshFrequency;
    }

    @Override
    public Collection<TopLevelItem> getItems()
    {
        return unmodifiableCollection(newArrayList(items));
    }

    @Override
    public boolean contains(TopLevelItem item)
    {
        return false;
    }

    @Override
    public void onJobRenamed(Item item, String oldName, String newName)
    {
        // Replace in model
    }

    @Override
    protected void submit(StaplerRequest req) throws IOException, ServletException, FormException
    {
        // What to do?
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        Item item = owner.getPrimaryView().doCreateItem(req, rsp);
        System.out.println(item.toString());
        // Register with this or not...?
        return item;
    }

    public String getViewTitle()
    {
        return viewTitle;
    }

    public void setViewTitle(final String viewTitle)
    {
        this.viewTitle = viewTitle;
    }

    public String getNumberOfBuilds()
    {
        return numberOfBuilds;
    }

    public void setNumberOfBuilds(final String numberOfBuilds)
    {
        this.numberOfBuilds = numberOfBuilds;
    }

    public int getRefreshFrequency()
    {
        return refreshFrequency;
    }

    public void setRefreshFrequency(final int refreshFrequency)
    {
        this.refreshFrequency = refreshFrequency;
    }

    public int getRefreshFrequencyInMillis()
    {
        return refreshFrequency * 1000;
    }

    public PipelineBuilder getPipelineBuilder()
    {
        return pipelineBuilder;
    }

    public void setPipelineBuilder(PipelineBuilder pipelineBuilder)
    {
        this.pipelineBuilder = pipelineBuilder;
    }

    @Extension
    public static class DescriptorImpl extends ViewDescriptor
    {
        public DescriptorImpl()
        {
            super();
        }

        public String getDisplayName()
        {
            return "Pipeline View";
        }

        public ListBoxModel doFillNumberOfBuildsItems()
        {
            final hudson.util.ListBoxModel options = new hudson.util.ListBoxModel();
            final List<String> noOfBuilds = new ArrayList<String>();
            noOfBuilds.add("1"); //$NON-NLS-1$
            noOfBuilds.add("2"); //$NON-NLS-1$
            noOfBuilds.add("3"); //$NON-NLS-1$
            noOfBuilds.add("5"); //$NON-NLS-1$
            noOfBuilds.add("10"); //$NON-NLS-1$
            noOfBuilds.add("20"); //$NON-NLS-1$
            noOfBuilds.add("50"); //$NON-NLS-1$
            noOfBuilds.add("100"); //$NON-NLS-1$
            noOfBuilds.add("200"); //$NON-NLS-1$
            noOfBuilds.add("500"); //$NON-NLS-1$

            for (final String noOfBuild : noOfBuilds)
            {
                options.add(noOfBuild);
            }
            return options;
        }
    }
}
