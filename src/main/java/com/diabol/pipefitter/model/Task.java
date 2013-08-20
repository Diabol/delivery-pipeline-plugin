package com.diabol.pipefitter.model;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Task extends Component {

    private int percentDone = 100;

    public Task(String name, Status status) {
        super(name, status);
    }

    public Task(String name, Status status, int percentDone) {
        super(name, status);
        this.percentDone = percentDone;
    }

    public int getPercentDone() {
        return percentDone;
    }
}
