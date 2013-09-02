package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.ExportedBean;

/**
* @author Per Huss <mr.per.huss@gmail.com>
*/
@ExportedBean
class Running implements Status
{
    private final int percentage;

    Running(int percentage) { this.percentage = percentage; }

    @Override public int getPercentage() { return percentage; }
    @Override public boolean isRunning() { return true; }
    @Override public boolean isSuccess() { return false; }
    @Override public boolean isFailed() { return false; }
    @Override public boolean isStarted() { return false; }
    @Override public boolean isIdle() { return false; }

    @Override
    public String toString()
    {
        return "RUNNING " + percentage + "%";
    }

    @Override
    public boolean equals(Object o)
    {
        return this == o || o instanceof Running && percentage == ((Running) o).percentage;
    }

    @Override
    public int hashCode()
    {
        return percentage;
    }
}
