package Handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MeHandler {

    /**
     * Me handler
     * 1. show liked posts
     * 2. show favorite posts
     * 3. show shared posts
     * 4. show following list
     */
    private static Connection con;
    private static Scanner in;
    private PreparedStatement stmt;
    private Printer printer;//print the result of posts

    private ResultSet rs;

    public MeHandler(Connection con, Scanner in) {
        MeHandler.con = con;
        MeHandler.in = in;
        printer = new Printer();
    }

    public void handleMe() {
        System.out.println("Operation: [1]show liked posts\t[2]show favorite posts\t[3]show shared posts");
        System.out.println("           [4]show following list\t[5]view my posts\t[6]view my replies");
//        System.out.println("           [7]block user\t");
        System.out.println("----------------------------------------------------------------------------");
        // current operation code
        int opcode = readNum();
        switch (opcode) {
            case 1:
                showLikedPosts1();
                break;
            case 2:
                showFavoritePosts2();
                break;
            case 3:
                showSharedPosts3();
                break;
            case 4:
                showFollowingList4();
                break;
            case 5:
                viewMyPosts5();
                break;
            case 6:
                viewMyReplies6();
                break;
            default:
                System.out.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                break;
        }
    }


    private void viewMyReplies6() {
        try {
            String sql = "select * from posts p join first_replies fr on p.post_id = fr.post_id " +
                    "left join second_replies sr on fr.first_id = sr.first_id " +
                    "where fr.first_author = ? or sr.second_author = ?;";
            //left join in order to show the first reply even if there is no second reply
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("Your replies are:");
            System.out.println("-----------------");
            printer.printSecondReply(rs, true);
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }

    private void viewMyPosts5() {
        try {
            String sql = "select * from posts where author_name = ?;";
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("The posts you posted are:");
            System.out.println("-------------------------");
            printer.printPost(rs);
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }

    private void showFollowingList4() {
        try {
            String sql = "select followed_name from author_follow where author_name = ?";
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("The users you followed are:");
            int cnt = 0;
            if (rs.next()) {// if there is result
                rs.first();// roll back to get the first one
                do {
                    System.out.print(rs.getString("followed_name") + "\t");
                    if (++cnt % 5 == 0) System.out.println();
                } while (rs.next());
            }
            System.out.println();
            System.out.println("-------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }

    private void showSharedPosts3() {
        try {
            String sql = "select p.post_id, p.author_name, p.title, p.content, p.post_time " +
                    "from posts p join author_shared_posts asp on p.post_id = asp.post_id " +
                    "where asp.shared_author_name = ?;";
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("The posts you shared are:");
            System.out.println("-------------------------");
            printer.printPost(rs);
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }

    private void showFavoritePosts2() {
        try {
            String sql = "select p.post_id, p.author_name, p.title, p.content, p.post_time " +
                    "from posts p join post_favorites pf on p.post_id = pf.post_id " +
                    "where pf.favorite_author_name = ?;";
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("The posts you favorite are:");
            System.out.println("---------------------------");
            printer.printPost(rs);
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }

    private void showLikedPosts1() {
        try {
            String sql = "select p.post_id, p.author_name, p.title, p.content, p.post_time " +
                    "from posts p join author_liked_posts alp on p.post_id = alp.post_id " +
                    "where alp.liked_author_name = ?;";
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("The posts you liked are:");
            System.out.println("------------------------");
            printer.printPost(rs);
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }

    }

    private boolean isNum(String s) {
        Pattern pattern = Pattern.compile("^[-\\+]?\\d*$"); // match integers
        return pattern.matcher(s).matches();
    }

    private int readNum() {
        String s = in.next();
        if (!isNum(s)) {
            System.out.println("Invalid input, please input a number.");
            System.out.println("-------------------------------------");
            return -1;
        }
        return Integer.parseInt(s);
    }

    public void close() {
        try {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }
}
