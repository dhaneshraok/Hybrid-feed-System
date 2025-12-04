package service;

import model.Post;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PostDatabase {
    private final Map<Long, Post> posts = new ConcurrentHashMap<>();


    public void save(Post post){
        posts.put(post.getPostId(),post);
        System.out.println("-> [DB] Post " + post.getPostId() + " saved.");
    }

    public Post get(Long postId){
        return posts.get(postId);
    }
    public Collection<Post> getAllPosts() {
        return posts.values();
    }
}
