package repository;

import criteria.FriendshipCriteria;
import model.Entity;
import model.Friendship;
import model.Tuple;
import model.User;
import util.paging.Page;
import util.paging.Pageable;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for friendships, adding queries for all friendships of a user.
 * Friendship ids are pairs of user ids, normalized as (smaller id, larger id).
 * The queries have default implementations based on {@link #findAll()} and {@link #delete(Object)},
 * so every implementation (memory, file) gets them without duplicating code;
 * a database implementation can override them with SQL queries.
 */
public interface FriendshipRepository extends Repository<Tuple<Long, Long>, Friendship> {

    /**
     * Finds all friendships in which a user takes part.
     *
     * @param userId the id of the user
     * @return the user's friendships; empty if the user has none
     */
    default List<Friendship> findByUser(Long userId) {
        return findAll(FriendshipCriteria.involves(userId));
    }

    /**
     * Removes all friendships in which a user takes part.
     * Used when the user is removed from the network.
     *
     * @param userId the id of the user
     * @throws exceptions.RepositoryException if the change cannot be persisted
     */
    default void deleteByUser(Long userId) {
        findAll(FriendshipCriteria.involves(userId)).stream()
                .map(Entity::getId)
                .forEach(this::delete);
    }

    /**
     * Returns one page of the friends of a user, ordered by id.
     * Paging is done in SQL, so only the database repository implements it.
     *
     * @param userId   the id of the user
     * @param pageable which page to return and how many friends per page
     * @return the friends on that page and the user's total number of friends; empty after the last page
     * @throws UnsupportedOperationException  for memory and file storage
     * @throws exceptions.RepositoryException if the database cannot be read
     */
    default Page<User> findFriendsOnPage(Long userId, Pageable pageable) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
