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
    private List<Pipeline> pipelines;
    private final String firstJob;
    private final String firstJobUrl;
    private final boolean firstJobParameterized;
    private final int noOfPipelines;
    private int componentNumber = 0;
    private boolean pagingEnabled = false;
    private int totalNoOfPipelines = 0;

    public Component(String name, String firstJob, String firstJobUrl, boolean firstJobParameterized,
                     int noOfPipelines, boolean pagingEnabled, int componentNumber) {
        super(name);
        this.firstJob = firstJob;
        this.firstJobUrl = firstJobUrl;
        this.firstJobParameterized = firstJobParameterized;
        this.noOfPipelines = noOfPipelines;
        this.pagingEnabled = pagingEnabled;
        this.componentNumber = componentNumber;
    }

    @Exported
    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    public void setPipelines(List<Pipeline> pipelines) {
        this.pipelines = ImmutableList.copyOf(pipelines);
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

    public int getTotalNoOfPipelines() {
        return totalNoOfPipelines;
    }

    public PipelinePagination getPagination() {
        if (pagingEnabled) {
            return new PipelinePagination(this.getCurrentPage(), totalNoOfPipelines, noOfPipelines, "?"
                    + (this.isFullScreenView() == true ? "fullscreen=true&" : "fullscreen=false&")
                    + "component=" + componentNumber + "&page=");
        }
        return null;
    }

    public int getCurrentPage() {
        StaplerRequest req = Stapler.getCurrentRequest();
        int page = req == null ? 1 : req.getParameter("page") == null ? 1 :
                Integer.parseInt(req.getParameter("page").toString());
        page = Math.max(page, 1);
        int component = req == null ? 1 : req.getParameter("component") == null ? 1 :
                Integer.parseInt(req.getParameter("component").toString());
        if (component != componentNumber) {
            page = 1;
        }
        return page;
    }

    public boolean isFullScreenView() {
        StaplerRequest req = Stapler.getCurrentRequest();
        return req == null ? false : req.getParameter("fullscreen") == null ? false :
                Boolean.parseBoolean(req.getParameter("fullscreen"));
    }

    @Exported
    public int getComponentNumber() {
        return componentNumber;
    }

    public void setTotalNoOfPipelines(int totalNoOfPipelines) {
        this.totalNoOfPipelines = totalNoOfPipelines;
    }
}
