package service.domain;

import java.math.BigDecimal;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/20/15.
 */
public class Price {
    private String id;

    private String currency;

    private BigDecimal value;

    public Price(String id, String currency, BigDecimal value) {
        this.id = id;
        this.currency = checkNotNull(currency);
        this.value = checkNotNull(value);
    }

    public String getId() {
        return id;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
