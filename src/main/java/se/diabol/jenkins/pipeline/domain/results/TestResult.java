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

import hudson.model.AbstractBuild;
import hudson.tasks.test.AbstractTestResultAction;
import org.kohsuke.stapler.export.Exported;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestResult extends Result {

    private int failed;
    private int skipped;
    private int total;

    public TestResult(String name, String url, int failed, int skipped, int total) {
        super(name, url);
        this.failed = failed;
        this.skipped = skipped;
        this.total = total;
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

    public static List<TestResult> getResults(AbstractBuild<?, ?> build) {
        if (build != null) {
            List<TestResult> result = new ArrayList<TestResult>();
            AbstractTestResultAction resultAction = build.getAction(AbstractTestResultAction.class);
            if (resultAction != null) {
                result.add(new TestResult(
                        resultAction.getDisplayName(),
                        build.getUrl() + resultAction.getUrlName(),
                        resultAction.getFailCount(),
                        resultAction.getSkipCount(),
                        resultAction.getTotalCount()));
            }
            return result;
        }
        return Collections.emptyList();
    }

}
