package Handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class MeHandler implements Me{

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
        System.out.println("Operation: to show: ");
        System.out.println("[1]liked\t[2]favorite\t[3]shared");
        System.out.println("[4]following list\t[5]my posts\t[6]my replies");
        System.out.println("[7]blocking list\t");
        System.out.println("----------------------------------------------------------------------------");
        // current operation code
        int opcode = readNum();
        switch (opcode) {
            case 1:
                showLikedPosts();
                break;
            case 2:
                showFavoritePosts();
                break;
            case 3:
                showSharedPosts();
                break;
            case 4:
                showFollowingList();
                break;
            case 5:
                showMyPosts();
                break;
            case 6:
                showMyReplies();
                break;
            case 7:
                showBlockedUsers();
                break;
            default:
                System.err.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                break;
        }
    }

    public void showBlockedUsers() {
        try {
            String sql = "select * from show_blocked_list(?);";
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("Your blocked users are:");
            System.out.println("-----------------------");
            int cnt = 0;
            if (rs.next()) {// if there is result
                rs.first();// roll back to get the first one
                do {
                    System.out.print(rs.getString("blocked_author") + "\t");
                    if (++cnt % 5 == 0) System.out.println();
                } while (rs.next());
            }
            System.out.println();
            System.out.println("-------------------------------------------------------------------");
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }

    public void showMyReplies() {
        try {
//            String sql = "select * from posts p join first_replies fr on p.post_id = fr.post_id " +
//                    "left join second_replies sr on fr.first_id = sr.first_id " +
//                    "where fr.first_author = ? or sr.second_author = ?;";
            String sql = "select * from show_my_replies(?);";
            //left join in order to show the first reply even if there is no second reply
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setString(1, AccountHandler.getUser());
            rs = stmt.executeQuery();
            System.out.println("Your replies are:");
            System.out.println("-----------------");
            printer.printSecondReply(rs, true);
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
        }
    }

    public void showMyPosts() {
        try {
            String sql = "select * from show_my_posts(?);";
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

    public void showFollowingList() {
        try {
            String sql = "select * from show_following_list(?);";
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

    public void showSharedPosts() {
        try {
            String sql = "select * from show_shared_posts(?);";
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

    public void showFavoritePosts() {
        try {
            String sql = "select * from show_favorite_posts(?);";
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

    public void showLikedPosts() {
        try {
            String sql = "select * from show_liked_posts(?);";
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
