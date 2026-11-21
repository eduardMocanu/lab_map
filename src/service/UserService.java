package service;

import exceptions.DuplicateEntityException;
import exceptions.EntityNotFoundException;
import model.User;
import repository.FriendshipRepository;
import repository.PagingRepository;
import repository.UserRepository;
import util.paging.Page;
import util.paging.Pageable;

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
    /**
     * Returns one page of users. Paging is done by the storage, so it is available only for
     * repositories that implement {@link PagingRepository} (the database).
     *
     * @param pageNumber the page, starting from 0
     * @param pageSize   the number of users per page
     * @return the users on that page and the total number of users; an empty page after the last one
     * @throws IllegalArgumentException       if {@code pageNumber < 0} or {@code pageSize <= 0}
     * @throws UnsupportedOperationException  if the storage does not support paging (memory, file)
     * @throws exceptions.RepositoryException if the storage cannot be read
     */
    public Page<User> getUsersOnPage(int pageNumber, int pageSize) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative.");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be positive.");
        }
        return repository.findAllOnPage(new Pageable(pageNumber, pageSize));
    }
}
