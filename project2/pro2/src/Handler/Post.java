package Handler;

public interface Post {
    /**
     * 1. create post
     * 2. delete post
     */

    void handlePost();

    void deletePost();

    void createPost();

    void close();
}
