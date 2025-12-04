package service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class FeedCache {

    private final Map<Integer, TreeMap<Long,Long>> userFeed = new ConcurrentHashMap<>();


    public void insertPostIntoFeed(int followerId,long postId,long timestamp){

        userFeed.computeIfAbsent(followerId,k->new TreeMap<>(Collections.reverseOrder())).put(timestamp,postId);

    }

    public List<Long> getFeedPostId(int userId,int limit){
        return userFeed.getOrDefault(userId,new TreeMap<>()).entrySet().stream()
                .limit(limit)
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }
}
