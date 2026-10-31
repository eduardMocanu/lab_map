package service;

import model.Friendship;
import model.User;

import java.util.*;

/**
 * Service that analyzes the network as a graph: users are nodes and friendships are edges.
 * A community is a connected component of this graph.
 * The service only reads data, through {@link UserService} and {@link FriendshipService}.
 */
public class CommunityService {
    private final UserService userService;
    private final FriendshipService friendshipService;

    /** Adjacency lists: for each user id, the ids of their friends. Rebuilt before every query. */
    private final HashMap<Long, ArrayList<Long>> graph = new HashMap<>();

    /**
     * @param userService       the service that provides the users (the nodes)
     * @param friendshipService the service that provides the friendships (the edges)
     */
    public CommunityService(UserService userService, FriendshipService friendshipService) {
        this.userService = userService;
        this.friendshipService = friendshipService;
    }

    /**
     * Rebuilds the graph from the current users and friendships.
     * Every user is a node, including users without friends; every friendship is added in both directions.
     */
    private void loadGraph() {
        graph.clear();
        List<Friendship> friendships = friendshipService.getAll();

        for (User user : userService.getAll()) {
            graph.put(user.getId(), new ArrayList<>());
        }

        for (var friendship : friendships) {
            Long id1 = friendship.getUserId1();
            Long id2 = friendship.getUserId2();
            if (!graph.containsKey(id1)) {
                graph.put(id1, new ArrayList<>());
            }
            if (!graph.containsKey(id2)) {
                graph.put(id2, new ArrayList<>());
            }
            graph.get(id1).add(id2);
            graph.get(id2).add(id1);
        }
    }

    /**
     * Finds the most sociable community: the community that contains the longest path
     * (the longest chain of friendships with no user repeated).
     * If several communities have the same longest path, one of them is returned.
     *
     * @return the users of that community, or an empty list if there are no users
     */
    public List<User> mostSocialCommunity() {
        loadGraph();

        int bestLength = -1;
        Long bestStart = null;

        for (Long node : graph.keySet()) {
            int length = longestPathFrom(node, new HashSet<>());
            if (length > bestLength) {
                bestLength = length;
                bestStart = node;
            }
        }

        if (bestStart == null) {
            return new ArrayList<>();
        }

        Set<Long> community = new HashSet<>();
        dfs(bestStart, community);

        List<User> users = new ArrayList<>();
        for (Long id : community) {
            Optional<User> user = userService.findOne(id);
            if (user.isPresent()) {
                users.add(user.get());
            }
        }
        return users;
    }

    /**
     * Counts the communities of the network. A user without friends is a community of their own.
     *
     * @return the number of communities
     */
    public Long numberCommunities() {
        loadGraph();
        long counter = 0;
        Set<Long> visited = new HashSet<>();
        for (var key : graph.keySet()) {
            if (!visited.contains(key)) {
                dfs(key, visited);
                counter += 1;
            }
        }
        return counter;
    }

    /**
     * Depth-first search that marks every user reachable from a starting user.
     *
     * @param idStart the id of the starting user
     * @param visited the set to which every reached user id is added
     */
    private void dfs(Long idStart, Set<Long> visited) {
        visited.add(idStart);
        for (var neighbour : graph.get(idStart)) {
            if (!visited.contains(neighbour)) {
                dfs(neighbour, visited);
            }
        }
    }

    /**
     * Computes the length of the longest simple path starting at a user, trying every route.
     * A user is removed from {@code onPath} when the search backs out of it (backtracking),
     * so other routes can still go through it. Exponential in the worst case, which is fine for small networks.
     *
     * @param node   the current user id
     * @param onPath the user ids already on the current path
     * @return the number of friendships in the longest path starting at {@code node}
     */
    private int longestPathFrom(Long node, Set<Long> onPath) {
        onPath.add(node);
        int best = 0;
        for (Long neighbour : graph.get(node)) {
            if (!onPath.contains(neighbour)) {
                best = Math.max(best, 1 + longestPathFrom(neighbour, onPath));
            }
        }
        onPath.remove(node);
        return best;
    }
}