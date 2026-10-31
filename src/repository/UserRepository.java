package repository;

import criteria.UserCriteria;
import model.User;

import java.util.Optional;

/**
 * Repository for users, adding queries by username and by email.
 * The queries have default implementations that search through {@link #findAll()},
 * so every implementation (memory, file) gets them without duplicating code;
 * a database implementation can override them with a SQL query.
 */
public interface UserRepository extends Repository<Long, User> {

    /**
     * Finds the user with the given username.
     *
     * @param username the username to search for
     * @return the user with this username, or an empty Optional if there is none (also for {@code null})
     */
    default Optional<User> findByUsername(String username) {
        return findAll(UserCriteria.hasUsername(username)).stream().findFirst();
    }

    /**
     * Finds the user with the given email address.
     *
     * @param email the email address to search for
     * @return the user with this email, or an empty Optional if there is none (also for {@code null})
     */
    default Optional<User> findByEmail(String email) {
        return findAll(UserCriteria.hasEmail(email)).stream().findFirst();
    }
}
