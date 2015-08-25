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
package se.diabol.jenkins.pipeline.domain.results;

import hudson.model.Action;
import hudson.model.AbstractBuild;
import hudson.plugins.analysis.core.ResultAction;
import hudson.plugins.analysis.core.AbstractResultAction;
import hudson.plugins.analysis.core.BuildResult;
import hudson.plugins.analysis.core.MavenResultAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

import se.diabol.jenkins.pipeline.util.JenkinsUtil;

public class StaticAnalysisResult extends Result {

    private static final String ANALYSIS_CORE_PLUGIN = "analysis-core";

    private int high;
    private int normal;
    private int low;

    public StaticAnalysisResult(String name, String url, int high, int normal, int low) {
        super(name, url);
        this.high = high;
        this.normal = normal;
        this.low = low;
    }

    @Exported
    public String getName() {
        return name;
    }

    @Exported
    public int getHigh() {
        return high;
    }

    @Exported
    public int getNormal() {
        return normal;
    }

    @Exported
    public int getLow() {
        return low;
    }

    @Exported
    public String getUrl() {
        return url;
    }

    @SuppressWarnings("deprecation")
    public static List<StaticAnalysisResult> getResults(AbstractBuild<?, ?> build) {
        if (build != null) {
            if (JenkinsUtil.isPluginInstalled(ANALYSIS_CORE_PLUGIN)) {
                List<StaticAnalysisResult> result = new ArrayList<StaticAnalysisResult>();
                for (Action action : build.getActions()) {
                    if (AbstractResultAction.class.isInstance(action) || MavenResultAction.class.isInstance(action)) {
                        @SuppressWarnings("rawtypes")
                        final BuildResult r = ((ResultAction) action).getResult();
                        result.add(new StaticAnalysisResult(
                                action.getDisplayName(),
                                build.getUrl() + action.getUrlName(),
                                r.getNumberOfHighPriorityWarnings(),
                                r.getNumberOfNormalPriorityWarnings(),
                                r.getNumberOfLowPriorityWarnings()));
                    }
                }
                return result;
            }
        }
        return Collections.emptyList();
    }

}
