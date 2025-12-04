package service;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FollowerGraph {

    private static final int CELEBRITY_THRESHOLD = 3;

    private final Map<Integer, Set<Integer>> followers = new ConcurrentHashMap<>();

    public static int getCelebrityThreshold() {
        return CELEBRITY_THRESHOLD;
    }

    public void addFollow(int followedId, int followerId) {
        followers.computeIfAbsent(followedId, k -> ConcurrentHashMap.newKeySet()).add(followerId);
    }

    public Set<Integer> getFollowers(int followedId) {
        return followers.getOrDefault(followedId, Collections.emptySet());
    }

    public boolean isCelebrity(int userId) {
        int count = followers.getOrDefault(userId, Collections.emptySet()).size();

        return count >= CELEBRITY_THRESHOLD;
    }


    public Set<Integer> getFollowees(int followerId) {
        Set<Integer> followees = ConcurrentHashMap.newKeySet();
        for (Map.Entry<Integer, Set<Integer>> entry : followers.entrySet()) {
            if (entry.getValue().contains(followerId)) {
                followees.add(entry.getKey());

            }

        }
        return followees;


    }
}
