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

import hudson.ExtensionPoint;
import hudson.model.AbstractProject;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;

import java.util.List;

public abstract class ManualTriggerResolver implements ExtensionPoint {

    public abstract ManualTrigger getManualTrigger(AbstractProject<?, ?> project, AbstractProject<?, ?> downstream);

    public abstract boolean isManualTrigger(AbstractProject<?, ?> project);

    public abstract List<AbstractProject> getUpstreamManualTriggered(AbstractProject<?, ?> project);

    public static List<ManualTriggerResolver> all() {
        return JenkinsUtil.getInstance().getExtensionList(ManualTriggerResolver.class);
    }
}
