package Handler;



public interface Reply {

    void handleReply();

    void replyReply(boolean isAnonymous);

    void replyPost(boolean isAnonymous);

    void close();
}
