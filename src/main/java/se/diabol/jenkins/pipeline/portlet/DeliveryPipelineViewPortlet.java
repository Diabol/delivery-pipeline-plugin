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
package se.diabol.jenkins.pipeline.portlet;

import hudson.Extension;
import hudson.model.Api;
import hudson.model.Descriptor;
import hudson.plugins.view.dashboard.DashboardPortlet;
import org.kohsuke.stapler.DataBoundConstructor;
import se.diabol.jenkins.pipeline.DeliveryPipelineView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DeliveryPipelineViewPortlet extends DashboardPortlet {

    private static final int BOUND = 32000;
    private static final Random GENERATOR = new Random();
    private ReadOnlyDeliveryPipelineView deliveryPipelineView;

    private final String initialJob;
    private final String finalJob;
    private String portletId = null;

    @DataBoundConstructor
    public DeliveryPipelineViewPortlet(String name, String initialJob, String finalJob) throws IOException {
        super(name);
        this.initialJob = initialJob;
        this.finalJob = finalJob;
        this.portletId = (portletId != null && !"".equals(portletId.trim())) ? portletId :
                Integer.toString(GENERATOR.nextInt(BOUND));
    }

    public String getPortletId() {
        return portletId;
    }

    public ReadOnlyDeliveryPipelineView getDeliveryPipelineView() {
        return setPipelineView();
    }

    ReadOnlyDeliveryPipelineView setPipelineView() {
        deliveryPipelineView = new ReadOnlyDeliveryPipelineView(this.portletId, this.getDashboard().getOwner());
        updateView(deliveryPipelineView);
        return deliveryPipelineView;
    }

    public Api getApi() {
        return deliveryPipelineView.getApi();
    }

    public String getInitialJob() {
        return initialJob;
    }

    public String getFinalJob() {
        return finalJob;
    }

    void updateView(ReadOnlyDeliveryPipelineView view) {
        view.setViewUrl(this.getDashboard().getViewUrl() + "/" + this.getUrl());
        view.setIsPortletView(true);
        view.setShowChanges(false);
        view.setShowAggregatedPipeline(true);
        view.setShowTestResults(true);
        view.setShowAggregatedChanges(true);
        view.setShowPromotions(true);
        view.setShowStaticAnalysisResults(true);
        view.setNoOfPipelines(0);
        List<DeliveryPipelineView.ComponentSpec> componentSpecs = new ArrayList<DeliveryPipelineView.ComponentSpec>();
        if (initialJob != null && !"".equals(initialJob.trim())) {
            DeliveryPipelineView.ComponentSpec componentSpec = new DeliveryPipelineView.ComponentSpec("Aggregated view",
                    initialJob, finalJob);
            componentSpecs.add(componentSpec);
        }
        view.setComponentSpecs(componentSpecs);
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<DashboardPortlet> {
        @Override
        public String getDisplayName() {
            return "Delivery Pipeline Portlet";
        }
    }
}
