package repository.file;

import exceptions.RepositoryException;
import model.User;
import repository.IdGenerator;
import repository.UserRepository;
import validators.Validator;

/**
 * File repository for users.
 * Line format: {@code id;username;email;password}, for example {@code 1;ana;ana@mail.com;secret}.
 * The queries by username and email are inherited from {@link UserRepository};
 * new users receive their id from this repository when they are saved.
 */
public class UserFileRepository extends AbstractFileRepository<Long, User> implements UserRepository {

    private static final int FIELDS = 4;

    /**
     * Creates the repository and loads all users from the file.
     *
     * @param validator the validator applied to every user, including the loaded ones
     * @param fileName  the path of the users file
     * @throws RepositoryException if the file exists but cannot be read
     */
    public UserFileRepository(Validator<User> validator, String fileName) {
        super(validator, fileName);
    }

    /**
     * {@inheritDoc}
     * If the user has no id yet, a new one is assigned first (largest existing id + 1).
     */
    @Override
    public User save(User user) {
        if (user != null && user.getId() == null) {
            user.setId(IdGenerator.nextId(entities.values().stream().toList()));
        }
        return super.save(user);
    }

    /**
     * Parses a line of the form {@code id;username;email;password}.
     *
     * @param line a line from the file
     * @return the user described by the line, with its id set
     * @throws RepositoryException if the line has the wrong number of fields or a non-numeric id
     */
    @Override
    protected User extractEntity(String line) {
        String[] parts = splitLine(line, FIELDS);
        try {
            User user = new User(parts[1].trim(), parts[2].trim(), parts[3].trim());
            user.setId(Long.parseLong(parts[0].trim()));
            return user;
        } catch (NumberFormatException e) {
            throw new RepositoryException("invalid user id '" + parts[0] + "'", e);
        }
    }

    /**
     * Converts a user into a line of the form {@code id;username;email;password}.
     *
     * @param user the user
     * @return the line that describes the user
     */
    @Override
    protected String createEntityAsString(User user) {
        return user.getId() + SEPARATOR + user.getUsername() + SEPARATOR
                + user.getEmail() + SEPARATOR + user.getPassword();
    }
}
