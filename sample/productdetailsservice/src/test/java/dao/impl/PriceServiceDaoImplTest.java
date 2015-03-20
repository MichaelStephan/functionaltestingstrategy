package dao.impl;

import com.google.common.util.concurrent.SettableFuture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockserver.client.server.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import service.domain.APIPrice;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

/**
 * Created by i303874 on 3/20/15.
 */
public class PriceServiceDaoImplTest {
    private static final int PORT = 9999;

    private ClientAndServer mockServer;

    @Before
    public void setup() {
        mockServer = startClientAndServer(PORT);
    }

    @After
    public void cleanup() {
        mockServer.stop();
    }

    @Test
    public void test() throws Exception {
        String productId = "123";
        new MockServerClient("localhost", PORT).when(HttpRequest.request("/priceservice/products/" + productId + "/prices").withMethod("GET")).respond(HttpResponse.response().withStatusCode(500).withDelay(new Delay(TimeUnit.SECONDS, 2)));

        PriceServiceDaoImpl priceService = new PriceServiceDaoImpl("http://localhost:"+PORT+"/priceservice");
        priceService.get(SettableFuture.<List<APIPrice>>create(), productId).get(3, TimeUnit.SECONDS);
        System.out.println("bla bla");
    }
}
