package se.diabol.jenkins.pipeline.model.status;

import org.kohsuke.stapler.export.Exported;
import org.kohsuke.stapler.export.ExportedBean;

/**
* @author Per Huss <mr.per.huss@gmail.com>
*/
@ExportedBean(defaultVisibility = 100)
public class Running extends SimpleStatus
{
    private final int percentage;

    Running(int percentage)
    {
        super(StatusType.RUNNING);
        this.percentage = percentage;
    }

    @Exported
    public int getPercentage() { return percentage; }

    @Override
    public boolean isRunning() { return true; }

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
