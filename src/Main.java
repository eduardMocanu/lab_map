import UI.ConsoleUI;
import UI.UI;
import exceptions.*;
import repository.FriendshipRepository;
import repository.UserRepository;
import repository.factory.RepositoryFactory;
import service.CommunityService;
import service.FriendshipService;
import service.UserService;
import validators.FriendshipValidator;
import validators.UserValidator;

/**
 * Entry point of the social network application.
 * Builds the application from its parts (composition root): the repositories come from
 * {@link RepositoryFactory}, according to config.properties, so this class never names
 * a concrete repository and does not change when the type of persistence changes.
 */
public class Main {

    private Main() {
    }

    /**
     * Starts the application.
     *
     * @param args not used
     */
    public static void main(String[] args) {
        try {
            RepositoryFactory factory = new RepositoryFactory();
            UserRepository userRepository = factory.createUserRepository(new UserValidator().andThen((user) -> {
                if (user.getUsername().equals("admin")) {
                    throw new ValidationException("Admin username is reserved for different purposes");
                }
            }));
            FriendshipRepository friendshipRepository = factory.createFriendshipRepository(new FriendshipValidator());
            System.out.println("Persistence: " + factory.getType());

            UserService userService = new UserService(friendshipRepository, userRepository);
            FriendshipService friendshipService = new FriendshipService(userRepository, friendshipRepository);
            CommunityService communityService = new CommunityService(userService, friendshipService);

            int pageSize = factory.getPageSize();
            UI ui = new ConsoleUI(userService, friendshipService, communityService, pageSize);
            ui.run();
        } catch (ConfigurationException e) {
            System.out.println("Configuration error: " + e.getMessage());
        } catch (RepositoryException e) {
            System.out.println("Could not load the data: " + e.getMessage());
        }
        catch (NumberFormatException e) {
            System.out.println("Error: ids must be numbers.");
        } catch (ValidationException | EntityNotFoundException | DuplicateEntityException | IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }
}
