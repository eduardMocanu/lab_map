package model;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * A friendship between two users of the network.
 * The id is the pair of the two user ids, always stored as (smaller id, larger id),
 * so the friendship between A and B is the same as the one between B and A.
 */
public class Friendship extends Entity<Tuple<Long, Long>> {

    private final LocalDateTime friendsFrom;

    /**
     * Creates a new friendship that starts now (precision: minutes).
     *
     * @param userId1 the id of one user
     * @param userId2 the id of the other user
     */
    public Friendship(Long userId1, Long userId2) {
        this(userId1, userId2, LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
    }

    /**
     * Creates a friendship with a given start moment, for example when loading it from a file.
     *
     * @param userId1     the id of one user
     * @param userId2     the id of the other user
     * @param friendsFrom the moment the friendship started
     */
    public Friendship(Long userId1, Long userId2, LocalDateTime friendsFrom) {
        setId(normalize(userId1, userId2));
        this.friendsFrom = friendsFrom;
    }

    /**
     * Builds the id of a friendship, ordering the two user ids so the smaller one comes first.
     * If one of the ids is null, the pair is returned unchanged so the validator can report it.
     *
     * @param a the id of one user
     * @param b the id of the other user
     * @return the normalized pair (min, max)
     */
    public static Tuple<Long, Long> normalize(Long a, Long b) {
        if (a == null || b == null) {
            return new Tuple<>(a, b);
        }
        if (a <= b) {
            return new Tuple<>(a, b);
        }
        return new Tuple<>(b, a);
    }

    /** @return the id of the first user (the smaller id) */
    public Long getUserId1() {
        return getId().getFirst();
    }

    /** @return the id of the second user (the larger id) */
    public Long getUserId2() {
        return getId().getSecond();
    }

    /** @return the moment the friendship started */
    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    /**
     * Checks whether a user takes part in this friendship.
     *
     * @param userId the id of the user
     * @return true if the user is one of the two friends
     */
    public boolean involves(Long userId) {
        return userId != null && (userId.equals(getUserId1()) || userId.equals(getUserId2()));
    }

    /**
     * @return a readable description of the friendship, with both user ids and the start moment
     */
    @Override
    public String toString() {
        return "Friendship{" + getUserId1() + " - " + getUserId2() + ", since " + friendsFrom + '}';
    }
}
