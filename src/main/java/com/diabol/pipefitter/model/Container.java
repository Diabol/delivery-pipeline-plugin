package com.diabol.pipefitter.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public abstract class Container<T extends Component> extends Component
{
    private final List<T> components;

    public Container(String name)
    {
        super(name);
        components = new ArrayList<>();
    }
}
