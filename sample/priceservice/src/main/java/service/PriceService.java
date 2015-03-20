package service;

import com.google.common.util.concurrent.SettableFuture;
import service.domain.Price;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by i303874 on 3/20/15.
 */
public interface PriceService {
    Future<List<Price>> get(SettableFuture<List<Price>> result, String id);

}
