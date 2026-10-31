package repository.memory;

import model.Friendship;
import model.Tuple;
import repository.FriendshipRepository;
import validators.Validator;

/**
 * In-memory repository for friendships.
 * The queries by user are inherited from {@link FriendshipRepository}.
 */
public class InMemoryFriendshipRepository extends InMemoryRepository<Tuple<Long, Long>, Friendship>
        implements FriendshipRepository {

    /**
     * @param validator the validator applied to every friendship before it is stored
     */
    public InMemoryFriendshipRepository(Validator<Friendship> validator) {
        super(validator);
    }
}
