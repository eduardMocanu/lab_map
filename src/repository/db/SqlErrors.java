package repository.db;

import exceptions.RepositoryException;

import java.sql.SQLException;

/**
 * Translates SQL errors into {@link RepositoryException}s with messages the user can understand.
 * The message is chosen from the standard SQL state of the error; the original SQLException
 * is kept as the cause.
 */
final class SqlErrors {

    private SqlErrors() {
    }

    /**
     * Wraps an SQLException in a RepositoryException with a specific message.
     *
     * @param action what the repository was doing, e.g. "save the user"
     * @param e      the SQL error
     * @return a RepositoryException with a specific message and {@code e} as cause
     */
    static RepositoryException wrap(String action, SQLException e) {
        String state = e.getSQLState() == null ? "" : e.getSQLState();
        String message;
        if (state.startsWith("08")) {
            message = "The database server is not reachable. Check that PostgreSQL is running and db.url is correct.";
        } else if (state.equals("28P01")) {
            message = "The database rejected the user or password (check db.user and db.password).";
        } else if (state.equals("3D000")) {
            message = "The database in db.url does not exist.";
        } else if (state.equals("42P01")) {
            message = "A table is missing in the database. Run schema.sql first.";
        } else if (state.equals("23505")) {
            message = "Could not " + action + ": this entry already exists.";
        } else if (state.equals("23503")) {
            message = "Could not " + action + ": it refers to a user that does not exist.";
        } else if (state.equals("23514")) {
            message = "Could not " + action + ": the data breaks a database rule.";
        } else {
            message = "Database error while trying to " + action + ".";
        }
        return new RepositoryException(message, e);
    }
}