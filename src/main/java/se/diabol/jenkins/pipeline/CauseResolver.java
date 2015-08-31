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
package se.diabol.jenkins.pipeline;

import hudson.ExtensionPoint;
import hudson.model.Cause;
import java.util.List;
import se.diabol.jenkins.pipeline.domain.TriggerCause;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;

import javax.annotation.CheckForNull;

public abstract class CauseResolver implements ExtensionPoint {

    /**
     * Resolves the Cause and returns a TriggerCause or null if not found.
     *
     * @param cause cause from the Jenkins build
     * @return a TriggerCause or null if the CauseResolver implementation cant find a proper cause indication that
     * other resolvers should make a try
     */
    @CheckForNull
    public abstract TriggerCause resolveCause(Cause cause);

    /**
     * Returns all loaded implementations of this extension point.
     */
    public static List<CauseResolver> all() {
        return JenkinsUtil.getInstance().getExtensionList(CauseResolver.class);
    }

    public static TriggerCause getTrigger(Cause cause) {
        List<CauseResolver> resolvers = all();
        for (CauseResolver resolver : resolvers) {
            TriggerCause triggerCause = resolver.resolveCause(cause);
            if (triggerCause != null) {
                return triggerCause;
            }
        }
        return new TriggerCause(TriggerCause.TYPE_UNKNOWN, "unknown cause");
    }

}
