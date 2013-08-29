package se.diabol.jenkins.pipeline;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import hudson.model.*;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import static java.util.Collections.emptySet;

public abstract class AbstractPipelineView extends View {

    // Prevent xstream from trying to read items property (which it gets from inherited getItems())
    @XStreamOmitField
    private List<TopLevelItem> items;

    public String getRootUrl() {
        return Jenkins.getInstance().getRootUrl();
    }


    protected AbstractPipelineView(String name) {
        super(name);
    }

    protected AbstractPipelineView(String name, ViewGroup owner) {
        super(name, owner);
    }

    @Override
    public Collection<TopLevelItem> getItems() {
        return emptySet(); // Not using the getItems functionality.
    }

    @Override
    public boolean contains(TopLevelItem item) {
        return false;
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
