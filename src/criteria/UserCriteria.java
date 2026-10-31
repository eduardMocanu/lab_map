package criteria;

import model.User;

import java.util.function.Predicate;

public final class UserCriteria {

    private UserCriteria() {}

    /**
     * @param username the exact username
     * @return a criterion matching the user with this username
     */
    public static Predicate<User> hasUsername(String username) {
        return user -> user.getUsername().equals(username);
    }

    /**
     * @param email the exact email address (case does not matter)
     * @return a criterion matching the user with this email
     */
    public static Predicate<User> hasEmail(String email) {
        return user -> user.getEmail().equalsIgnoreCase(email);
    }

}
