package dao.impl;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.exceptions.AlreadyExistsException;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.SettableFuture;
import dao.CassandraSessionProvider;
import dao.DaoException;
import dao.ProductDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import service.domain.Product;
import service.domain.ProductId;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/18/15.
 */
public class CassandraProductDao implements ProductDao {
    private final static Logger logger = LoggerFactory.getLogger(CassandraProductDao.class);

    private final static String TABLE = "products";

    private String keyspace;

    private CassandraSessionProvider cassandraSessionProvider;

    public CassandraProductDao(CassandraSessionProvider cassandraSessionProvider, String keyspace) {
        this.keyspace = checkNotNull(keyspace);
        this.cassandraSessionProvider = checkNotNull(cassandraSessionProvider);
        createStructuresIfNotExisting(keyspace);
    }

    void createKeyspace(String keyspace) {
        try {
            cassandraSessionProvider.getSession().execute("CREATE KEYSPACE " + keyspace + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor' : 3 }");
            logger.info("creating keyspace " + keyspace + " succeeded");
            createProductsTable(keyspace);
        } catch (Exception e) {
            if (e instanceof AlreadyExistsException) {
                logger.info("keyspace " + keyspace + " already exists");
                // TODO implement keyspace structure analysis to avoid accidental collisions
                createProductsTable(keyspace);
            } else {
                logger.error("creating keyspace " + keyspace + " failed", e);
            }
        }
    }

    void createProductsTable(String keyspace) {
        try {
            cassandraSessionProvider.getSession().execute("CREATE TABLE " + keyspace + "." + TABLE + "(id uuid PRIMARY KEY, name text, description text)");
            logger.info("creating table " + keyspace + "." + TABLE + " succeeded");
        } catch (Exception e) {
            if (e instanceof AlreadyExistsException) {
                logger.info(keyspace + "." + TABLE + " already exists");
                // TODO implement table structure analysis to avoid accidental collisions
            } else {
                logger.error("creating table " + keyspace + "." + TABLE + " failed", e);
            }
        }
    }

    private void createStructuresIfNotExisting(String keyspace) {
        checkNotNull(keyspace);

        createKeyspace(keyspace);
    }

    @Override
    public Future<Product> save(SettableFuture<Product> result, Product product) {
        checkNotNull(result);
        checkNotNull(product);

        UUID id = (product.getId() == null || product.getId().getValue() == null) ? UUID.randomUUID() : product.getId().getValue();
        Futures.addCallback(cassandraSessionProvider.getSession().executeAsync(QueryBuilder.insertInto(keyspace, TABLE).values(new String[]{"id", "name", "description"}, new Object[]{id, product.getName(), product.getDescription()}).disableTracing()), new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet rows) {
                try {
                    result.set(new Product(new ProductId(id), product.getName(), product.getDescription()));
                } catch (Exception e) {
                    result.setException(new DaoException(e));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                result.setException(new DaoException(throwable));
            }
        });
        return result;
    }


    @Override
    public Future<List<Product>> get(SettableFuture<List<Product>> result, ProductId id) {
        checkNotNull(result);
        checkNotNull(id);
        checkNotNull(id.getValue());

        Futures.addCallback(cassandraSessionProvider.getSession().executeAsync(QueryBuilder.select().from(keyspace, TABLE).where(eq("id", id.getValue()))), new FutureCallback<ResultSet>() {
            @Override
            public void onSuccess(ResultSet rows) {
                try {
                    Row row = rows.one();
                    if (row == null) {
                        result.set(Collections.<Product>emptyList());
                    } else {
                        result.set(Arrays.asList(new Product[]{new Product(new ProductId(row.getUUID("id")), row.getString("name"), row.getString("description"))}));
                    }
                } catch (Exception e) {
                    result.setException(new DaoException(e));
                }
            }

            @Override
            public void onFailure(Throwable throwable) {
                result.setException(new DaoException(throwable));
            }
        });
        return result;
    }
}
