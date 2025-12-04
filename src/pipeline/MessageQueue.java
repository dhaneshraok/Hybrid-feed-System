package pipeline;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageQueue {

    public final BlockingQueue<Long> queue = new LinkedBlockingQueue<>();

    public void publishPostEvent(long postId){
        queue.add(postId);
        System.out.println("-> [MQ] Post " + postId + " published to queue. Write successful.");
    }

    public Long consumerPostEvent()throws InterruptedException{

        return queue.poll(1, TimeUnit.SECONDS);
    }

}
