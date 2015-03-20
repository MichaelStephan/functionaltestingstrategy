package service;

import com.google.common.util.concurrent.SettableFuture;
import service.domain.Product;
import service.domain.ProductId;

import java.util.concurrent.Future;

/**
 * Created by i303874 on 3/18/15.
 */
public interface ProductService {
    Future<Product> create(SettableFuture<Product> result, Product product);

    Future<Product> get(SettableFuture<Product> result, ProductId id);
}