package model;

public class Post {
    private final long postId;
    private final int userId;
    private final String content;
    private final long timestamp;



    public Post(long postId,int userId,String content){
        this.postId=postId;
        this.userId=userId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public long getPostId(){
        return postId;
    }

    public int getUserId(){
        return userId;
    }

    public String getContent(){
        return content;
    }

    public long getTimestamp(){
        return timestamp;
    }
    @Override
    public String toString() {
        return "[ID:" + postId + " | User:" + userId + " | Time:" + timestamp + " | Content:'" + content.substring(0, Math.min(content.length(), 20)) + "...']";
    }


}
