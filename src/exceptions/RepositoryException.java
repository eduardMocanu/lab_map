package exceptions;

/**
 * Thrown when the data cannot be read from or written to the storage
 * (for example a file that cannot be opened, or a line with an invalid format).
 * The original low-level exception (IOException, NumberFormatException, DateTimeParseException, ...)
 * is kept as the cause, so no information is lost (exception chaining).
 */
public class RepositoryException extends RuntimeException {

    /**
     * @param message the description of the problem
     */
    public RepositoryException(String message) {
        super(message);
    }

    /**
     * @param message the description of the problem
     * @param cause   the original exception that caused this one
     */
    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
