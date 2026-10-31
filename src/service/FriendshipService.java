package service;

import exceptions.DuplicateEntityException;
import exceptions.EntityNotFoundException;
import model.Friendship;
import model.Tuple;
import model.User;
import repository.FriendshipRepository;
import repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing friendships between users.
 * A friendship can only be created between two existing users, and only once.
 */
public class FriendshipService extends AbstractService<Tuple<Long, Long>, Friendship, FriendshipRepository> {

    private final UserRepository userRepository;

    /**
     * @param userRepository the repository of users, used to check that both users exist
     * @param repository     the repository of friendships
     */
    public FriendshipService(UserRepository userRepository, FriendshipRepository repository) {
        super(repository);
        this.userRepository = userRepository;
    }

    /**
     * Checks that both users exist and that they are not already friends.
     * Null ids are left to the validator, which reports them when the friendship is saved.
     *
     * @param friendship the friendship about to be added
     * @throws EntityNotFoundException  if one of the users does not exist
     * @throws DuplicateEntityException if the friendship already exists
     */
    @Override
    protected void checkBeforeAdd(Friendship friendship) {
        Long id1 = friendship.getUserId1();
        Long id2 = friendship.getUserId2();
        if (id1 != null && userRepository.findOne(id1).isEmpty()) {
            throw new EntityNotFoundException("User " + id1 + " does not exist.");
        }
        if (id2 != null && userRepository.findOne(id2).isEmpty()) {
            throw new EntityNotFoundException("User " + id2 + " does not exist.");
        }
        if (repository.findOne(friendship.getId()).isPresent()) {
            throw new DuplicateEntityException("The friendship already exists.");
        }
    }

    /**
     * Creates a friendship between two users, starting now.
     *
     * @param user1 the id of one user
     * @param user2 the id of the other user
     * @return the saved friendship
     * @throws exceptions.ValidationException if the ids are invalid or equal
     * @throws EntityNotFoundException        if one of the users does not exist
     * @throws DuplicateEntityException       if the users are already friends
     * @throws exceptions.RepositoryException if the friendship cannot be persisted
     */
    public Friendship addFriendship(Long user1, Long user2) {
        return add(new Friendship(user1, user2));
    }

    /**
     * Creates a friendship between two users identified by their usernames.
     *
     * @param username1 the username of one user
     * @param username2 the username of the other user
     * @return the saved friendship
     * @throws exceptions.ValidationException if both usernames belong to the same user
     * @throws EntityNotFoundException        if one of the usernames does not exist
     * @throws DuplicateEntityException       if the users are already friends
     * @throws exceptions.RepositoryException if the friendship cannot be persisted
     */
    public Friendship addFriendshipByUsername(String username1, String username2) {
        User user1 = findUserByUsername(username1);
        User user2 = findUserByUsername(username2);
        return addFriendship(user1.getId(), user2.getId());
    }

    /**
     * Removes the friendship between two users. The order of the ids does not matter.
     *
     * @param user1 the id of one user
     * @param user2 the id of the other user
     * @return the removed friendship
     * @throws EntityNotFoundException        if the two users are not friends
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    public Friendship removeFriendship(Long user1, Long user2) {
        Tuple<Long, Long> id = Friendship.normalize(user1, user2);
        if (repository.findOne(id).isEmpty()) {
            throw new EntityNotFoundException("Users " + user1 + " and " + user2 + " are not friends.");
        }
        return remove(id);
    }

    /**
     * Removes the friendship between two users identified by their usernames.
     *
     * @param username1 the username of one user
     * @param username2 the username of the other user
     * @return the removed friendship
     * @throws EntityNotFoundException        if a username does not exist or the users are not friends
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    public Friendship removeFriendshipByUsername(String username1, String username2) {
        User user1 = findUserByUsername(username1);
        User user2 = findUserByUsername(username2);
        return removeFriendship(user1.getId(), user2.getId());
    }

    /**
     * Removes every friendship that refers to a user who does not exist
     * (for example after the users file was edited by hand), so the two files stay consistent.
     * Called once when the application starts.
     *
     * @return the friendships that were removed; empty if all friendships were valid
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    public List<Friendship> removeFriendshipsOfMissingUsers() {
        List<Friendship> removed = new ArrayList<>();
        for (Friendship friendship : repository.findAll()) {
            boolean user1Exists = userRepository.findOne(friendship.getUserId1()).isPresent();
            boolean user2Exists = userRepository.findOne(friendship.getUserId2()).isPresent();
            if (!user1Exists || !user2Exists) {
                repository.delete(friendship.getId());
                removed.add(friendship);
            }
        }
        return removed;
    }

    /**
     * Finds a user by username.
     *
     * @param username the username to look up
     * @return the user with this username
     * @throws EntityNotFoundException if no user has this username
     */
    private User findUserByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User '" + username + "' does not exist.");
        }
        return user.get();
    }
}
