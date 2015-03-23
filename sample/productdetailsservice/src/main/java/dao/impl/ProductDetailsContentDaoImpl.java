package dao.impl;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import dao.ProductDetailsContentDao;
import service.domain.APIPrice;
import service.domain.ProductDetailsContent;

import java.util.List;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/20/15.
 */
public class ProductDetailsContentDaoImpl implements ProductDetailsContentDao {
    private PriceServiceDaoImpl priceService;

    public ProductDetailsContentDaoImpl(PriceServiceDaoImpl priceService) {
        this.priceService = checkNotNull(priceService);
    }

    @Override
    public Future<ProductDetailsContent> get(SettableFuture<ProductDetailsContent> result, String id) {
        SettableFuture<List<APIPrice>> getResult = SettableFuture.create();
        Futures.addCallback(getResult, new com.google.common.util.concurrent.FutureCallback<List<APIPrice>>() {
            @Override
            public void onSuccess(List<APIPrice> apiPrices) {
                ProductDetailsContent content = new ProductDetailsContent();
                content.setId("123");
                content.setName("name");
                content.setDescription("description");

                if (!apiPrices.isEmpty()) {
                    APIPrice price = apiPrices.get(0);
                    content.setCurrency(price.getCurrency());
                    content.setPrice(price.getValue());
                }
                result.set(content);
            }

            @Override
            public void onFailure(Throwable throwable) {
                result.setException(throwable);
            }
        });
        priceService.getPrices(getResult, id);
        return result;
    }
}
