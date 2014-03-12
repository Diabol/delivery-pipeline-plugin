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
import hudson.model.Cause;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;
import jenkins.model.Jenkins;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.ArrayList;
import java.util.List;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Trigger {

    private String type;
    private String description;

    public static final String TYPE_UPSTREAM = "UPSTREAM";
    public static final String TYPE_MANUAL = "MANUAL";
    public static final String TYPE_REMOTE = "REMOTE";
    public static final String TYPE_SCM = "SCM";
    public static final String TYPE_TIMER = "TIMER";
    public static final String TYPE_UNKNOWN = "UNKNOWN";


    public Trigger(String type, String description) {
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

    public static List<Trigger> getTriggeredBy(AbstractBuild<?, ?> build) {
        List<Trigger> result = new ArrayList<Trigger>();
        List<Cause> causes = build.getCauses();
        for (Cause cause : causes) {
           if(cause instanceof Cause.UserIdCause){
               result.add(new Trigger(Trigger.TYPE_MANUAL, "user " + getDisplayName(((Cause.UserIdCause) cause).getUserName())));
           } else if(cause instanceof Cause.RemoteCause){
               result.add(new Trigger(Trigger.TYPE_REMOTE, "remote trigger"));
           } else if(cause instanceof Cause.UpstreamCause || cause instanceof Cause.UpstreamCause.DeeplyNestedUpstreamCause){
               //TODO add which project!
               result.add(new Trigger(Trigger.TYPE_UPSTREAM, "upstream"));
           } else if(cause instanceof SCMTrigger.SCMTriggerCause){
               result.add(new Trigger(Trigger.TYPE_SCM, "SCM change"));
           } else if(cause instanceof TimerTrigger.TimerTriggerCause){
               result.add(new Trigger(Trigger.TYPE_TIMER, "timer"));
           } else {
               result.add(new Trigger(Trigger.TYPE_UNKNOWN, "unknown cause"));
           }
        }
        return result;
    }

    private static String getDisplayName(String userName) {
        return Jenkins.getInstance().getUser(userName).getDisplayName();
    }


}
