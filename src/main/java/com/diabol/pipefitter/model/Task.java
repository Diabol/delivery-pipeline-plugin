package com.diabol.pipefitter.model;

import com.diabol.pipefitter.model.status.Status;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Task extends Component
{
    private Task()
    {
        super(null, null);
    }

    public Task(String name, Status status) {
        super(name, status);
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj instanceof Task && super.equals(obj);
    }
}
