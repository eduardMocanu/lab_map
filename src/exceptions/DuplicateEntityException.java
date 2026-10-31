package exceptions;

/**
 * Thrown when an entity conflicts with an existing one
 * (for example a username that is already taken, or two users who are already friends).
 */
public class DuplicateEntityException extends RuntimeException {

    /**
     * @param message the description of the conflict
     */
    public DuplicateEntityException(String message) {
        super(message);
    }
}
