package se.diabol.pipefitter.model.status;

/**
* @author Per Huss <mr.per.huss@gmail.com>
*/
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
}
