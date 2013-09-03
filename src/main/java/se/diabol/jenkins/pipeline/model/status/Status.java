package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.ExportedBean;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@ExportedBean
public interface Status
{
    public boolean isIdle();

    public boolean isQueued();

    public boolean isRunning();

    public boolean isSuccess();

    public boolean isFailed();

    public boolean isUnstable();

    public boolean isCancelled();
}
