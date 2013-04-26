package com.diabol.pipefitter.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Stage extends Component
{
    @XmlElement(name = "task")
    private List<Task> tasks;

    public Stage(String name)
    {
        super(name);
    }
}
