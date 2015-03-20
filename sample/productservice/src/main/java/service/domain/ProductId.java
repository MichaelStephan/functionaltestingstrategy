package service.domain;

import java.util.UUID;

/**
 * Created by i303874 on 3/18/15.
 */
public class ProductId {
    private UUID value;

    public ProductId(UUID value) {
        this.value = value;
    }

    public UUID getValue() {
        return value;
    }
}
