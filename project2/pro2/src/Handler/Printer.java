package Handler;

import Handler.AccountHandler;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Printer {

    public void printSecondReply(ResultSet rs, boolean isShowMe) {
        // if isShowMe, then just print reply with my username
        try {
            int post_id = 0;
            int first_id = 0;
            int second_id = 0;
            if (rs.next()) {
                rs.first();
                do {
                    int cur_post_id = rs.getInt("post_id");
                    if (cur_post_id != post_id) {// new post
                        post_id = outPost(rs, cur_post_id);
                        System.out.println("------------------------------------------------------------------------------------------------------------------------------------------");
                    }//then print first reply
                    first_id = outFirst(rs, first_id);
                    //then print second reply
                    int cur_second_id = rs.getInt("second_id");
                    if (cur_second_id != second_id && rs.getString("second_author") != null) {// new second reply
                        second_id = cur_second_id;
                        if (isShowMe) {
                            String user = AccountHandler.getUser();
                            String first_author = rs.getString("first_author");
                            String second_author = rs.getString("second_author");
                            if (first_author != null && first_author.equals(user) && second_author != null && !second_author.equals(user))
                                // if first_author is me and second_author is not me, do not print second reply
                                continue;
                        }
                        System.out.println("[ second reply id ]: " + rs.getInt("second_id"));
                        System.out.println("[ second content ]: " + rs.getString("second_content"));
                        System.out.println("[ second author ]: " + rs.getString("second_author"));
                        System.out.println("----------------------------------------");
                    }
                } while (rs.next());
            } else {
                System.out.println("No second replies.");
                System.out.println("------------------");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void printFirstReply(ResultSet rs) {
        try {
            int post_id = 0;
            int first_id = 0;
            if (rs.next()) {
                rs.first();
                do {// print post first
                    int cur_post_id = rs.getInt("post_id");
                    if (cur_post_id != post_id) {// new post
                        post_id = outPost(rs, cur_post_id);
                        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------");
                    }//then print first reply
                    first_id = outFirst(rs, first_id);

                } while (rs.next());
            } else {
                System.out.println("No reply found.");
                System.out.println("---------------");
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Search failed, please try again.");
            System.out.println("--------------------------------");
        }
    }

    /**
     * print first reply to console
     */
    public int outFirst(ResultSet rs, int first_id) throws SQLException {
        int cur_first_id = rs.getInt("first_id");
        if (cur_first_id != first_id && rs.getString("first_author") != null) {// new first reply
            first_id = cur_first_id;
            System.out.println("[ first_id ]: " + rs.getInt("first_id"));
            System.out.println("[ first_stars ]: " + rs.getInt("first_stars"));
            System.out.println("[ first_author ]: " + rs.getString("first_author"));
            System.out.println("[ first_content ]: " + rs.getString("first_content"));
            System.out.println("--------------------------------------------------------------------------------------");
        }
        return first_id;
    }

    /**
     * print post to console
     */
    public int outPost(ResultSet rs, int cur_post_id) throws SQLException {
        int post_id;
        post_id = cur_post_id;
        System.out.println("[ Post ID ]: " + rs.getInt("post_id"));
        System.out.println("[ Title ]: " + rs.getString("title"));
        System.out.println("[ Author ]: " + rs.getString("author_name"));
        System.out.println("[ Content ]: " + rs.getString("content"));
        System.out.println("[ Post time ]: " + rs.getTimestamp("post_time"));
        return post_id;
    }

    public void printPost(ResultSet rs) throws SQLException {
        if (rs.next()) {// if there is result
            rs.first();// roll back to the first one
            int post_id = 0;
            do {
                int cur_post_id = rs.getInt("post_id");
                if (cur_post_id != post_id) {// new post
                    post_id = outPost(rs, cur_post_id);
                    System.out.println("----------------------------------------------------------------------------------------------------------------------");
                }

            } while (rs.next());
        } else {
            System.out.println("No post are found.");
            System.out.println("-------------------------------------------------------------------");
        }
    }
}
