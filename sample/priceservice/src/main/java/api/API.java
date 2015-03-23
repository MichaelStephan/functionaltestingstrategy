package api;

import api.domain.APIPrice;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.PriceService;
import service.domain.Price;

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
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/20/15.
 */
@Singleton
@Path("/priceservice")
public class API {
    private final static Logger logger = LoggerFactory.getLogger(API.class);

    private PriceService priceService;

    public API(PriceService priceService) {
        this.priceService = checkNotNull(priceService);
    }

    private List<APIPrice> transform(List<Price> prices) {
        return prices.stream().map((price) -> {
            return transform(price);
        }).collect(Collectors.toList());
    }

    private APIPrice transform(Price price) {
        APIPrice apiPrice = new APIPrice();
        apiPrice.setId(price.getId());
        apiPrice.setCurrency(price.getCurrency());
        apiPrice.setValue(price.getValue());
        return apiPrice;
    }

    @GET
    @Path("/products/{productId}/prices")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPrices(@Suspended final AsyncResponse asyncResponse, @Context HttpHeaders headers, @PathParam("productId") String id) {
        checkNotNull(id);

        SettableFuture<List<Price>> result = SettableFuture.create();
        Futures.addCallback(result, new FutureCallback<List<Price>>() {
            @Override
            public void onSuccess(List<Price> prices) {
                asyncResponse.resume(Response.ok().entity(transform(prices)).build());
            }

            @Override
            public void onFailure(Throwable throwable) {
                asyncResponse.resume(throwable);
            }
        });

        priceService.get(result, id);
    }

    @GET
    @Path("/products/{productId}/price")
    @Produces(MediaType.APPLICATION_JSON)
    public void getPrice(@Suspended final AsyncResponse asyncResponse, @Context HttpHeaders headers, @PathParam("productId") String id) {
        checkNotNull(id);

        SettableFuture<List<Price>> result = SettableFuture.create();
        Futures.addCallback(result, new FutureCallback<List<Price>>() {
            @Override
            public void onSuccess(List<Price> prices) {
                asyncResponse.resume(Response.ok().entity(transform(prices.stream().findFirst().get())).build());
            }

            @Override
            public void onFailure(Throwable throwable) {
                asyncResponse.resume(throwable);
            }
        });

        priceService.get(result, id);
    }
}
