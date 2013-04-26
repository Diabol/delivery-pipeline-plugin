package com.diabol.pipefitter.dashboard;

import com.diabol.pipefitter.model.Product;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Per Huss <mr.per.huss@gmail.com>
 */
@XmlRootElement
public class View
{
    @XmlElement(name = "product")
    private List<Product> products = new ArrayList<>();

    /**
     * Constructs an empty view.
     */
    public View()
    {
    }

    /**
     * Constructs a view of the given products.
     * @param products
     */
    public View(List<Product> products)
    {
        this.products.addAll(products);
    }
}
