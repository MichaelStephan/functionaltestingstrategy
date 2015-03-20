package service.impl;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import dao.ProductDao;
import service.ProductService;
import service.domain.Product;
import service.domain.ProductId;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/18/15.
 */
public class ProductServiceImpl implements ProductService {
    private ProductDao dao;

    public ProductServiceImpl(ProductDao dao) {
        this.dao = checkNotNull(dao);
    }

    public Future<Product> create(SettableFuture<Product> result, Product product) {
        dao.save(result, product);
        return result;
    }

    public Future<Product> get(SettableFuture<Product> result, ProductId id) {
        SettableFuture<List<Product>> daoResult = SettableFuture.create();
        Futures.addCallback(daoResult, new FutureCallback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                if (products.isEmpty()) {
                    result.setException(new NoSuchElementException());
                } else {
                    result.set(products.get(0));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                result.setException(throwable);
            }
        });

        dao.get(daoResult, id);
        return result;
    }
}
