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
package se.diabol.jenkins.pipeline.domain.status.promotion;


import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;
import se.diabol.jenkins.pipeline.domain.AbstractItem;

import java.util.List;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class PromotionStatus {
    final private String name;
    final private long duration;
    final private long startTime;
    final private String user;
    final private String icon;
    final private List<String> params;

    public PromotionStatus(String name, long startTime, long duration, String user, String icon, List<String> params) {
        this.name = name;
        this.startTime = startTime;
        this.duration = duration;
        this.user = user;
        this.icon = icon;
        this.params = params;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public long getStartTime() {
        return startTime;
    }

    @Exported
    public long getDuration() {
        return duration;
    }

    @Exported
    public String getUser() {
        return user;
    }

    @Exported
    public String getIcon() {
        return icon;
    }

    @Exported
    public List<String> getParams() {
        return params;
    }
}
