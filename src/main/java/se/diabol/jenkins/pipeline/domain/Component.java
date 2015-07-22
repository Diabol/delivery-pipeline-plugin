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

import com.google.common.collect.ImmutableList;
import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import java.util.List;

import static com.google.common.base.Objects.toStringHelper;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class Component extends AbstractItem {
    private final List<Pipeline> pipelines;
    private final String firstJob;
    private final String firstJobUrl;
    private final boolean firstJobParameterized;

    public Component(String name, String firstJob, String firstJobUrl, boolean firstJobParameterized,
            List<Pipeline> pipelines) {
        super(name);
        this.pipelines = ImmutableList.copyOf(pipelines);
        this.firstJob = firstJob;
        this.firstJobUrl = firstJobUrl;
        this.firstJobParameterized = firstJobParameterized;
    }

    @Exported
    public List<Pipeline> getPipelines() {
        return pipelines;
    }

    @Exported
    public String getFirstJob() {
        return firstJob;
    }

    @Exported
    public String getFirstJobUrl() {
        return firstJobUrl;
    }

    @Exported
    public boolean isFirstJobParameterized() {
        return firstJobParameterized;
    }

    @Override
    public String toString() {
        return toStringHelper(this).add("name", getName()).add("pipelines", pipelines).toString();
    }
}
