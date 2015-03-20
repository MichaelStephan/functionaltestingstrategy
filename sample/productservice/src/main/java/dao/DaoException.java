package dao;

/**
 * Created by i303874 on 3/18/15.
 */
public class DaoException extends RuntimeException {
    public DaoException(String message) {
        super(message);
    }

    public DaoException(Throwable e) {
        super(e);
    }
}
