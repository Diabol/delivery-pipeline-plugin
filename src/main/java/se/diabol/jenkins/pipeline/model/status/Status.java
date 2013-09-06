package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean
public interface Status
{
    @SuppressWarnings("unused")
    public boolean isIdle();

    @SuppressWarnings("unused")
    public boolean isQueued();

    @SuppressWarnings("unused")
    public boolean isRunning();

    @SuppressWarnings("unused")
    public boolean isSuccess();

    @SuppressWarnings("unused")
    public boolean isFailed();

    @SuppressWarnings("unused")
    public boolean isUnstable();

    @SuppressWarnings("unused")
    public boolean isCancelled();
}
