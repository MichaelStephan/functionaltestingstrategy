package service.domain;

import java.math.BigDecimal;

/**
 * Created by i303874 on 3/20/15.
 */
public class APIPrice {
    private String id;

    private BigDecimal value;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    private String currency;
}
