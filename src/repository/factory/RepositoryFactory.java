package repository.factory;

import exceptions.ConfigurationException;
import model.Friendship;
import model.User;
import repository.FriendshipRepository;
import repository.UserRepository;
import repository.db.FriendshipDbRepository;
import repository.db.UserDbRepository;
import repository.file.FriendshipFileRepository;
import repository.file.UserFileRepository;
import repository.memory.InMemoryFriendshipRepository;
import repository.memory.InMemoryUserRepository;
import validators.Validator;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Creates the repositories of the application (Simple Factory pattern).
 * <p>
 * This is the only class that knows the concrete repository classes. The kind of persistence
 * ({@link RepositoryType}) and its parameters are read from a properties file, so switching
 * between memory, file and database storage only means changing {@code repository.type};
 * services, UI and Main do not change. Both repositories are always created for the same type.
 * <p>
 * Keys read from the configuration:
 * <ul>
 *     <li>{@code repository.type} = MEMORY | FILE | DATABASE (always required)</li>
 *     <li>{@code file.users}, {@code file.friendships} (required for FILE)</li>
 *     <li>{@code db.url}, {@code db.user}, {@code db.password} (required for DATABASE)</li>
 * </ul>
 */
public class RepositoryFactory {

    /** The default location of the configuration file, relative to the project folder. */
    public static final String DEFAULT_CONFIG_FILE = "src/main/resources/config.properties";

    private final Properties properties = new Properties();
    private final RepositoryType type;

    /**
     * Loads the configuration from the given properties file and reads the repository type.
     *
     * @param configFile the path of the properties file
     * @throws ConfigurationException if the file cannot be read, or {@code repository.type}
     *                                is missing or unknown
     */
    public RepositoryFactory(String configFile) {
        Path path = Path.of(configFile);
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            properties.load(reader);
        } catch (NoSuchFileException e) {
            throw new ConfigurationException("Configuration file not found: " + path, e);
        } catch (IOException e) {
            throw new ConfigurationException("Could not read configuration file " + path + ".", e);
        }
        this.type = readType();
    }

    /**
     * Loads the configuration from {@link #DEFAULT_CONFIG_FILE}.
     *
     * @throws ConfigurationException if the file cannot be read, or {@code repository.type}
     *                                is missing or unknown
     */
    public RepositoryFactory() {
        this(DEFAULT_CONFIG_FILE);
    }

    /**
     * @return the kind of persistence selected in the configuration
     */
    public RepositoryType getType() {
        return type;
    }

    /**
     * Creates the user repository for the configured type of persistence.
     *
     * @param validator the validator the repository applies to every user
     * @return the user repository
     * @throws ConfigurationException         if a parameter required by the configured type is missing
     * @throws exceptions.RepositoryException if the users file exists but cannot be read
     */
    public UserRepository createUserRepository(Validator<User> validator) {
        switch (type) {
            case MEMORY:
                return new InMemoryUserRepository(validator);
            case FILE:
                return new UserFileRepository(validator, getRequired("file.users"));
            case DATABASE:
                return new UserDbRepository(validator,
                        getRequired("db.url"), getRequired("db.user"), getRequired("db.password"));
            default:
                throw new ConfigurationException("Unsupported repository type: " + type);
        }
    }

    /**
     * Creates the friendship repository for the configured type of persistence.
     *
     * @param validator the validator the repository applies to every friendship
     * @return the friendship repository
     * @throws ConfigurationException         if a parameter required by the configured type is missing
     * @throws exceptions.RepositoryException if the friendships file exists but cannot be read
     */
    public FriendshipRepository createFriendshipRepository(Validator<Friendship> validator) {
        switch (type) {
            case MEMORY:
                return new InMemoryFriendshipRepository(validator);
            case FILE:
                return new FriendshipFileRepository(validator, getRequired("file.friendships"));
            case DATABASE:
                return new FriendshipDbRepository(validator,
                        getRequired("db.url"), getRequired("db.user"), getRequired("db.password"));
            default:
                throw new ConfigurationException("Unsupported repository type: " + type);
        }
    }

    /**
     * Reads and checks {@code repository.type}.
     *
     * @return the configured repository type
     * @throws ConfigurationException if the value is missing or is not one of MEMORY, FILE, DATABASE
     */
    private RepositoryType readType() {
        String value = getRequired("repository.type");
        try {
            return RepositoryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ConfigurationException("Unknown repository.type '" + value
                    + "' in config.properties. Expected MEMORY, FILE or DATABASE.", e);
        }
    }

    /**
     * Reads a property that must be present and not blank.
     *
     * @param key the name of the property
     * @return the trimmed value of the property
     * @throws ConfigurationException if the property is missing (or blank, except for db.password)
     */
    private String getRequired(String key) {
        String value = properties.getProperty(key);
        if (value == null) {
            throw new ConfigurationException("Missing property '" + key + "' in config.properties.");
        }
        value = value.trim();
        return value;
    }
}
