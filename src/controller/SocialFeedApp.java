package controller;

import model.Post;
import pipeline.FanoutWorker;
import pipeline.MessageQueue;
import service.FeedCache;
import service.FollowerGraph;
import service.PostDatabase;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SocialFeedApp {
    private final PostDatabase postDb;
    private final FollowerGraph followerGraph;
    private final FeedCache feedCache;
    private final MessageQueue mq;

    public SocialFeedApp(PostDatabase postDb,FollowerGraph followerGraph,FeedCache feedCache,MessageQueue mq){
        this.postDb=postDb;
        this.followerGraph=followerGraph;
        this.feedCache=feedCache;
        this.mq=mq;
    }
    public static void main(String[] arg) throws  InterruptedException {

        PostDatabase postDb= new PostDatabase();
        FollowerGraph followerGraph = new FollowerGraph();
        FeedCache feedCache = new FeedCache();
        MessageQueue mq = new MessageQueue();

        SocialFeedApp app = new SocialFeedApp(postDb,followerGraph,feedCache,mq);

        ExecutorService fanoutExecutor = Executors.newSingleThreadExecutor();
        FanoutWorker worker = new FanoutWorker(mq, postDb, followerGraph, feedCache);
        fanoutExecutor.submit(worker);

        // --- 2. Setup Mock Users and Follower Graph ---
        int regularUserA = 101;
        int regularUserB = 102;
        int celebrityUserZ = 900;
        int followerP = 201; // Follows A, B, and Z
        int followerQ = 202; // Follows A and Z
        int nonFollowerR = 300; // Follows nobody


        followerGraph.addFollow(celebrityUserZ, followerP);
        followerGraph.addFollow(celebrityUserZ, followerQ);
        followerGraph.addFollow(celebrityUserZ, regularUserB);

        System.out.println("\n--- Setup Complete (Threshold is " + FollowerGraph.getCelebrityThreshold() + " Followers) ---");
        System.out.println("User " + regularUserA + " and " + regularUserB + " are REGULAR users.");
        System.out.println("User " + celebrityUserZ + " is a CELEBRITY user (3+ followers).");
        System.out.println("----------------------");

        // Post 1 (Regular User A): Fanout-on-Write (PUSH)
        app.postMessage(regularUserA, "The design decision is Hybrid Fanout!");

        // Post 2 (Celebrity User Z): Fanout-on-Read (SKIP PUSH)
        app.postMessage(celebrityUserZ, "My latest viral post about software architecture.");

        // Post 3 (Regular User B): Fanout-on-Write (PUSH)
        app.postMessage(regularUserB, "Testing the regular user throughput.");

        // Give the Fanout Worker time to process the messages
        Thread.sleep(2000);

        System.out.println("\n--- Feed Retrieval Simulation (Hybrid Read Path) ---");
        int feedLimit = 5;

        // Follower P's feed (Should see posts from A, B, and Z)
        app.displayHybridFeed(followerP, feedLimit);

        // Follower Q's feed (Should see posts from A and Z)
        app.displayHybridFeed(followerQ, feedLimit);

        // Non-Follower R's feed (Should be empty)
        app.displayHybridFeed(nonFollowerR, feedLimit);

        // --- 5. Cleanup ---
        worker.stop();
        fanoutExecutor.shutdown();
        fanoutExecutor.awaitTermination(1, TimeUnit.SECONDS);


    }

    private void postMessage(int userId, String content) {
        long postId = System.nanoTime();
        Post post = new Post(postId, userId, content);
        postDb.save(post);
        mq.publishPostEvent(postId);
    }

    private void displayHybridFeed(int userId,int limit){
        System.out.println("\n--- User " + userId + " Feed ---");

        List<Long> cachedPostIds = feedCache.getFeedPostId(userId,limit);

        Set<Integer> followees = followerGraph.getFollowees(userId);
        List<Integer> celebrityFollowees = followees.stream()
                .filter(followerGraph::isCelebrity)
                .collect(Collectors.toList());

        System.out.println("   [Read Path] Follows " + followees.size() + " users. " + celebrityFollowees.size() + " are Celebrities.");

        // 3. PULL CELEBRITY POSTS (Fanout-on-Read posts: Live PULL data)
        List<Post> celebrityPosts = new ArrayList<>();
        for (Integer celebId : celebrityFollowees) {
            List<Post> celebPosts = postDb.getAllPosts().stream()
                    .filter(p -> p.getUserId() == celebId)
                    .sorted(Comparator.comparingLong(Post::getTimestamp).reversed())
                    .limit(limit)
                    .collect(Collectors.toList());
            celebrityPosts.addAll(celebPosts);
        }

        // 4. Merge and Sort (PUSH + PULL data)
        List<Post> cachedPosts = cachedPostIds.stream().map(postDb::get).filter(Objects::nonNull).collect(Collectors.toList());
        Set<Post> mergedPosts = new HashSet<>(cachedPosts);
        mergedPosts.addAll(celebrityPosts);

        List<Post> finalFeed = mergedPosts.stream()
                .sorted(Comparator.comparingLong(Post::getTimestamp).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        // 5. Display the Feed
        if (finalFeed.isEmpty()) {
            System.out.println("   [Feed] (Empty)");
        } else {
            System.out.println("   [Feed] Merged and Sorted " + finalFeed.size() + " posts:");
            finalFeed.forEach(post -> System.out.println("   -> " + post));
        }
    }


}
