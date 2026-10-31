package exceptions;

/**
 * Thrown when an entity has invalid data (for example an empty username or a self-friendship).
 */
public class ValidationException extends RuntimeException {

    /**
     * @param message the description of every problem found
     */
    public ValidationException(String message) {
        super(message);
    }
}
