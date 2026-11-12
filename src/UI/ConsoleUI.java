package UI;

import exceptions.DuplicateEntityException;
import exceptions.EntityNotFoundException;
import exceptions.RepositoryException;
import exceptions.ValidationException;
import model.Friendship;
import model.User;
import service.CommunityService;
import service.FriendshipService;
import service.UserService;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Minimal console interface for the social network.
 * Reads one command per line, calls the matching service method and prints the result.
 * This is the only place where exceptions from the lower layers are caught
 * and turned into messages for the user, so invalid input never stops the program.
 */
public class ConsoleUI implements UI {

    private final UserService userService;
    private final FriendshipService friendshipService;
    private final CommunityService communityService;
    private final Scanner scanner = new Scanner(System.in);

    /**
     * @param u the service for users
     * @param f the service for friendships
     * @param c the service for community analysis
     */
    public ConsoleUI(UserService u, FriendshipService f, CommunityService c) {
        this.userService = u;
        this.friendshipService = f;
        this.communityService = c;
    }

    /**
     * Starts the interface: checks that the stored data is consistent, prints the available commands,
     * then reads and runs one command per line until the user types {@code exit} or the input ends.
     * Errors from any command are printed and the loop continues.
     */
    @Override
    public void run() {
        checkConsistency();
        printHelp();
        while (true) {
            System.out.print("> ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String line = scanner.nextLine().trim();
            if (line.isEmpty()) {
                continue;
            }
            String[] parts = line.split("\\s+");
            if (parts[0].equalsIgnoreCase("exit")) {
                System.out.println("Bye!");
                break;
            }
            try {
                handle(parts);
            } catch (ValidationException | EntityNotFoundException | DuplicateEntityException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (RepositoryException e) {
                System.out.println("Storage error: " + e.getMessage());
            } catch (UnsupportedOperationException e) {
                System.out.println("Not available: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: ids must be numbers.");
            }
        }
    }

    /**
     * Removes the friendships that refer to users who do not exist (for example after a corrupt
     * user line was skipped) and tells the user, so the two files stay consistent.
     */
    private void checkConsistency() {
        try {
            List<Friendship> removed = friendshipService.removeFriendshipsOfMissingUsers();
            removed.stream().map(friendship -> "Removed friendship " + friendship.getUserId1() + " - "
                     + friendship.getUserId2() + ": one of the users does not exist.").forEach(System.out::println);
        } catch (RepositoryException e) {
            System.out.println("Storage error: " + e.getMessage());
        } catch (UnsupportedOperationException e) {
            System.out.println("Not available: " + e.getMessage());
        }
    }

    /**
     * Runs the command named by the first word of the input line.
     *
     * @param parts the words of the input line; {@code parts[0]} is the command
     */
    private void handle(String[] parts) {
        switch (parts[0].toLowerCase()) {
            case "adduser":
                addUser(parts);
                break;
            case "removeuser":
                removeUser(parts);
                break;
            case "addfriend":
                addFriend(parts);
                break;
            case "removefriend":
                removeFriend(parts);
                break;
            case "users":
                listUsers();
                break;
            case "friendships":
                listFriendships();
                break;
            case "communities":
                countCommunities();
                break;
            case "sociable":
                mostSociable();
                break;
            case "help":
                printHelp();
                break;
            default:
                System.out.println("Unknown command. Type 'help' to see the commands.");
        }
    }

    /**
     * Command {@code adduser <username> <email> <password>}: adds a new user.
     * With no arguments ({@code adduser} alone), the fields are asked one per line,
     * so they may contain spaces.
     *
     * @param args the words of the command
     */
    private void addUser(String[] args) {
        User user;
        if (args.length == 1) {
            System.out.print("username: ");
            String username = scanner.nextLine();
            System.out.print("email: ");
            String email = scanner.nextLine();
            System.out.print("password: ");
            String password = scanner.nextLine();
            user = userService.addUser(username, email, password);
        } else if (args.length == 4) {
            user = userService.addUser(args[1], args[2], args[3]);
        } else {
            System.out.println("Usage: adduser <username> <email> <password>   (or just: adduser)");
            return;
        }
        System.out.println("Added: " + user);
    }

    /**
     * Command {@code removeuser <id | username>}: removes a user and all their friendships.
     * The argument is treated as an id if it is a number, otherwise as a username.
     *
     * @param args the words of the command
     */
    private void removeUser(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: removeuser <id | username>");
            return;
        }
        User removed;
        if (isNumber(args[1])) {
            removed = userService.removeUser(Long.parseLong(args[1]));
        } else {
            removed = userService.removeUserByUsername(args[1]);
        }
        System.out.println("Removed: " + removed + " (and all their friendships)");
    }

    /**
     * Command {@code addfriend <user1> <user2>}: creates a friendship between two users,
     * given either by their ids (both numbers) or by their usernames.
     *
     * @param args the words of the command
     */
    private void addFriend(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: addfriend <id1 | username1> <id2 | username2>");
            return;
        }
        Friendship friendship;
        if (isNumber(args[1]) && isNumber(args[2])) {
            friendship = friendshipService.addFriendship(Long.parseLong(args[1]), Long.parseLong(args[2]));
        } else {
            friendship = friendshipService.addFriendshipByUsername(args[1], args[2]);
        }
        System.out.println("Added: " + describe(friendship));
    }

