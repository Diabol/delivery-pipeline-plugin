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

import au.com.centrumsystems.hudson.plugin.buildpipeline.extension.StandardBuildCard;
import hudson.model.AbstractProject;
import hudson.model.ItemGroup;
import hudson.model.TopLevelItem;

public class BPPManualTrigger implements ManualTrigger {

    @Override
    public void triggerManual(AbstractProject<?, ?> project, AbstractProject<?, ?> upstream, String buildId,
                              ItemGroup<? extends TopLevelItem> itemGroup) throws TriggerException {
        StandardBuildCard buildCard = new StandardBuildCard();

        if (upstream != null && upstream.getBuild(buildId) != null) {

            try {
                buildCard.triggerManualBuild(itemGroup, Integer.parseInt(buildId),
                        project.getRelativeNameFrom(itemGroup),
                        upstream.getRelativeNameFrom(itemGroup));
            } catch (Exception e) {
                throw new TriggerException("Could not trigger", e);
            }
        } else {
            throw new TriggerException("Could not find build: " + buildId + " for project: " + upstream);
        }
    }
}
