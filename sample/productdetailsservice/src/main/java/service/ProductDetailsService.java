package service;

import com.google.common.util.concurrent.SettableFuture;
import service.domain.ProductDetailsContent;

import java.util.concurrent.Future;

/**
 * Created by i303874 on 3/20/15.
 */
public interface ProductDetailsService {
    Future<ProductDetailsContent> get(SettableFuture<ProductDetailsContent> result, String id);
}
