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
package se.diabol.jenkins.pipeline.trigger;

import au.com.centrumsystems.hudson.plugin.buildpipeline.BuildPipelineView;
import au.com.centrumsystems.hudson.plugin.buildpipeline.DownstreamProjectGridBuilder;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;

public class BPPManualTrigger implements ManualTrigger {

    @Override
    public void triggerManual(AbstractProject<?, ?> project, AbstractProject<?, ?> upstream, String buildId,
                              ItemGroup<? extends TopLevelItem> itemGroup) throws TriggerException {
        MyView view = new MyView(itemGroup);
        if (upstream != null && upstream.getBuild(buildId) != null) {

            try {
                view.triggerManualBuild(Integer.parseInt(buildId), project.getRelativeNameFrom(itemGroup), upstream.getRelativeNameFrom(itemGroup));
            } catch (Exception e) {
                throw new TriggerException("Could not trigger", e);
            }
        } else {
            throw new TriggerException("Could not find build: " + buildId + " for project: " + upstream);
        }

    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("SIC_INNER_SHOULD_BE_STATIC")
    public class MyView extends BuildPipelineView {

        ItemGroup<? extends TopLevelItem> context;

        public MyView(ItemGroup<? extends TopLevelItem> context) {
            super("", "", new DownstreamProjectGridBuilder(""), "1", false, "");
            this.context = context;
        }

        @Override
        public ItemGroup<? extends TopLevelItem> getOwnerItemGroup() {
            return context;
        }
    }

}
