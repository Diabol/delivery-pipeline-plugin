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
package se.diabol.jenkins.pipeline.domain.task;

import hudson.model.AbstractBuild;
import hudson.tasks.test.AggregatedTestResultAction;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

import se.diabol.jenkins.pipeline.domain.AbstractItem;

@ExportedBean(defaultVisibility = AbstractItem.VISIBILITY)
public class TestResult {

    private int failed;
    private int skipped;
    private int total;
    private String url;

    public TestResult(int failed, int skipped, int total, String url) {
        this.failed = failed;
        this.skipped = skipped;
        this.total = total;
        this.url = url;
    }

    @Exported
    @SuppressWarnings("unused")
    public String getUrl() {
        return url;
    }

    @Exported
    public int getFailed() {
        return failed;
    }

    @Exported
    public int getSkipped() {
        return skipped;
    }

    @Exported
    public int getTotal() {
        return total;
    }

    public static TestResult getTestResult(AbstractBuild build) {
        if (build != null) {
            AggregatedTestResultAction tests = build.getAction(AggregatedTestResultAction.class);
            if (tests != null) {
                return new TestResult(tests.getFailCount(), tests.getSkipCount(), tests.getTotalCount(),
                        build.getUrl() + tests.getUrlName());
            }
        }
        return null;
    }

}
