package com.diabol.pipefitter;

import com.diabol.pipefitter.dashboard.View;
import com.google.inject.Inject;
import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import hudson.util.FormValidation;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static com.beust.jcommander.internal.Lists.newArrayList;
import static java.util.Collections.unmodifiableCollection;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class PipelineView extends hudson.model.View
{
    private View view = new View();
    private Collection<TopLevelItem> items = newArrayList();

    @Inject
    @DataBoundConstructor
    public PipelineView(String name)
    {
        super(name);
    }

    public PipelineView(String name, ViewGroup owner)
    {
        super(name, owner);
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
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException
    {
        // What to do?
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException
    {
        Item item = owner.getPrimaryView().doCreateItem(req, rsp);
        // Register with this or not...?
        return item;
    }

    @Extension
    public static class Descriptor extends ViewDescriptor
    {
        public String getDisplayName()
        {
            return "Pipeline view";
        }


    }
}
