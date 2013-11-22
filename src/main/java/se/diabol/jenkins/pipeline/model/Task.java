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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.model.status.Status;

import static com.google.common.base.Objects.toStringHelper;

public class Task extends AbstractItem {
    private final String id;
    private final String link;
    private final TestResult testResult;
    private final Status status;
    private final boolean manual;
    private final String buildId;

    public Task(String id, String name, String buildId, Status status, String link, boolean manual,
                TestResult testResult) {
        super(name);
        this.id = id;
        this.link = link;
        this.testResult = testResult;
        this.status = status;
        this.manual = manual;
        this.buildId = buildId;
    }

    @Exported
    public boolean isManual() {
        return manual;
    }

    @Exported
    public String getBuildId() {
        return buildId;
    }

    @Exported
    public String getId() {
        return id;
    }

    @Exported
    public String getLink() {
        return link;
    }

    @Exported
    @SuppressWarnings("unused")
    public TestResult getTestResult() {
        return testResult;
    }

    @Exported
    public Status getStatus() {
        return status;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).append(status).appendSuper(super.hashCode()).toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Task && equals((Task) obj);
    }

    private boolean equals(Task o) {
        return new EqualsBuilder().append(id, o.id).append(status, o.status).appendSuper(super.equals(o)).isEquals();
    }

    @Override
    public String toString() {
        return toStringHelper(this)
                .add("id", getId())
                .add("name", getName())
                .add("status", getStatus())
                .toString();
    }
}
