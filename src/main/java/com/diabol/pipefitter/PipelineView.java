package com.diabol.pipefitter;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import hudson.Extension;
import hudson.model.*;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
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

    private int refreshFrequency;
    private String title;
    private String numberOfBuilds;
    private Collection<TopLevelItem> items = newArrayList();

    @DataBoundConstructor
    public PipelineView(String name, String title, ViewGroup owner)
    {
        super(name, owner);
        this.title = title;
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
        req.bindJSON(this, req.getSubmittedForm());
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        return getOwner().getPrimaryView().doCreateItem(req, rsp);
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Extension
    public static class DescriptorImpl extends ViewDescriptor
    {
        public String getDisplayName()
        {
            return "Pipeline View";
        }
    }
}
