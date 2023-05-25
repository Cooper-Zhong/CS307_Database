package Handler;

public interface Action {

    /**
     * 1.like 2.favorite 3.share 4.follow/unfollow
     * 5. block/unblock 6. three in one(like, favorite, share)
     */

    void handleActions();

    void unblockUser();

    void blockUser();

    void followUser();

    void unfollowUser();

    void threeInOne(); // like, favorite, share in one function

    void sharePost();

    void favoritePost();

    void likePost();

    void close();
}