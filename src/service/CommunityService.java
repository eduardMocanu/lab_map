package service;

import model.Friendship;
import model.User;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service that analyzes the network as a graph: users are nodes and friendships are edges.
 * A community is a connected component of this graph.
 * The service only reads data, through {@link UserService} and {@link FriendshipService}.
 */
public class CommunityService {
    private final UserService userService;
    private final FriendshipService friendshipService;

    /** Adjacency lists: for each user id, the ids of their friends. Rebuilt before every query. */
    private HashMap<Long, ArrayList<Long>> graph = new HashMap<>();

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

        HashMap<Long, ArrayList<Long>> graph = userService.getAll().stream().collect(Collectors.toMap(User::getId, user -> new ArrayList<>(), (a, b) -> a, HashMap::new));

        friendshipService.getAll().forEach(friendship -> {
            graph.computeIfAbsent(friendship.getUserId1(), id -> new ArrayList<>()).add(friendship.getUserId2());
            graph.computeIfAbsent(friendship.getUserId2(), id -> new ArrayList<>()).add(friendship.getUserId1());
        });
        this.graph = graph;
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

        Optional<Long> bestStart = graph.keySet().stream()
                .max(Comparator.comparingInt(node -> longestPathFrom(node, new HashSet<>())));

        if (bestStart.isEmpty()) {
            return new ArrayList<>();
        }

        Set<Long> community = new HashSet<>();
        dfs(bestStart.get(), community);

        return community.stream().map(userService::findOne).flatMap(Optional::stream).collect(Collectors.toList());
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
        if (!visited.add(idStart)) {
            return;
        }
        graph.get(idStart).stream()
                .filter(n -> !visited.contains(n))
                .forEach(n -> {dfs(n, visited);});

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
        int best = graph.get(node).stream().filter(id -> !onPath.contains(id)).mapToInt(nei -> 1 + longestPathFrom(nei, onPath)).max().orElse(0);
        onPath.remove(node);
        return best;
    }
}