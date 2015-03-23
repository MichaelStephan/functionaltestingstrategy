package service.impl;

import com.google.common.util.concurrent.SettableFuture;
import service.PriceService;
import service.domain.Price;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

/**
 * Created by i303874 on 3/20/15.
 */
public class PriceServiceImpl implements PriceService {
    @Override
    public Future<List<Price>> get(SettableFuture<List<Price>> result, String id) {
        result.set(Arrays.asList(new Price[]{
                new Price(UUID.randomUUID().toString(), "EUR", new BigDecimal(123))
        }));
        return result;
    }
}
