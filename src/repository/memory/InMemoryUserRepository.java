package repository.memory;

import model.User;
import repository.IdGenerator;
import repository.UserRepository;
import validators.Validator;

/**
 * In-memory repository for users.
 * The queries by username and email are inherited from {@link UserRepository};
 * new users receive their id from this repository when they are saved.
 */
public class InMemoryUserRepository extends InMemoryRepository<Long, User> implements UserRepository {

    /**
     * @param validator the validator applied to every user before it is stored
     */
    public InMemoryUserRepository(Validator<User> validator) {
        super(validator);
    }

    /**
     * {@inheritDoc}
     * If the user has no id yet, a new one is assigned first (largest existing id + 1).
     */
    @Override
    public User save(User user) {
        if (user != null && user.getId() == null) {
            user.setId(IdGenerator.nextId(entities.values()));
        }
        return super.save(user);
    }
}
