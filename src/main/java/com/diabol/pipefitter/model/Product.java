package com.diabol.pipefitter.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
public class Product
{
    @XmlAttribute
    private final String name;

    @XmlElement
    private Pipeline prototype;

    @XmlElement(name = "pipeline")
    private final List<Pipeline> pipelines;

    public Product(String name) {
        this.name = name;
        pipelines = new ArrayList<Pipeline>();
    }
}
