package api;

import api.expectations.APITest_Expectations_v0;
import api.expectations.APITest_Expectations_v1;
import com.google.common.util.concurrent.SettableFuture;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.Test;
import org.mockito.Mockito;
import server.Server;
import service.ProductService;
import service.domain.Product;
import service.domain.ProductId;

import java.util.UUID;

/**
 * Created by i303874 on 3/19/15.
 */
public class APITest {
    private final static int PORT = 9001;

    private Product createNewProduct() {
        return new Product(new ProductId(UUID.randomUUID()), "someName", "someDescription");
    }

    private String getGetUrl() {
        return "http://localhost:" + PORT + "/productservice/products/f81d4fae-7dec-11d0-a765-00a0c91e6bf6";
    }

    @Test
    public void givenValidInputToGetThenReturns500() throws Exception {
        ProductService productService = Mockito.mock(ProductService.class);
        Mockito.when(productService.get((SettableFuture<Product>) Mockito.notNull(), (ProductId) Mockito.notNull())).thenAnswer(invocationOnMock -> {
            SettableFuture<Product> result = (SettableFuture<Product>) invocationOnMock.getArguments()[0];
            result.setException(new IllegalArgumentException());
            return result;
        });

        new Server().run(PORT, () -> {
            new APITest_Expectations_v0(getGetUrl()).givenValidInputToGetThenReturns500();
        }, new ResourceConfig().register(new API(productService))).join();
    }

    @Test
    public void givenValidInputToGetThenReturnsProduct() throws Exception {
        ProductService productService = Mockito.mock(ProductService.class);
        Mockito.when(productService.get((SettableFuture<Product>) Mockito.notNull(), (ProductId) Mockito.notNull())).thenAnswer(invocationOnMock -> {
            SettableFuture<Product> result = (SettableFuture<Product>) invocationOnMock.getArguments()[0];
            result.set(createNewProduct());
            return result;
        });

        new Server().run(PORT, () -> {
            new APITest_Expectations_v0(getGetUrl()).givenValidInputToGetThenReturnsProduct();
            new APITest_Expectations_v1(getGetUrl()).givenValidInputToGetThenReturnsProduct();
        }, new ResourceConfig().register(new API(productService))).join();
    }
}