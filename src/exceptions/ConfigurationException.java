package exceptions;

/**
 * Thrown when the application configuration (config.properties) is missing,
 * cannot be read, or contains a missing or unknown value.
 */
public class ConfigurationException extends RuntimeException {

    /**
     * @param message the description of the problem
     */
    public ConfigurationException(String message) {
        super(message);
    }

    /**
     * @param message the description of the problem
     * @param cause   the original exception that caused this one
     */
    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
