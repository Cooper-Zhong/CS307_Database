package Handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ActionHandler implements Action {
    /**
     * 点赞、收藏、转发、关注、取关、屏蔽
     * 一键三连加关注
     * 1.like 2.favorite 3.share 4.follow
     */

    private static Connection con;
    private static Scanner in;
    private PreparedStatement stmt;
    private Printer printer;//print the result of posts

    private ResultSet rs;

    public ActionHandler(Connection con, Scanner in) {
        ActionHandler.con = con;
        ActionHandler.in = in;
        printer = new Printer();

    }

    public void handleActions() {
        System.out.println("Operation: post -> [1]like\t\t[2]favorite\t\t[3]share\t[4]一键三连");
        System.out.println("           user -> [5]follow\t[6]unfollow\t\t[7]block\t[8]unblock");
        // current operation code
        int opcode = readNum();
        switch (opcode) {
            case 1:
                likePost();
                break;
            case 2:
                favoritePost();
                break;
            case 3:
                sharePost();
                break;
            case 4:
                threeInOne();
                break;
            case 5:
                followUser();
                break;
            case 6:
                unfollowUser();
                break;
            case 7:
                blockUser();
                break;
            case 8:
                unblockUser();
                break;

            default:
                System.err.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                break;

        }
    }

    public void unblockUser() {
        System.out.println("Please input the user you want to unblock:");
        in.nextLine();
        String blockee = in.nextLine();
        try {
            String sql = "delete from block_user where author = ? and blocked_author = ?;";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, blockee);
            stmt.executeUpdate();
            System.out.println("You unblocked [ "+blockee+" ]");
            System.out.println("----------------------------------");
        } catch (SQLException e) {
            System.err.println("[ "+blockee+" ] is not in your block list.");
            System.out.println("------------------------------------");
            System.err.println(""+e.getMessage());
        }
    }

    public void blockUser() {
        System.out.println("Please input the user you want to block:");
        in.nextLine();
        String blockee = in.nextLine();
        try {
            String sql = "insert into block_user (author,blocked_author) values (?,?);";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, blockee);
            stmt.executeUpdate();
            System.out.println("You blocked [ "+blockee+" ]");
            System.out.println("----------------------------------");
        } catch (SQLException e) {
            System.err.println("You have already blocked [ "+blockee+" ]");
            System.out.println("----------------------------------");
            System.err.println(""+e.getMessage());
        }
    }

    public void followUser() {
        System.out.println("Please input the user name you want to follow:");
        String name = in.next();
        if (!nameIsIn(name)) {
            System.err.println("User name not found, please input a valid user name.");
            System.out.println("----------------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into author_follow(author_name, followed_name)"+
                    "values (?, ?);");
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, name);
            stmt.executeUpdate();
            System.out.println("You followed [ "+name+" ] successfully!");
            System.out.println("----------------------------");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("You have already followed [ "+name+" ]!");
            System.out.println("--------------------------------");
        }
    }

    public void unfollowUser() {
        System.out.println("Please input the user name you want to unfollow:");
        String name = in.next();
        if (!nameIsIn(name)) {
            System.err.println("User name not found, please input a valid user name.");
            System.out.println("----------------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("delete from author_follow where author_name = ? and followed_name = ?;");
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, name);
            stmt.executeUpdate();
            System.out.println("You unfollowed [ "+name+" ]");
            System.out.println("----------------------");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("You did not follow [ "+name+" ]!");
            System.out.println("----------------------------------");
        }
    }

    public void postFeedback(int pid) {
        String sql = "select * from posts where post_id = ?";
        try {
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.setInt(1, pid);
            rs = stmt.executeQuery();
            printer.printPost(rs);
        } catch (SQLException e) {
            System.err.println("You have already liked post [ "+pid+" ]");
            System.out.println("------------------------------------------");
            System.err.println(""+e.getMessage());
        }
    }

    public void threeInOne() {
        System.out.println("Please input the post id you want to 三连:");
        int pid = readNum();
        if (!postIsIn(pid)) {
            System.err.println("Post id not found, please input a valid post id.");
            System.out.println("-----------------------------------------------");
            return;
        }
        try {
            String sql = "select three_in_one(?,?);";
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, pid);
            stmt.setString(2, AccountHandler.getUser());
            stmt.executeQuery();
            System.out.println("You 成功三连 post [ "+pid+" ]");
            postFeedback(pid);
        } catch (SQLException e) {
            System.err.println("Failed to 三连 post [ "+pid+" ]");
            System.out.println("----------------------------------");
            System.err.println(""+e.getMessage());
        }
    }

    public void sharePost() {
        System.out.println("Please input the post ID you share:");
        int pid = readNum();
        if (!postIsIn(pid)) {
            System.out.println("Post not found, please input a valid post ID.");
            System.out.println("----------------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into author_shared_posts(post_id, shared_author_name)"+
                    "values (?, ?);");
            stmt.setInt(1, pid);
            stmt.setString(2, AccountHandler.getUser());
            stmt.executeUpdate();
            System.out.println("Share successfully:");
            postFeedback(pid);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("You have already shared this post!");
            System.out.println("-------------------------------");
        }
    }

    public void favoritePost() {
        System.out.println("Please input the post ID you favorite:");
        int pid = readNum();
        if (!postIsIn(pid)) {
            System.out.println("Post not found, please input a valid post ID.");
            System.out.println("---------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into post_favorites(post_id, favorite_author_name)"+
                    "values (?, ?);");
            stmt.setInt(1, pid);
            stmt.setString(2, AccountHandler.getUser());
            stmt.executeUpdate();
            System.out.println("Favorite successfully!");
            postFeedback(pid);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("You have already favorite this post!");
            System.out.println("----------------------------------");
        }

    }

    public void likePost() {
        System.out.println("Please input the post ID you like:");
        int pid = readNum();
        if (!postIsIn(pid)) {
            System.out.println("Post not found, please input a valid post ID.");
            System.out.println("---------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into author_liked_posts(post_id, liked_author_name)"+
                    "values (?, ?);");
            stmt.setInt(1, pid);
            stmt.setString(2, AccountHandler.getUser());
            stmt.executeUpdate();
            System.out.println("Like successfully!");
            postFeedback(pid);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("You have already liked this post!");
            System.out.println("------------------------------");
        }
    }

    public boolean postIsIn(int pid) {
        try {
            stmt = con.prepareStatement("select * from posts where post_id = ?;");
            stmt.setInt(1, pid);
            rs = stmt.executeQuery();
            if (rs.next()) return true;// post is in
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("The post does not exist.");
            System.out.println("------------------------");
        }
        return false;
    }

    public boolean nameIsIn(String name) {
        try {
            stmt = con.prepareStatement("select * from authors where author_name = ?;");
            stmt.setString(1, name);
            rs = stmt.executeQuery();
            if (rs.next()) return true;// name is in
        } catch (SQLException e) {
            System.err.println("Query statement failed");
            System.err.println(e.getMessage());
        }
        return false;
    }

    public void close() {
        try {
            if (rs!=null) rs.close();
            if (stmt!=null) stmt.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
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

}
