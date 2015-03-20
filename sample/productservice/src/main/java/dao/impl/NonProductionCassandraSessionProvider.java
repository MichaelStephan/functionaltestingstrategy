package dao.impl;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PoolingOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import dao.CassandraSessionProvider;
import dao.DaoException;

import java.net.InetSocketAddress;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by i303874 on 3/19/15.
 */
public class NonProductionCassandraSessionProvider implements CassandraSessionProvider {
    private Session session;

    public NonProductionCassandraSessionProvider(List<InetSocketAddress> urls) {
        checkNotNull(urls);

        session = initializeCassandraConnection(urls);
    }

    public Session getSession() {
        if (session == null) {
            throw new IllegalStateException();
        }
        return session;
    }

    private Session initializeCassandraConnection(List<InetSocketAddress> urls) {
        checkNotNull(urls);
        checkArgument(!urls.isEmpty());

        if (urls.size() > 1) {
            throw new DaoException("the current datastax driver has some driver with multiple hosts/ ports");
        }

        try {
            InetSocketAddress url = urls.stream().findFirst().get();

            Cluster cluster = new Cluster.Builder()
                    .addContactPointsWithPorts(urls)
                    .withPoolingOptions(new PoolingOptions())
                    .withSocketOptions(new SocketOptions().setTcpNoDelay(true)).addContactPoint(url.getHostName()).withPort(url.getPort()).build();

            return cluster.connect();
        } catch (Exception e) {
            throw new DaoException(e);
        }
    }
}
