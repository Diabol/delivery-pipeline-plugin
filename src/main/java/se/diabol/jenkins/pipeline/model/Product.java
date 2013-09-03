package se.diabol.jenkins.pipeline.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Product
{
    private final String name;

    private Pipeline prototype;

    private final List<Pipeline> pipelines;

    public Product(String name) {
        this.name = name;
        pipelines = new ArrayList<Pipeline>();
    }
}
