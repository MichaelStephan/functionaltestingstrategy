package dao;

import com.google.common.util.concurrent.SettableFuture;
import service.domain.Product;
import service.domain.ProductId;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by i303874 on 3/18/15.
 */
public interface ProductDao {
    Future<Product> save(SettableFuture<Product> result, Product product);

    Future<List<Product>> get(SettableFuture<List<Product>> result, ProductId id);
}
