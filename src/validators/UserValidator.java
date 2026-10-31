package validators;

import exceptions.ValidationException;
import model.User;

/**
 * Validates the data of a user: username, email and password.
 * All problems are collected and reported together in one ValidationException.
 * The character ";" is rejected in every field because users are stored as lines with fields separated by ";".
 */
public class UserValidator implements Validator<User> {

    /** The maximum number of characters allowed in a username. */
    private static final int MAX_USERNAME_LENGTH = 20;

    /** The minimum number of characters required in a password. */
    private static final int MIN_PASSWORD_LENGTH = 4;

    /**
     * Creates a validator for users.
     */
    public UserValidator() {
    }

    /**
     * Checks that the user's fields are valid.
     *
     * @param user the user to check
     * @throws ValidationException if the user is null or any field is invalid
     */
    @Override
    public void validate(User user) {
        if (user == null) {
            throw new ValidationException("User cannot be null.");
        }

        StringBuilder errors = new StringBuilder();
        validateUsername(user.getUsername(), errors);
        validateEmail(user.getEmail(), errors);
        validatePassword(user.getPassword(), errors);

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }

    /**
     * Checks that the username is not empty, not too long and contains no ";".
     *
     * @param username the username to check
     * @param errors   where the error messages are added
     */
    private void validateUsername(String username, StringBuilder errors) {
        if (isEmpty(username)) {
            errors.append("Username cannot be empty.\n");
        } else {
            if (username.length() > MAX_USERNAME_LENGTH)
                errors.append("Username cannot have more than " + MAX_USERNAME_LENGTH + " characters.\n");
            if (username.contains(";"))
                errors.append("Username cannot contain ';'.\n");
        }
    }

    /**
     * Checks that the email is not empty, contains '@' and contains no ";".
     *
     * @param email  the email to check
     * @param errors where the error messages are added
     */
    private void validateEmail(String email, StringBuilder errors) {
        if (isEmpty(email)) {
            errors.append("Email cannot be empty.\n");
        } else {
            if (!email.contains("@"))
                errors.append("Email must contain '@'.\n");
            if (email.contains(";"))
                errors.append("Email cannot contain ';'.\n");
        }
    }

    /**
     * Checks that the password is not empty, long enough and contains no ";".
     *
     * @param password the password to check
     * @param errors   where the error messages are added
     */
    private void validatePassword(String password, StringBuilder errors) {
        if (isEmpty(password)) {
            errors.append("Password cannot be empty.\n");
        } else {
            if (password.length() < MIN_PASSWORD_LENGTH)
                errors.append("Password must have at least " + MIN_PASSWORD_LENGTH + " characters.\n");
            if (password.contains(";"))
                errors.append("Password cannot contain ';'.\n");
        }
    }

    /**
     * @param value the text to check
     * @return true if the text is null or contains only whitespace
     */
    private boolean isEmpty(String value) {
        return value == null || value.isBlank();
    }
}