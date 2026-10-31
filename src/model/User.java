package model;

/**
 * A user of the social network, identified by a numeric id.
 * Friendships are not stored in the user; they are kept separately as {@link Friendship} entities.
 */
public class User extends Entity<Long> {

    private String username;
    private String email;
    private String password;

    /**
     * Creates a new user without an id.
     * The id is assigned by the repository when the user is saved.
     *
     * @param username the user's unique username
     * @param email    the user's unique email address
     * @param password the user's password
     */
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    /** @return the username */
    public String getUsername() {
        return username;
    }

    /** @param username the new username */
    public void setUsername(String username) {
        this.username = username;
    }

    /** @return the email address */
    public String getEmail() {
        return email;
    }

    /** @param email the new email address */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return the password */
    public String getPassword() {
        return password;
    }

    /** @param password the new password */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * @return a readable description of the user: id, username and email (the password is not shown)
     */
    @Override
    public String toString() {
        return "User{id=" + getId() + ", username='" + username + "', email='" + email + "'}";
    }
}
