package api;

import api.domain.APIProduct;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ProductService;
import service.domain.Product;
import service.domain.ProductId;

import javax.inject.Singleton;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.NoSuchElementException;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 1/2/15.
 */
@Singleton
@Path("/productservice")
public class API implements ExceptionMapper<Exception> {
    private final static Logger logger = LoggerFactory.getLogger(API.class);

    private ProductService productService;

    public API(ProductService productService) {
        this.productService = checkNotNull(productService);
    }

    private APIProduct transform(service.domain.Product src) {
        APIProduct dst = new APIProduct();
        dst.setId(src.getId().getValue().toString());
        dst.setName(src.getName());
        dst.setDescription(src.getDescription());
        return dst;
    }

    private service.domain.Product transform(APIProduct src) {
        ProductId id = new ProductId(src.getId() == null ? null : UUID.fromString(src.getId()));
        return new service.domain.Product(id, src.getName(), src.getDescription());
    }

    @POST
    @Path("/products")
    @Produces(MediaType.APPLICATION_JSON)
    public void postProducts(@Suspended final AsyncResponse asyncResponse, @Context HttpHeaders headers, APIProduct apiProduct) {
        checkNotNull(apiProduct);

        SettableFuture<Product> result = SettableFuture.create();
        Futures.addCallback(result, new FutureCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                asyncResponse.resume(Response.ok().entity(transform(product)).build());
            }

            @Override
            public void onFailure(Throwable throwable) {
                asyncResponse.resume(throwable);
            }
        });

        productService.create(result, transform(apiProduct));
    }

    @GET
    @Path("/products/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public void getProduct(@Suspended final AsyncResponse asyncResponse, @Context HttpHeaders headers, @PathParam("id") String id) {
        checkNotNull(id);

        SettableFuture<Product> result = SettableFuture.create();
        Futures.addCallback(result, new FutureCallback<Product>() {
            @Override
            public void onSuccess(Product product) {
                asyncResponse.resume(Response.ok().entity(transform(product)).build());
            }

            @Override
            public void onFailure(Throwable throwable) {
                asyncResponse.resume(throwable);
            }
        });

        productService.get(result, new ProductId(UUID.fromString(id)));
    }

    @Override
    public Response toResponse(Exception e) {
        logger.error("an API call caused an exception", e);

        if (e instanceof javax.ws.rs.NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else if (e instanceof NoSuchElementException) {
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        }
    }
}
