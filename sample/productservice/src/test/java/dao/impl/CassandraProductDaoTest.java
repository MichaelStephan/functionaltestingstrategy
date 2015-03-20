package dao.impl;

import com.google.common.util.concurrent.SettableFuture;
import dao.ProductDao;
import org.cassandraunit.utils.EmbeddedCassandraServerHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import service.domain.Product;
import service.domain.ProductId;

import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

/**
 * Created by i303874 on 3/18/15.
 */
public class CassandraProductDaoTest {
    private final static List<InetSocketAddress> URLS = Arrays.asList(new InetSocketAddress[]{InetSocketAddress.createUnresolved("127.0.0.1", 9142)});

    private final static String KEYSPACE = "test";

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

    private Product createNewProduct() {
        return new Product(null, "someName", "someDescription");
    }

    @Test
    public void givenProductWithoutIdAsArgumentToSaveThenReturnsTheSameProductWithNewId() throws Exception {
        Product expectedProduct = createNewProduct();
        Product actualProduct = dao.save(SettableFuture.create(), expectedProduct).get(3, TimeUnit.SECONDS);
        assertTrue(actualProduct.getId().getValue() != null);
        assertEquals(expectedProduct.getName(), actualProduct.getName());
        assertEquals(expectedProduct.getDescription(), actualProduct.getDescription());
    }

    @Test
    public void givenExistingProductIdAsArgumentToGetThenReturnsProduct() throws Exception {
        Product product = dao.save(SettableFuture.create(), createNewProduct()).get(3, TimeUnit.SECONDS);
        Product expectedProduct = createNewProduct();
        Product actualProduct = dao.get(SettableFuture.<List<Product>>create(), product.getId()).get(3, TimeUnit.SECONDS).stream().findFirst().get();
        assertTrue(actualProduct.getId().getValue() != null);
        assertEquals(expectedProduct.getName(), actualProduct.getName());
        assertEquals(expectedProduct.getDescription(), actualProduct.getDescription());
    }

    @Test(expected = NullPointerException.class)
    public void givenNullProductAsArgumentToGetThenRaiseException() throws Exception {
        dao.save(SettableFuture.create(), null).get(3, TimeUnit.SECONDS);
    }

    @Test(expected = NullPointerException.class)
    public void givenNullFutureAsArgumentToGetThenRaiseException() throws Exception {
        dao.save(null, createNewProduct()).get(3, TimeUnit.SECONDS);
    }

    @Test
    public void givenNonExistingProductIdAsArgumentToGetThenReturnsEmptyList() throws Exception {
        assertTrue(dao.get(SettableFuture.<List<Product>>create(), new ProductId(UUID.randomUUID())).get(3, TimeUnit.SECONDS).isEmpty());
    }
}

//    @Test(expected = DaoException.class)
//    public void givenValidInputToSaveWhenCassandraExceptionThenRaiseException() throws Exception {
//        ProductDao dao = new CassandraProductDao(() -> {
//            Session session = Mockito.mock(Session.class);
//            Mockito.when(session.executeAsync((Statement) Mockito.notNull())).thenReturn(new ResultSetFuture() {
//                @Override
//                public void addListener(Runnable runnable, Executor executor) {
//
//                }
//
//                @Override
//                public ResultSet getUninterruptibly() {
//                    return null;
//                }
//
//                @Override
//                public ResultSet getUninterruptibly(long l, TimeUnit timeUnit) throws TimeoutException {
//                    return null;
//                }
//
//                @Override
//                public boolean cancel(boolean b) {
//                    return false;
//                }
//
//                @Override
//                public boolean isCancelled() {
//                    return false;
//                }
//
//                @Override
//                public boolean isDone() {
//                    return true;
//                }
//
//                @Override
//                public ResultSet get() throws InterruptedException, ExecutionException {
//                    throw new RuntimeException();
//                }
//
//                @Override
//                public ResultSet get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
//                    throw new RuntimeException();
//                }
//            });
//            return session;
//        }, KEYSPACE);
//
//        ResultSetFuture x;
//
//        dao.save(SettableFuture.create(), createNewProduct()).get(3, TimeUnit.SECONDS);
//    }
//}
