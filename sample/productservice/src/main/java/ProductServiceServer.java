import api.API;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dao.CassandraSessionProvider;
import dao.impl.CassandraProductDao;
import dao.impl.NonProductionCassandraSessionProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;
import server.ServerException;
import service.impl.ProductServiceImpl;

import java.net.InetSocketAddress;
import java.util.Arrays;

/**
 * Created by i303874 on 3/18/15.
 */
public class ProductServiceServer {
    private final static Logger logger = LoggerFactory.getLogger(ProductServiceServer.class);

    private static ResourceConfig resourceConfig() {
        try {
            ResourceConfig resourceConfig = new ResourceConfig();

            ObjectMapper om = new ObjectMapper();
            om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
            provider.setMapper(om);
            resourceConfig.register(provider);

            CassandraSessionProvider cassandraSessionProvider = new NonProductionCassandraSessionProvider(Arrays.asList(new InetSocketAddress[]{InetSocketAddress.createUnresolved("127.0.0.1", 9042)}));

            resourceConfig.register(new API(new ProductServiceImpl(new CassandraProductDao(cassandraSessionProvider, "testABC"))));

            return resourceConfig;
        } catch (Exception e) {
            throw new ServerException(e);
        }
    }

    public static void main(String[] args) {
        try {
            new Server().run(Server.getPortFromEnv(), resourceConfig()).join();
        } catch (Exception e) {
            logger.error("running server failed", e);
        }
    }
}
