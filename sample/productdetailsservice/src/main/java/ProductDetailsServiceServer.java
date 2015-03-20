import api.API;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import dao.ProductDetailsContentDao;
import dao.impl.PriceServiceDaoImpl;
import dao.impl.ProductDetailsContentDaoImpl;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.Server;
import server.ServerException;
import service.impl.ProductDetailsServiceImpl;

/**
 * Created by i303874 on 3/18/15.
 */
public class ProductDetailsServiceServer {
    private final static Logger logger = LoggerFactory.getLogger(ProductDetailsServiceServer.class);

    private static ResourceConfig resourceConfig() {
        try {
            ResourceConfig resourceConfig = new ResourceConfig();

            ObjectMapper om = new ObjectMapper();
            om.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            om.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
            JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
            provider.setMapper(om);
            resourceConfig.register(provider);

            ProductDetailsContentDao productDetailsContentDao = new ProductDetailsContentDaoImpl(new PriceServiceDaoImpl("http://localhost:10001/priceservice"));

            resourceConfig.register(new API(new ProductDetailsServiceImpl(productDetailsContentDao)));

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
