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

import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertTrue;
import static org.mockserver.integration.ClientAndServer.startClientAndServer;

/**
 * Created by i303874 on 3/20/15.
 */
public class PriceServiceDaoImplTechnicalTest {
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

    private PriceServiceDaoImpl createPriceService() {
        return new PriceServiceDaoImpl("http://localhost:" + PORT + "/priceservice");
    }

    @Test
    public void givenProductIdAsArgumentToGetPricesWhenRemoteServerResponseIsSlowThenThrowTimeoutException() throws
            Exception {
        try {
            String productId = "123";
            new MockServerClient("localhost", PORT).when(HttpRequest.request("/priceservice/products/" + productId + "/prices").withMethod("GET")).respond(HttpResponse.response().withStatusCode(500).withDelay(new Delay(TimeUnit.SECONDS, 2)));

            createPriceService().getPrices(SettableFuture.<List<APIPrice>>create(), productId).get(3, TimeUnit.SECONDS);
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause().getCause().getCause() instanceof java.net.SocketTimeoutException);
        }
    }
}
