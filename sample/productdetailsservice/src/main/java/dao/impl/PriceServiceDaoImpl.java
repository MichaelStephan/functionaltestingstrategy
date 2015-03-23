package dao.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.google.common.util.concurrent.SettableFuture;
import dao.DaoException;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import service.domain.APIPrice;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.Future;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/20/15.
 */
public class PriceServiceDaoImpl {
    private class PriceServiceGetResponse extends GenericType<List<APIPrice>> {
    }

    private String priceServiceUrl;

    private Client client;

    public PriceServiceDaoImpl(String priceServiceUrl) {
        this.priceServiceUrl = checkNotNull(priceServiceUrl);

        ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 1000);
        clientConfig.property(ClientProperties.READ_TIMEOUT, 1000);

        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(om);
        clientConfig.register(provider);

        this.client = ClientBuilder.newClient(clientConfig);
    }

    public Future<List<APIPrice>> getPrices(SettableFuture<List<APIPrice>> result, String productId) {
        client.target(priceServiceUrl).path("/products/" + productId + "/prices").request(MediaType.APPLICATION_JSON).async().get(new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                if (response.getStatus() == 200) {
                    result.set(response.readEntity(new PriceServiceGetResponse()));
                } else {
                    result.setException(new DaoException("invalid error code " + response.getStatus()));
                }
            }

            @Override
            public void failed(Throwable throwable) {
                result.setException(new DaoException(throwable));
            }
        });
        return result;
    }

    public Future<APIPrice> getPrice(SettableFuture<APIPrice> result, String productId) {
        client.target(priceServiceUrl).path("/products/" + productId + "/price").request(MediaType.APPLICATION_JSON).async().get(new InvocationCallback<Response>() {
            @Override
            public void completed(Response response) {
                if (response.getStatus() == 200) {
                    result.set(response.readEntity(APIPrice.class));
                } else {
                    result.setException(new DaoException("invalid error code " + response.getStatus()));
                }
            }

            @Override
            public void failed(Throwable throwable) {
                result.setException(new DaoException(throwable));
            }
        });
        return result;
    }
}
