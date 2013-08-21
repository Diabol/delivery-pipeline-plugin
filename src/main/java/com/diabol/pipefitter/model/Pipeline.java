package com.diabol.pipefitter.model;

import com.diabol.pipefitter.model.status.StatusFactory;
import com.google.common.collect.ImmutableList;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Pipeline extends Component
{
    @XmlElement(name = "stage")
    private List<Stage> stages;

    public Pipeline(String name, List<Stage> stages)
    {
        super(name, StatusFactory.idle()); // todo: IDLE is cheating
        this.stages = ImmutableList.copyOf(stages);
    }

    public List<Stage> getStages()
    {
        return stages;
    }
}
