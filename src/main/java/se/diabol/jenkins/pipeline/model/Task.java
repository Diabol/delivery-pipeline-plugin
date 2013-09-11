package se.diabol.jenkins.pipeline.model;

import org.kohsuke.stapler.export.Exported;
import se.diabol.jenkins.pipeline.model.status.Status;

import java.util.Objects;

import static com.google.common.base.Objects.toStringHelper;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Task extends AbstractItem
{
    private final String id;
    private final String link;
    private final TestResult testResult;
    private final Status status;

    public Task(String id, String name, Status status, String link, TestResult testResult)
    {
        super(name);
        this.id = id;
        this.link = link;
        this.testResult = testResult;
        this.status = status;
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
    public Status getStatus()
    {
        return status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, status, super.hashCode());
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj || obj instanceof Task && equals((Task) obj);
    }

    private boolean equals(Task o) {
        return Objects.equals(id, o.id) && Objects.equals(status, o.status) && super.equals(o);
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
