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

import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Cause;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.CauseResolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class TriggerCause {

    private String type;
    private String description;

    public static final String TYPE_UPSTREAM = "UPSTREAM";
    public static final String TYPE_MANUAL = "MANUAL";
    public static final String TYPE_REMOTE = "REMOTE";
    public static final String TYPE_SCM = "SCM";
    public static final String TYPE_TIMER = "TIMER";
    public static final String TYPE_UNKNOWN = "UNKNOWN";


    public TriggerCause(String type, String description) {
        this.type = type;
        this.description = description;
    }

    @Exported
    public String getType() {
        return type;
    }

    @Exported
    public String getDescription() {
        return description;
    }

    public static List<TriggerCause> getTriggeredBy(AbstractProject project, AbstractBuild<?, ?> build) {
        Set<TriggerCause> result = new HashSet<TriggerCause>();
        List<Cause> causes;
        if (build == null && project.isInQueue()) {
            causes = project.getQueueItem().getCauses();
        } else {
            if (build != null) {
                causes = build.getCauses();
            } else {
                return new ArrayList<TriggerCause>();
            }

        }
        for (Cause cause : causes) {
            result.add(CauseResolver.getTrigger(cause));
        }
        return new ArrayList<TriggerCause>(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()){
            return false;
        }
        TriggerCause trigger = (TriggerCause) o;

        return description.equals(trigger.description) && type.equals(trigger.type);

    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + description.hashCode();
        return result;
    }
}
