package dao.impl;

import au.com.dius.pact.consumer.ConsumerPactBuilder;
import au.com.dius.pact.consumer.ConsumerPactTest;
import au.com.dius.pact.consumer.PactDslJsonBody;
import au.com.dius.pact.model.PactFragment;
import com.google.common.util.concurrent.SettableFuture;
import service.domain.APIPrice;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static junit.framework.TestCase.fail;

/**
 * Created by i303874 on 3/23/15.
 */
public class GivenProductIdAsArgumentToGetPricesThenReturnProductPriceTest extends ConsumerPactTest {
    private UUID expectedId = UUID.randomUUID();
    private String expectedCurrency = "USD";
    String productId = "123";

    private PriceServiceDaoImpl createPriceService(String url) {
        return new PriceServiceDaoImpl(url + "/priceservice");
    }

    @Override
    protected PactFragment createFragment(ConsumerPactBuilder.PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");

        return builder.uponReceiving("a request for price")
                .path("/priceservice/products/" + productId + "/price")
                .method("GET")
                .willRespondWith()
                .headers(headers)
                .status(200)
                .body(new PactDslJsonBody().guid("id", expectedId).stringMatcher("currency", "[A-Z]{3}", expectedCurrency).numberType("value", 99.90).asBody()).toFragment();
    }

    @Override
    protected String providerName() {
        return "priceservice";
    }

    @Override
    protected String consumerName() {
        return "productdetailsservice";
    }

    @Override
    protected void runTest(String url) {
        try {
            APIPrice price = createPriceService(url).getPrice(SettableFuture.<APIPrice>create(), productId).get(3, TimeUnit.SECONDS);
            assertEquals(expectedId.toString(), price.getId().toString());
            assertEquals(expectedCurrency, price.getCurrency());
            assertTrue(Math.abs(new BigDecimal(99.90).doubleValue() - price.getValue().doubleValue()) < 0.1);
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
