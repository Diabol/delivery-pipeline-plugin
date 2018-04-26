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
package se.diabol.jenkins.pipeline.sort;

import hudson.Extension;
import se.diabol.jenkins.core.GenericComponent;

import java.io.Serializable;

public class LatestActivityComparator extends GenericComponentComparator implements Serializable {

    @Override
    public int compare(GenericComponent component1, GenericComponent component2) {
        long component1LastActivity = component1 == null ? 0 : component1.getLastActivity();
        long component2LastActivity = component2 == null ? 0 : component2.getLastActivity();
        return Long.compare(component2LastActivity, component1LastActivity);
    }

    @Extension
    public static class DescriptorImpl extends ComponentComparatorDescriptor {
        @Override
        public String getDisplayName() {
            return "Sorting by last activity";
        }

        @Override
        public GenericComponentComparator createInstance() {
            return new LatestActivityComparator();
        }
    }
}
