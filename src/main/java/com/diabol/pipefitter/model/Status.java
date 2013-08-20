package com.diabol.pipefitter.model;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public enum Status {
    SUCCESS, FAILED, UNSTABLE, RUNNING, CANCELLED, DISABLED, UNKNOWN;

    public boolean isRunning() {
        return this == RUNNING;
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

}
