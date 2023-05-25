package Handler;

public interface Me {
    /** show:
     * 1. liked posts
     * 2. favorite posts
     * 3. shared posts
     * 4. following list
     * 5. my posts
     * 6. my replies
     * 7. blocking list
     */

    /**
     * handle all operations in Me
     */
    void handleMe();

    void showBlockedUsers();

    void showMyReplies();

    void showMyPosts();

    void showFollowingList();

    void showSharedPosts();

    void showFavoritePosts();

    void showLikedPosts();

    void close();
}
