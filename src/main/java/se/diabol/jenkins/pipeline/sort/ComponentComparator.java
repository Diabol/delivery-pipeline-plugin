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

import hudson.DescriptorExtensionList;
import hudson.ExtensionPoint;
import hudson.model.Describable;
import hudson.model.Descriptor;
import se.diabol.jenkins.pipeline.domain.Component;
import se.diabol.jenkins.pipeline.util.JenkinsUtil;

import java.util.Comparator;

public abstract class ComponentComparator implements Comparator<Component>, ExtensionPoint, Describable<ComponentComparator> {

    @Override
    public Descriptor<ComponentComparator> getDescriptor() {
        return (ComponentComparatorDescriptor) JenkinsUtil.getInstance().getDescriptor(getClass());
    }

    public static DescriptorExtensionList<ComponentComparator, ComponentComparatorDescriptor> all() {
        return JenkinsUtil.getInstance().getDescriptorList(ComponentComparator.class);
    }
}
