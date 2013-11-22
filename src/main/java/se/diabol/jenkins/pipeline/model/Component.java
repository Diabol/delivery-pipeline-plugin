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
package se.diabol.jenkins.pipeline.model;

import com.google.common.collect.ImmutableList;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

import static com.google.common.base.Objects.toStringHelper;

@ExportedBean(defaultVisibility = 100)
public class Component extends AbstractItem {
    private final List<Pipeline> pipelines;

    public Component(String name, List<Pipeline> pipelines) {
        super(name);
        this.pipelines = ImmutableList.copyOf(pipelines);
    }

    @Exported
    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("name", getName()).add("pipelines", pipelines).toString();
    }

    @Override
    public int hashCode() {
        return pipelines.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof Component && equals((Component) o);
    }

    private boolean equals(Component o) {
        return super.equals(o) && pipelines.equals(o.pipelines);
    }
}
