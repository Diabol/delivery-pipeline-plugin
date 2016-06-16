/*
This file is part of Delivery Pipeline Plugin.

Delivery Pipeline Plugin is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Delivery Pipeline Plugin is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Delivery Pipeline Plugin.
If not, see <http://www.gnu.org/licenses/>.
*/
package se.diabol.jenkins.pipeline.domain;

import static com.google.common.base.Objects.toStringHelper;

import com.google.common.collect.ImmutableList;

import org.kohsuke.stapler.Stapler;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import se.diabol.jenkins.pipeline.PipelinePagination;

import java.util.List;


@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Component extends AbstractItem {
    private final List<Pipeline> pipelines;
    private final String firstJob;
    private final String firstJobUrl;
    private final boolean firstJobParameterized;
    private final int noOfPipelines;
    private int componentNumber = 0;
    private boolean pagingEnabled = false;

    public Component(String name, String firstJob, String firstJobUrl, boolean firstJobParameterized,
            List<Pipeline> pipelines, int noOfPipelines, boolean pagingEnabled) {
        super(name);
        this.pipelines = ImmutableList.copyOf(pipelines);
        this.firstJob = firstJob;
        this.firstJobUrl = firstJobUrl;
        this.firstJobParameterized = firstJobParameterized;
        this.noOfPipelines = noOfPipelines;
        this.pagingEnabled = pagingEnabled;
    }

    @Exported
    public List<Pipeline> getPipelines() {
        if (pagingEnabled && !isFullScreenView()) {
            int startIndex = ((this.getCurrentPage() - 1) * noOfPipelines);
            int retrieveSize = Math.min(pipelines.size() - ((this.getCurrentPage() - 1) * noOfPipelines),
                    noOfPipelines);
            return pipelines.subList(startIndex, startIndex + retrieveSize);
        }
        return pipelines;
    }

    @Exported
    public String getFirstJob() {
        return firstJob;
    }

    @Exported
    public String getFirstJobUrl() {
        return firstJobUrl;
    }

    @Exported
    public boolean isFirstJobParameterized() {
        return firstJobParameterized;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("name", getName()).add("pipelines", pipelines).toString();
    }

    @Exported
    public String getPagingData() {
        if (getPagination() == null) {
            return "";
        }
        return getPagination().getTag();
    }

    private PipelinePagination getPagination() {
        if (pagingEnabled) {
            return new PipelinePagination(this.getCurrentPage(), pipelines.size(), noOfPipelines, "?"
                    + (this.isFullScreenView() ? "fullscreen=true&" : "fullscreen=false&")
                    + "component=" + componentNumber + "&page=");
        }
        return null;
    }

    private int getCurrentPage() {
        StaplerRequest req = Stapler.getCurrentRequest();    
        if (req == null) {
            return 1;
        }
        int page = req.getParameter("page") == null ? 1 : Integer.parseInt(req.getParameter("page").toString());
        page = Math.max(page, 1);
        int component = req.getParameter("component") == null ? 1 :
                Integer.parseInt(req.getParameter("component").toString());
        if (component != componentNumber) {
            page = 1;
        }
        return page;
    }

    private boolean isFullScreenView() {
        StaplerRequest req = Stapler.getCurrentRequest();
        return req != null && req.getParameter("fullscreen") != null
                && Boolean.parseBoolean(req.getParameter("fullscreen"));
    }

    @Exported
    public int getComponentNumber() {
        return componentNumber;
    }

    public void setComponentNumber(int componentNumber) {
        this.componentNumber = componentNumber;
    }
}
