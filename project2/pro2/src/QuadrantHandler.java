import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class QuadrantHandler {
    /**
     * 点赞、收藏、转发、关注、取关、屏蔽
     * 一键三连加关注!
     * 1.like 2.favorite 3.share 4.follow
     */

    private static Connection con;
    private static Scanner in;
    private static PreparedStatement stmt;

    public QuadrantHandler(Connection con, Scanner in) {
        QuadrantHandler.con = con;
        QuadrantHandler.in = in;
    }

    public void handleQuadrant() {
        System.out.println("Operation: [1]like a post\t[2]favorite a post\t[3]share a post");
        System.out.println("           [4]follow a user\t[5]unfollow a user\t[6]block a user\t[7]unblock a user");
        // current operation code
        int opcode = readNum();
        switch (opcode) {
            case 1 -> likePost1();
            case 2 -> favoritePost2();
            case 3 -> sharePost3();
            case 4 -> followUser();
            case 5 -> unfollowUser();
            case 6 -> blockUser();
            case 7 -> unblockUser();
            default -> {
                System.out.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
            }
        }
    }

    private void unblockUser() {
        System.out.println("Please input the user you want to unblock:");
        in.nextLine();
        String blockee = in.nextLine();
        try {
            String sql = "delete from block_user where author = ? and blocked_author = ?;";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, blockee);
            stmt.executeUpdate();
            System.out.println("You unblocked [ " + blockee + " ]");
            System.out.println("----------------------------------");
        } catch (SQLException e) {
            System.out.println("[ " + blockee + " ] is not in your block list.");
            System.out.println("------------------------------------");
            System.err.println("" + e.getMessage());
        }
    }

    private void blockUser() {
        System.out.println("Please input the user you want to block:");
        in.nextLine();
        String blockee = in.nextLine();
        try {
            String sql = "insert into block_user (author,blocked_author) values (?,?);";
            stmt = con.prepareStatement(sql);
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, blockee);
            stmt.executeUpdate();
            System.out.println("You blocked [ " + blockee + " ]");
            System.out.println("----------------------------------");
        } catch (SQLException e) {
            System.out.println("You have already blocked [ " + blockee + " ]");
            System.out.println("----------------------------------");
            System.err.println("" + e.getMessage());
        }
    }

    private void followUser() {
        System.out.println("Please input the user name you want to follow:");
        String name = in.next();
        if (!nameIsIn(name)) {
            System.out.println("User name not found, please input a valid user name.");
            System.out.println("----------------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into author_follow(author_name, followed_name)" +
                    "values (?, ?);");
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, name);
            stmt.executeUpdate();
            System.out.println("Follow successfully!");
            System.out.println("--------------------");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Follow failed, please try again.");
            System.out.println("--------------------------------");
        }
    }

    private void unfollowUser() {
        System.out.println("Please input the user name you want to unfollow:");
        String name = in.next();
        if (!nameIsIn(name)) {
            System.out.println("User name not found, please input a valid user name.");
            System.out.println("----------------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("delete from author_follow where author_name = ? and followed_name = ?;");
            stmt.setString(1, AccountHandler.getUser());
            stmt.setString(2, name);
            stmt.executeUpdate();
            System.out.println("Unfollow successfully!");
            System.out.println("----------------------");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("You did not follow [ " + name + " ]!");
            System.out.println("----------------------------------");
        }
    }

    private void sharePost3() {
        System.out.println("Please input the post ID you share:");
        int pid = readNum();
        if (!postIsIn(pid)) {
            System.out.println("loader.Post not found, please input a valid post ID.");
            System.out.println("----------------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into author_shared_posts(post_id, shared_author_name)" +
                    "values (?, ?);");
            stmt.setInt(1, pid);
            stmt.setString(2, AccountHandler.getUser());
            stmt.executeUpdate();
            System.out.println("Share successfully!");
            System.out.println("-------------------");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Share failed, please try again.");
            System.out.println("-------------------------------");
        }
    }

    private void favoritePost2() {
        System.out.println("Please input the post ID you favorite:");
        int pid = readNum();
        if (!postIsIn(pid)) {
            System.out.println("loader.Post not found, please input a valid post ID.");
            System.out.println("---------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into post_favorites(post_id, favorite_author_name)" +
                    "values (?, ?);");
            stmt.setInt(1, pid);
            stmt.setString(2, AccountHandler.getUser());
            stmt.executeUpdate();
            System.out.println("Favorite successfully!");
            System.out.println("----------------------");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Favorite failed, please try again.");
            System.out.println("----------------------------------");
        }

    }

    public void likePost1() {
        System.out.println("Please input the post ID you like:");
        int pid = readNum();
        if (!postIsIn(pid)) {
            System.out.println("loader.Post not found, please input a valid post ID.");
            System.out.println("---------------------------------------------");
            return;
        }
        try {
            stmt = con.prepareStatement("insert into author_liked_posts(post_id, liked_author_name)" +
                    "values (?, ?);");
            stmt.setInt(1, pid);
            stmt.setString(2, AccountHandler.getUser());
            stmt.executeUpdate();
            System.out.println("Like successfully!");
            System.out.println("------------------");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Like failed, please try again.");
            System.out.println("------------------------------");
        }
    }

    public boolean postIsIn(int pid) {
        try {
            stmt = con.prepareStatement("select * from posts where post_id = ?;");
            stmt.setInt(1, pid);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return true;// post is in
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("Query statement failed");
        }
        return false;
    }

    private boolean nameIsIn(String name) {
        try {
            stmt = con.prepareStatement("select * from authors where author_name = ?;");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return true;// name is in
        } catch (SQLException e) {
            System.err.println("Query statement failed");
            System.err.println(e.getMessage());
        }
        return false;
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