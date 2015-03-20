package dao;

/**
 * Created by i303874 on 3/20/15.
 */
public class DaoException extends RuntimeException {
    public DaoException(String message) {
        super(message);
    }

    public DaoException(Throwable cause) {
        super(cause);
    }
}
