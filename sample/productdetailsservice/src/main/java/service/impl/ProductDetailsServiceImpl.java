package service.impl;

import com.google.common.util.concurrent.SettableFuture;
import dao.ProductDetailsContentDao;
import service.ProductDetailsService;
import service.domain.ProductDetailsContent;

import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/20/15.
 */
public class ProductDetailsServiceImpl implements ProductDetailsService {

    private ProductDetailsContentDao productDetailsContentDao;

    public ProductDetailsServiceImpl(ProductDetailsContentDao productDetailsContentDao) {
        this.productDetailsContentDao = checkNotNull(productDetailsContentDao);
    }

    public Future<ProductDetailsContent> get(SettableFuture<ProductDetailsContent> result, String id) {
        productDetailsContentDao.get(result, id);
        return result;
    }
}
