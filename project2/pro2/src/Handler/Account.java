package Handler;


public interface Account {
    String user = null; // current user name

    /**
     * 1. login
     * 2. register
     * 3. logout
     */

    static String getUser() {
        return user;
    } // get current user name

    /**
     * handle all actions in account
     */
    void handleAccount();

    void logout();

    void login();

    void register();

    void close();
}
