package dao;

import com.datastax.driver.core.Session;

/**
 * Created by i303874 on 3/19/15.
 */
public interface CassandraSessionProvider {
    Session getSession();
}
