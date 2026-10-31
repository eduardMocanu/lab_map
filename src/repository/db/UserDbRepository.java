package repository.db;

import model.User;
import repository.UserRepository;
import validators.Validator;

import java.util.List;
import java.util.Optional;

/**
 * Skeleton of the database repository for users.
 * It already receives everything it will need (validator and connection parameters),
 * so the factory can create it, but the JDBC code is written in Lab 4:
 * until then every operation throws {@link UnsupportedOperationException}.
 */
public class UserDbRepository implements UserRepository {

    private static final String NOT_IMPLEMENTED =
            "Database persistence is not implemented yet (Lab 4). Set repository.type to FILE or MEMORY.";

    private final Validator<User> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * @param validator the validator applied to every user before it is stored
     * @param url       the JDBC url of the database (db.url)
     * @param user      the database user (db.user)
     * @param password  the database password (db.password)
     */
    public UserDbRepository(Validator<User> validator, String url, String user, String password) {
        this.validator = validator;
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public Optional<User> findOne(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public List<User> findAll() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public User save(User entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public Optional<User> delete(Long id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public Optional<User> update(User entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
