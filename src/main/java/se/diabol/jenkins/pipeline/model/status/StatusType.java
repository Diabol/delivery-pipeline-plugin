package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.ExportedBean;

/**
* @author Per Huss <mr.per.huss@gmail.com>
*/
public enum StatusType
{
    IDLE, RUNNING, QUEUED, SUCCESS, UNSTABLE, FAILED, CANCELLED, DISABLED
}
