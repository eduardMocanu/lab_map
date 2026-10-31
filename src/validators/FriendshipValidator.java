package validators;

import exceptions.ValidationException;
import model.Friendship;

/**
 * Validates the data of a friendship: both user ids must be present and positive,
 * and a user cannot be friends with themselves.
 * All problems are collected and reported together in one ValidationException.
 */
public class FriendshipValidator implements Validator<Friendship> {

    /**
     * Creates a validator for friendships.
     */
    public FriendshipValidator() {
    }

    /**
     * Checks that the friendship's user ids are valid.
     *
     * @param friendship the friendship to check
     * @throws ValidationException if the friendship is null or its ids are invalid
     */
    @Override
    public void validate(Friendship friendship) {
        if (friendship == null) {
            throw new ValidationException("Friendship cannot be null.");
        }

        Long id1 = friendship.getUserId1();
        Long id2 = friendship.getUserId2();
        StringBuilder errors = new StringBuilder();

        validateId(id1, "First user id", errors);
        validateId(id2, "Second user id", errors);

        if (id1 != null && id2 != null && id1.equals(id2)) {
            errors.append("A user cannot be friends with themselves.\n");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors.toString().trim());
        }
    }

    /**
     * Checks that a user id is present and positive.
     *
     * @param id     the id to check
     * @param label  the name of the id, used in the error message
     * @param errors where the error messages are added
     */
    private void validateId(Long id, String label, StringBuilder errors) {
        if (id == null) {
            errors.append(label).append(" cannot be null.\n");
        } else if (id <= 0) {
            errors.append(label).append(" must be a positive number.\n");
        }
    }
}