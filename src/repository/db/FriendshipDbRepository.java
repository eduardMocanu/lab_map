package repository.db;

import model.Friendship;
import model.Tuple;
import repository.FriendshipRepository;
import validators.Validator;

import java.util.List;
import java.util.Optional;

/**
 * Skeleton of the database repository for friendships.
 * It already receives everything it will need (validator and connection parameters),
 * so the factory can create it, but the JDBC code is written in Lab 4:
 * until then every operation throws {@link UnsupportedOperationException}.
 */
public class FriendshipDbRepository implements FriendshipRepository {

    private static final String NOT_IMPLEMENTED =
            "Database persistence is not implemented yet (Lab 5). Set repository.type to FILE or MEMORY.";

    private final Validator<Friendship> validator;
    private final String url;
    private final String user;
    private final String password;

    /**
     * @param validator the validator applied to every friendship before it is stored
     * @param url       the JDBC url of the database (db.url)
     * @param user      the database user (db.user)
     * @param password  the database password (db.password)
     */
    public FriendshipDbRepository(Validator<Friendship> validator, String url, String user, String password) {
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
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public List<Friendship> findAll() {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public Friendship save(Friendship entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }

    /**
     * {@inheritDoc}
     *
     * @throws UnsupportedOperationException always, until Lab 4
     */
    @Override
    public Optional<Friendship> update(Friendship entity) {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED);
    }
}
