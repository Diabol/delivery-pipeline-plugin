package se.diabol.pipefitter;

import hudson.model.*;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;

import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableCollection;

public abstract class AbstractPipelineView extends View {

    protected transient Collection<TopLevelItem> items = newArrayList();

    protected AbstractPipelineView(String name) {
        super(name);
    }

    protected AbstractPipelineView(String name, ViewGroup owner) {
        super(name, owner);
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
    protected void submit(StaplerRequest req) throws IOException, ServletException, Descriptor.FormException {
        req.bindJSON(this, req.getSubmittedForm());
    }

    @Override
    public Item doCreateItem(StaplerRequest req, StaplerResponse rsp) throws IOException, ServletException {
        return getOwner().getPrimaryView().doCreateItem(req, rsp);
    }


}
