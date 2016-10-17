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

import hudson.model.Api;
import hudson.model.ViewGroup;
import hudson.security.Permission;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.DeliveryPipelineView;
import se.diabol.jenkins.pipeline.PipelineApi;

import java.util.Random;

public class ReadOnlyDeliveryPipelineView extends DeliveryPipelineView {

    private static final int BOUND = 32000;
    private static Random GENERATOR = new Random();
    private String id = null;
    private String viewUrl = null;
    private boolean isPortletView = false;

    public ReadOnlyDeliveryPipelineView(String name) {
        super(name);
        this.id = Integer.toString(GENERATOR.nextInt(BOUND));
    }

    public ReadOnlyDeliveryPipelineView(String name, ViewGroup itemGroup) {
        super(name, itemGroup);
        this.id = Integer.toString(GENERATOR.nextInt(BOUND));
    }

    public String getId() {
        return id == null ? setIdValue() : id;
    }

    String setIdValue() {
        this.id = Integer.toString(GENERATOR.nextInt(BOUND));
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean getIsPortletView() {
        return isPortletView;
    }

    public void setIsPortletView(boolean isPortletView) {
        this.isPortletView = isPortletView;
    }

    @Override
    @Exported
    public String getViewUrl() {
        return (this.viewUrl == null ? super.getViewUrl() : viewUrl);
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    @Override
    public boolean hasPermission(final Permission permission) {
        return false;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public Api getApi() {
        return new PipelineApi(this);
    }
}
