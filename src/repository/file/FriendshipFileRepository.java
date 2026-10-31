package repository.file;

import exceptions.RepositoryException;
import model.Friendship;
import model.Tuple;
import repository.FriendshipRepository;
import validators.Validator;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/**
 * File repository for friendships.
 * Line format: {@code userId1;userId2;friendsFrom}, with the date in ISO format,
 * for example {@code 1;2;2026-10-19T10:30}.
 * The queries by user are inherited from {@link FriendshipRepository}.
 */
public class FriendshipFileRepository extends AbstractFileRepository<Tuple<Long, Long>, Friendship>
        implements FriendshipRepository {

    private static final int FIELDS = 3;

    /**
     * Creates the repository and loads all friendships from the file.
     *
     * @param validator the validator applied to every friendship, including the loaded ones
     * @param fileName  the path of the friendships file
     * @throws RepositoryException if the file exists but cannot be read
     */
    public FriendshipFileRepository(Validator<Friendship> validator, String fileName) {
        super(validator, fileName);
    }

    /**
     * Parses a line of the form {@code userId1;userId2;friendsFrom}.
     *
     * @param line a line from the file
     * @return the friendship described by the line
     * @throws RepositoryException if the line has the wrong number of fields,
     *                             a non-numeric id or an invalid date
     */
    @Override
    protected Friendship extractEntity(String line) {
        String[] parts = splitLine(line, FIELDS);
        Long user1;
        Long user2;
        try {
            user1 = Long.parseLong(parts[0].trim());
            user2 = Long.parseLong(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new RepositoryException("invalid user id in '" + parts[0] + ";" + parts[1] + "'", e);
        }
        try {
            LocalDateTime date = LocalDateTime.parse(parts[2].trim());
            return new Friendship(user1, user2, date);
        } catch (DateTimeParseException e) {
            throw new RepositoryException("invalid date '" + parts[2] + "' (expected e.g. 2026-10-19T10:30)", e);
        }
    }

    /**
     * Converts a friendship into a line of the form {@code userId1;userId2;friendsFrom}.
     *
     * @param friendship the friendship
     * @return the line that describes the friendship
     */
    @Override
    protected String createEntityAsString(Friendship friendship) {
        return friendship.getUserId1() + SEPARATOR + friendship.getUserId2() + SEPARATOR
                + friendship.getFriendsFrom();
    }
}
