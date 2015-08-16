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

import static com.google.common.base.Objects.toStringHelper;
import hudson.model.AbstractBuild;
import hudson.plugins.cobertura.CoberturaBuildAction;
import hudson.plugins.cobertura.targets.CoverageMetric;
import hudson.plugins.jacoco.JacocoBuildAction;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.stapler.export.Exported;

import se.diabol.jenkins.pipeline.util.PluginUtil;

public class CoverageResult extends Result {

    private static final String COBERTUR_PLUGIN = "cobertur";
    private static final String JACOCO_PLUGIN = "jacoco";

    private float line;
    private float method;
    private float classes;

    public CoverageResult(String name, String url, float line, float method, float classes) {
        super(name, url);
        this.line = line;
        this.method = method;
        this.classes = classes;
    }

    @Exported
    public float getLine() {
        return line;
    }

    @Exported
    public float getMethod() {
        return method;
    }

    @Exported
    public float getClasses() {
        return classes;
    }

    public static List<CoverageResult> getResults(AbstractBuild<?, ?> build) {
        if (build != null) {
            List<CoverageResult> result = new ArrayList<CoverageResult>();
            /* CoberturaResult */
            if (PluginUtil.isPluginInstalled(COBERTUR_PLUGIN)) {
                CoberturaBuildAction action = build.getAction(CoberturaBuildAction.class);
                if (action != null) {
                    final hudson.plugins.cobertura.targets.CoverageResult r = action.getResult();
                    result.add(new CoverageResult(
                                action.getDisplayName(),
                                build.getUrl() + action.getUrlName(),
                                r.getCoverage(CoverageMetric.LINE).getPercentageFloat(),
                                r.getCoverage(CoverageMetric.METHOD).getPercentageFloat(),
                                r.getCoverage(CoverageMetric.CLASSES).getPercentageFloat()));
                }
            }
            /* JacocoResult */
            if (PluginUtil.isPluginInstalled(JACOCO_PLUGIN)) {
                JacocoBuildAction action = build.getAction(JacocoBuildAction.class);
                if (action != null) {
                    result.add(new CoverageResult(
                                action.getDisplayName(),
                                build.getUrl() + action.getUrlName(),
                                action.getLineCoverage().getPercentageFloat(),
                                action.getMethodCoverage().getPercentageFloat(),
                                action.getClassCoverage().getPercentageFloat()));
                }
            }
            System.out.println(result);
            return result;
        }
        return null;
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("name", name)
                .add("url", url)
                .add("line", line)
                .add("method", method)
                .add("classes", classes).toString();
    }

}
