package exceptions;

/**
 * Thrown when an operation refers to an entity that does not exist
 * (for example removing a user that is not in the network).
 */
public class EntityNotFoundException extends RuntimeException {

    /**
     * @param message the description of the missing entity
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
