package com.diabol.pipefitter.model;

import com.diabol.pipefitter.model.status.Status;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Task extends Component
{
    public Task(String name, Status status) {
        super(name, status);
    }
}
