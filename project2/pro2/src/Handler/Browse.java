package Handler;


public interface Browse {
    /**
     * 1. browse (posts, with replies)
     * 2. hot search list display and update
     * 3. multi-search
     */

    /**
     * handle all browse operations
     */
    void handleBrowse();

    /**
     * when a user search for a keyword/author/category, update the hot search list
     */
    void update_hot_search_list(String searchContent);

    void showHotSearchList();

    /**
     * @param opcode 1. print posts, 2. with first replies, 3. with second replies
     */
    void browsePost(int opcode);


    /**
     * @param codes  the codes of the parameters
     * @param values the values of the parameters
     * @param opcode 1 print posts, 2 print first replies, 3 print second replies
     */
    void multiSearch(String[] codes, String[] values, int opcode);

    void close();
}
