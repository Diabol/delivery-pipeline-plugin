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
package se.diabol.jenkins.core;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public abstract class GenericComponent extends AbstractItem {

    public GenericComponent(String name) {
        super(name);
    }

    @Exported
    public long getLastActivity() {
        long result = 0;
        if (getPipelines() != null) {
            for (GenericPipeline pipeline : getPipelines()) {
                long lastActivity = pipeline.getLastActivity();
                if (lastActivity > result) {
                    result = lastActivity;
                }
            }
        }
        return result;
    }

    @Exported
    public abstract List<? extends GenericPipeline> getPipelines();
}
