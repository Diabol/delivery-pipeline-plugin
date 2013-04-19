package com.diabol.pipefitter.model;

import static com.google.common.base.Objects.toStringHelper;

/**
 * This is the common abstraction for all the entities that makes a pipeline.
 *
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public abstract class Component
{
    private final String name;

    private final Status status;

    protected Component(String name)
    {
        this.name = name;
        this.status = Status.createIdle();
    }

    @Override
    public String toString()
    {
        return toStringHelper(this).add("name", name).add("status", status).toString();
    }
}
