package api;

import service.domain.ProductDetailsContent;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.ProductDetailsService;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/20/15.
 */
@Singleton
@Path("/productdetailsservice")
public class API {
    private final static Logger logger = LoggerFactory.getLogger(API.class);

    private ProductDetailsService productDetailsService;

    public API(ProductDetailsService productDetailsService) {
        this.productDetailsService = checkNotNull(productDetailsService);
    }

    @GET
    @Path("/productdetails/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    public void get(@Suspended final AsyncResponse asyncResponse, @Context HttpHeaders headers, @PathParam("productId") String id) {
        checkNotNull(id);

        SettableFuture<ProductDetailsContent> result = SettableFuture.create();
        Futures.addCallback(result, new FutureCallback<ProductDetailsContent>() {
            @Override
            public void onSuccess(ProductDetailsContent content) {
                asyncResponse.resume(Response.ok().entity(content).build());
            }

            @Override
            public void onFailure(Throwable throwable) {
                asyncResponse.resume(throwable);
            }
        });

        productDetailsService.get(result, id);
    }
}
