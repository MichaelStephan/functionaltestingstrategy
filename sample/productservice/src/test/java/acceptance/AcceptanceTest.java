package acceptance;

import acceptance.expectations.AcceptanceExpectations_v0;
import acceptance.expectations.AcceptanceExpectations_v1;
import api.API;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dao.ProductDao;
import dao.impl.CassandraProductDao;
import dao.impl.NonProductionCassandraSessionProvider;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.Server;
import service.impl.ProductServiceImpl;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;

/**
 * Created by i303874 on 3/20/15.
 */
public class AcceptanceTest {

    private final static int PORT = 9001;

    private final static List<InetSocketAddress> URLS = Arrays.asList(new InetSocketAddress[]{InetSocketAddress.createUnresolved("127.0.0.1", 9142)});

    private final static String KEYSPACE = "test";

    private String getGetUrl() {
        return "http://localhost:" + PORT + "/productservice/products/";
    }

    private String getPostUrl() {
        return "http://localhost:" + PORT + "/productservice/products/";
    }

    private ProductDao dao;

    @Before
    public void setup() throws Exception {
        EmbeddedCassandraServerHelper.startEmbeddedCassandra("cassandra.yaml");
        this.dao = new CassandraProductDao(new NonProductionCassandraSessionProvider(URLS), KEYSPACE);
    }

    @After
    public void cleanup() throws Exception {
        EmbeddedCassandraServerHelper.cleanEmbeddedCassandra();
    }

    private ResourceConfig createProductionLikeResourceConfig() {
        ResourceConfig resourceConfig = new ResourceConfig();
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(om);
        resourceConfig.register(provider);
        resourceConfig.register(new API(new ProductServiceImpl(dao)));
        return resourceConfig;
    }

    @Test
    public void givenValidInputToGetThenReturnsProduct() throws Exception {
        new Server().run(PORT, () -> {
            new AcceptanceExpectations_v0(getPostUrl(), getGetUrl()).givenValidInputToGetThenReturnsProduct();
            new AcceptanceExpectations_v1(getPostUrl(), getGetUrl()).givenValidInputToGetThenReturnsProduct();
        }, createProductionLikeResourceConfig()).join();
    }
}
