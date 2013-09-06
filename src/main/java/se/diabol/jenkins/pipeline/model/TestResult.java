package se.diabol.jenkins.pipeline.model;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

@ExportedBean(defaultVisibility = 100)
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
    @SuppressWarnings("unused")
    public int getFailed() {
        return failed;
    }

    @Exported
    @SuppressWarnings("unused")
    public int getSkipped() {
        return skipped;
    }

    @Exported
    @SuppressWarnings("unused")
    public int getTotal() {
        return total;
    }
}
