package repository.db;

import exceptions.RepositoryException;
import model.Friendship;
import model.Tuple;
import model.User;
import repository.FriendshipRepository;
import util.paging.Page;
import util.paging.Pageable;
import validators.Validator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository for friendships stored in the PostgreSQL table {@code friendships}.
 * A friendship is identified by the pair (id1, id2) of user ids, always with id1 &lt; id2;
 * the database does not generate it. Every operation opens its own connection; connections,
 * statements and result sets are closed with try-with-resources, and all values are passed as
 * PreparedStatement parameters.
 */
public class FriendshipDbRepository extends AbstractDbRepository<Tuple<Long, Long>, Friendship>
        implements FriendshipRepository {

    private static final String COLUMNS = "id1, id2, friends_from";

    /**
     * @param validator the validator applied to every friendship before it is saved or updated
     * @param url       the JDBC url of the database (db.url)
     * @param user      the database user (db.user)
     * @param password  the database password (db.password)
     */
    public FriendshipDbRepository(Validator<Friendship> validator, String url, String user, String password) {
        super(validator, url, user, password);
    }

    /**
     * Builds a friendship from the current row of a result set.
     *
     * @param rs a result set positioned on a row of the friendships table
     * @return the friendship described by the row
     * @throws SQLException if a column cannot be read
     */
    private Friendship extractFriendship(ResultSet rs) throws SQLException {
        return new Friendship(rs.getLong("id1"), rs.getLong("id2"),
                rs.getTimestamp("friends_from").toLocalDateTime());
    }

    /**
     * Checks that a friendship id and both of its user ids are present.
     *
     * @param id the friendship id
     * @throws IllegalArgumentException if the id or one of its user ids is null
     */
    private static void requireValidId(Tuple<Long, Long> id) {
        requireNonNull(id, "Id");
        requireNonNull(id.getFirst(), "First user id");
        requireNonNull(id.getSecond(), "Second user id");
    }

    /**
     * {@inheritDoc}
     *
     * @throws RepositoryException if the database cannot be read
     */
    @Override
    public Optional<Friendship> findOne(Tuple<Long, Long> id) {
        requireValidId(id);
        String sql = "SELECT * FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id.getFirst());
            statement.setLong(2, id.getSecond());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(extractFriendship(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw SqlErrors.wrap("find the friendship", e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RepositoryException if the database cannot be read
     */
    @Override
    public List<Friendship> findAll() {
        String sql = "SELECT * FROM friendships ORDER BY id1, id2";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            List<Friendship> friendships = new ArrayList<>();
            while (rs.next()) {
                friendships.add(extractFriendship(rs));
            }
            return friendships;
        } catch (SQLException e) {
            throw SqlErrors.wrap("read the friendships", e);
        }
    }

    /**
     * Validates the friendship and inserts it.
     *
     * @param entity the friendship to save
     * @return the saved friendship
     * @throws IllegalArgumentException       if {@code entity} is null
     * @throws exceptions.ValidationException if the friendship is not valid
     * @throws RepositoryException            if it cannot be saved (already friends, or a user does not exist)
     */
    @Override
    public Friendship save(Friendship entity) {
        requireNonNull(entity, "Entity");
        validator.validate(entity);
        String sql = "INSERT INTO friendships (id1, id2, friends_from) VALUES (?, ?, ?)";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, entity.getUserId1());
            statement.setLong(2, entity.getUserId2());
            statement.setTimestamp(3, Timestamp.valueOf(entity.getFriendsFrom()));
            statement.executeUpdate();
            return entity;
        } catch (SQLException e) {
            throw SqlErrors.wrap("save the friendship", e);
        }
    }

    /**
     * {@inheritDoc}
     * The friendship is read first, so it can be returned after it is deleted.
     *
     * @throws RepositoryException if the friendship cannot be deleted
     */
    @Override
    public Optional<Friendship> delete(Tuple<Long, Long> id) {
        requireValidId(id);
        Optional<Friendship> existing = findOne(id);
        if (existing.isEmpty()) {
            return existing;
        }
        String sql = "DELETE FROM friendships WHERE id1 = ? AND id2 = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id.getFirst());
            statement.setLong(2, id.getSecond());
            statement.executeUpdate();
            return existing;
        } catch (SQLException e) {
            throw SqlErrors.wrap("delete the friendship", e);
        }
    }

    /**
     * {@inheritDoc}
     * Only the start moment can change, because the two user ids are the friendship's identity.
     * Whether the friendship exists is decided by the number of rows changed by executeUpdate
     * (0 = not found), without a SELECT first.
     *
     * @throws RepositoryException if the friendship cannot be updated
     */
    @Override
    public Optional<Friendship> update(Friendship entity) {
        requireNonNull(entity, "Entity");
        validator.validate(entity);
        String sql = "UPDATE friendships SET friends_from = ? WHERE id1 = ? AND id2 = ?";
        try (Connection connection = connect();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setTimestamp(1, Timestamp.valueOf(entity.getFriendsFrom()));
            statement.setLong(2, entity.getUserId1());
            statement.setLong(3, entity.getUserId2());
            int changedRows = statement.executeUpdate();
            return changedRows == 0 ? Optional.of(entity) : Optional.empty();
        } catch (SQLException e) {
            throw SqlErrors.wrap("update the friendship", e);
        }
    }

    /**
     * Returns one page of the friends of a user, ordered by id.
     * A single SELECT joins friendships with users and is paged in SQL; for each friendship of the
     * user, the friend is the other user (CASE). The total number of friends is read on the same connection.
     *
     * @param userId   the id of the user
     * @param pageable which page to return and how many friends per page
     * @return the friends on that page and the user's total number of friends; empty after the last page
     * @throws IllegalArgumentException if an argument is null
     * @throws RepositoryException      if the database cannot be read
     */
    @Override
    public Page<User> findFriendsOnPage(Long userId, Pageable pageable) {
        requireNonNull(userId, "User id");
        requireNonNull(pageable, "Pageable");
        String pageSql = "SELECT u.id, u.username, u.email, u.password "
                + "FROM friendships f "
                + "JOIN users u ON u.id = CASE WHEN f.id1 = ? THEN f.id2 ELSE f.id1 END "
                + "WHERE f.id1 = ? OR f.id2 = ? "
                + "ORDER BY u.id LIMIT ? OFFSET ?";
        String countSql = "SELECT count(*) FROM friendships WHERE id1 = ? OR id2 = ?";
        try (Connection connection = connect();
             PreparedStatement pageStatement = connection.prepareStatement(pageSql);
             PreparedStatement countStatement = connection.prepareStatement(countSql)) {

            pageStatement.setLong(1, userId);
            pageStatement.setLong(2, userId);
            pageStatement.setLong(3, userId);
            pageStatement.setInt(4, pageable.pageSize());
            pageStatement.setInt(5, pageable.pageNumber() * pageable.pageSize());
            List<User> friends = new ArrayList<>();
            try (ResultSet rs = pageStatement.executeQuery()) {
                while (rs.next()) {
                    friends.add(UserDbRepository.extractUser(rs));
                }
            }

            countStatement.setLong(1, userId);
            countStatement.setLong(2, userId);
            try (ResultSet rs = countStatement.executeQuery()) {
                rs.next();
                return new Page<>(friends, rs.getInt(1));
            }
        } catch (SQLException e) {
            throw SqlErrors.wrap("read a page of friends", e);
        }
    }
}