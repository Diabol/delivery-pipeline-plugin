package se.diabol.jenkins.pipeline.model;

import se.diabol.jenkins.pipeline.model.status.Status;

import java.util.Objects;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Task extends Component
{
    private final Object id;
    private final String link;

    private Task()
    {
        this(null, null, null, null);
    }

    public Task(Object id, String name, Status status, String link)
    {
        super(name, status);
        this.id = id;
        this.link = link;
    }

    public Object getId()
    {
        return id;
    }

    public String getLink()
    {
        return link;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, super.hashCode());
    }

    @Override
    public boolean equals(Object obj)
    {
        return this == obj || obj instanceof Task && equals((Task) obj);
    }

    private boolean equals(Task o)
    {
        return Objects.equals(id, o.id) && super.equals(o);
    }


}
