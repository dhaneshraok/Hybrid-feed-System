package pipeline;

import model.Post;
import service.FeedCache;
import service.FollowerGraph;
import service.PostDatabase;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class FanoutWorker implements  Runnable{
    private final MessageQueue mq;
    private final PostDatabase postDb;
    private final FollowerGraph followerGraph;
    private final FeedCache feedCache;
    private final AtomicBoolean running = new AtomicBoolean(true);


    public FanoutWorker(MessageQueue mq, PostDatabase postDb, FollowerGraph followerGraph, FeedCache feedCache){
        this.mq=mq;
        this.postDb=postDb;
        this.followerGraph=followerGraph;
        this.feedCache=feedCache;
    }

    @Override
    public void run(){
        System.out.println("\n*** Fanout Worker started... (Waiting for messages) ***");
        while (running.get()||!mq.queue.isEmpty()){
            try{
                Long postId = mq.consumerPostEvent();
                if(postId!=null){
                    executeConditionalFanout(postId);
                }
            }catch (InterruptedException e){
                Thread.currentThread().interrupt();
                running.set(false);
            }
        }
        System.out.println("*** Fanout Worker stopped. ***\n");
    }


    private void executeConditionalFanout(long postId){
        Post post = postDb.get(postId);
        if(post==null) return;

        int postingUserId = post.getUserId();;


        feedCache.insertPostIntoFeed(postingUserId,post.getPostId(),post.getTimestamp());



        if(followerGraph.isCelebrity(postingUserId)){
            System.out.println("--- [Fanout SKIP] Post " + postId + " from CELEBRITY " + postingUserId + ". Fanout-on-Write SKIPPED. Followers will PULL.");
        }else{
            Set<Integer> followers= followerGraph.getFollowers(postingUserId);
            System.out.println("--- [Fanout PUSH] Processing Post " + postId + " from REGULAR User " + postingUserId + ". Pushing to " + followers.size() + " followers.");

            for(int followerId:followers){
                feedCache.insertPostIntoFeed(followerId,post.getPostId(),post.getTimestamp());
            }

            System.out.println("--- [Fanout PUSH] Successfully fanned out Post " + postId + " to feed caches.");
        }




    }

    public void stop(){
        this.running.set(false);
    }

}
