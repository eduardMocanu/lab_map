package repository.db;

import model.Entity;
import repository.PagingRepository;
import repository.Repository;
import validators.Validator;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Base class for repositories that store entities in a relational database through JDBC.
 * It holds what every database repository needs (the validator and the connection parameters)
 * and opens connections with {@link DriverManager}; subclasses only write the SQL for their table.
 *
 * @param <ID> the type of the entity id
 * @param <E>  the type of the entity
 */
public abstract class AbstractDbRepository<ID, E extends Entity<ID>> implements Repository<ID, E> {

    /** The validator applied to every entity before it is saved or updated. */
    protected final Validator<E> validator;

    private final String url;
    private final String user;
    private final String password;

    /**
     * @param validator the validator applied to every entity before it is saved or updated
     * @param url       the JDBC url of the database (db.url)
     * @param user      the database user (db.user)
     * @param password  the database password (db.password)
     */
    protected AbstractDbRepository(Validator<E> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Opens a new connection to the database; the caller closes it (try-with-resources).
     *
     * @return a new connection
     * @throws SQLException if the database cannot be reached
     */
    protected Connection connect() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Checks that an argument is not null, as the Repository contract requires.
     *
     * @param value the argument to check
     * @param name  the name of the argument, used in the message
     * @throws IllegalArgumentException if {@code value} is null
     */
    protected static void requireNonNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null.");
        }
    }
}