    /**
     * Command {@code removefriend <user1> <user2>}: removes the friendship between two users,
     * given either by their ids (both numbers) or by their usernames.
     *
     * @param args the words of the command
     */
    private void removeFriend(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: removefriend <id1 | username1> <id2 | username2>");
            return;
        }
        Friendship removed;
        if (isNumber(args[1]) && isNumber(args[2])) {
            removed = friendshipService.removeFriendship(Long.parseLong(args[1]), Long.parseLong(args[2]));
        } else {
            removed = friendshipService.removeFriendshipByUsername(args[1], args[2]);
        }
        System.out.println("Removed: " + describe(removed));
    }

    /** Command {@code users}: prints all users. */
    private void listUsers() {
        List<User> users = userService.getAll();
        if (users.isEmpty()) {
            System.out.println("No users.");
            return;
        }
        users.forEach(System.out::println);
    }

    /** Command {@code friendships}: prints all friendships, with usernames. */
    private void listFriendships() {
        List<Friendship> friendships = friendshipService.getAll();
        if (friendships.isEmpty()) {
            System.out.println("No friendships.");
            return;
        }
        friendships.forEach(System.out::println);
    }

    /** Command {@code communities}: prints the number of communities in the network. */
    private void countCommunities() {
        System.out.println("Number of communities: " + communityService.numberCommunities());
    }

    /** Command {@code sociable}: prints the users of the most sociable community. */
    private void mostSociable() {
        List<User> community = communityService.mostSocialCommunity();
        if (community.isEmpty()) {
            System.out.println("No users in the network.");
            return;
        }
        System.out.println("Most sociable community (" + community.size() + " users):");
        community.stream().map(user -> " " + user).forEach(System.out::println);
    }

    /** Command {@code help}: prints the list of available commands. */
    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  adduser <username> <email> <password>");
        System.out.println("  removeuser <id | username>");
        System.out.println("  addfriend <id1 | username1> <id2 | username2>");
        System.out.println("  removefriend <id1 | username1> <id2 | username2>");
        System.out.println("  users");
        System.out.println("  friendships");
        System.out.println("  communities");
        System.out.println("  sociable");
        System.out.println("  help");
        System.out.println("  exit");
    }

    /**
     * Builds a readable description of a friendship, showing usernames instead of ids.
     *
     * @param friendship the friendship to describe
     * @return a text like "ana - bob (since 2026-10-19T10:30)"
     */
    private String describe(Friendship friendship) {
        return usernameOf(friendship.getUserId1()) + " - " + usernameOf(friendship.getUserId2())
                + " (since " + friendship.getFriendsFrom() + ")";
    }

    /**
     * @param id the id of a user
     * @return the user's username, or "#id" if the user no longer exists
     */
    private String usernameOf(Long id) {
        Optional<User> user = userService.findOne(id);
        if (user.isPresent()) {
            return user.get().getUsername();
        }
        return "#" + id;
    }

    /**
     * @param text the text to check
     * @return true if the text contains only digits
     */
    private boolean isNumber(String text) {
        return text.matches("\\d+");
    }
}
