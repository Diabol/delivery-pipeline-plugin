package com.diabol.pipefitter.model;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Pipeline extends Component
{
    @XmlElement(name = "stage")
    private List<Stage> stages;

    public Pipeline(String name)
    {
        super(name);
    }
}
