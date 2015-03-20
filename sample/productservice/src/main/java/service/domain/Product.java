package service.domain;

/**
 * Created by i303874 on 1/2/15.
 */
public class Product {
    public Product(ProductId id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public ProductId id;

    public String name;

    public String description;

    public ProductId getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }


}
