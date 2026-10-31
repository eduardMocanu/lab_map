package criteria;

import model.Friendship;

import java.util.function.Predicate;

public final class FriendshipCriteria {

    private FriendshipCriteria() {
    }

    /**
     * @param userId the id of a user
     * @return a criterion matching the friendships in which the user takes part
     */
    public static Predicate<Friendship> involves(Long userId) {
        return friendship -> friendship.involves(userId);
    }
}