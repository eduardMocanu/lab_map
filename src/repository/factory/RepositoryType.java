package repository.factory;

/**
 * The kinds of persistence the application supports, selected with
 * {@code repository.type} in config.properties.
 */
public enum RepositoryType {
    /** Data is kept only in memory and is lost when the application stops. */
    MEMORY,
    /** Data is kept in text files in the data/ folder. */
    FILE,
    /** Data is kept in a relational database (implemented in Lab 4). */
    DATABASE
}
