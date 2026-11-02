package validators;

/**
 * Strategy for validating an entity.
 * Each implementation contains the rules for one type of entity.
 * Repositories receive a validator through their constructor (from the repository factory)
 * and apply it to every entity they store, including entities loaded from a file,
 * so the rules can be replaced without changing the repositories (Strategy pattern).
 * Validators only check the entity itself; rules that depend on other data
 * (uniqueness, related entities existing) are checked by the services.
 *
 * @param <T> the type of entity being validated
 */
@FunctionalInterface
public interface Validator<T> {

    /**
     * Checks that the entity's data is valid.
     * Returns normally if the entity is valid.
     *
     * @param entity the entity to check
     * @throws exceptions.ValidationException if the entity is invalid, with a message describing every problem
     */
    void validate(T entity);

    /**
     * Adds the ability to chain validators
     * @param next next validator that we want to check
     * @return the new validator with the validation logic of both
     */
    default Validator<T> andThen(Validator<T> next) {
        if (next == null) {
            throw new NullPointerException("Validator cannot be null.");
        }
        return (entity) ->{
            this.validate(entity);
            next.validate(entity);
        };
    }
}
