package service;

import exceptions.DuplicateEntityException;
import exceptions.EntityNotFoundException;
import model.User;
import repository.FriendshipRepository;
import repository.UserRepository;

import java.util.Optional;

/**
 * Service for managing the users of the social network.
 * Usernames and emails must be unique, and removing a user also removes all their friendships,
 * so the users and friendships stay consistent.
 */
public class UserService extends AbstractService<Long, User, UserRepository> {

    private final FriendshipRepository friendshipRepository;

    /**
     * @param friendshipRepository the repository of friendships, used to remove a user's friendships
     * @param repository           the repository of users
     */
    public UserService(FriendshipRepository friendshipRepository, UserRepository repository) {
        super(repository);
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * Checks that the username and the email are not already used by another user.
     *
     * @param user the user about to be added
     * @throws DuplicateEntityException if the username or the email is already taken
     */
    @Override
    protected void checkBeforeAdd(User user) {
        if (repository.findByUsername(user.getUsername()).isPresent()) {
            throw new DuplicateEntityException("Username '" + user.getUsername() + "' is already taken.");
        }
        if (repository.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateEntityException("Email '" + user.getEmail() + "' is already used.");
        }
    }

    /**
     * Removes all friendships of the user, so none refers to a user that no longer exists.
     *
     * @param id the id of the user about to be removed
     */
    @Override
    protected void beforeRemove(Long id) {
        friendshipRepository.deleteByUser(id);
    }

    /**
     * Creates and saves a new user; its id is assigned automatically.
     *
     * @param username the username, unique in the network
     * @param email    the email address, unique in the network
     * @param password the password
     * @return the saved user, with its id set
     * @throws exceptions.ValidationException if a field is invalid
     * @throws DuplicateEntityException       if the username or the email is already taken
     * @throws exceptions.RepositoryException if the user cannot be persisted
     */
    public User addUser(String username, String email, String password) {
        return add(new User(username, email, password));
    }

    /**
     * Removes a user and all their friendships.
     *
     * @param id the id of the user
     * @return the removed user
     * @throws EntityNotFoundException        if no user has this id
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    public User removeUser(Long id) {
        return remove(id);
    }

    /**
     * Removes the user with the given username and all their friendships.
     *
     * @param username the username of the user
     * @return the removed user
     * @throws EntityNotFoundException        if no user has this username
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    public User removeUserByUsername(String username) {
        Optional<User> user = repository.findByUsername(username);
        if (user.isEmpty()) {
            throw new EntityNotFoundException("User '" + username + "' does not exist.");
        }
        return remove(user.get().getId());
    }
}
