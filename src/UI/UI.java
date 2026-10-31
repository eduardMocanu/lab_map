package UI;

/**
 * A user interface for the social network.
 * Implementations (console, graphical) only talk to the service layer,
 * so the interface can be replaced without changing the rest of the application.
 */
public interface UI {

    /**
     * Starts the interface and keeps it running until the user chooses to exit.
     */
    void run();
}