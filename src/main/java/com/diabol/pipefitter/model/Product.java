package com.diabol.pipefitter.model;

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
        pipelines = new ArrayList<>();
    }
}
