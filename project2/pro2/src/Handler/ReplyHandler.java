package Handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class ReplyHandler {
    /**
     * Reply handler
     * 1. reply a post
     * 2. reply a reply
     */
    private static Connection con;
    private static Scanner in;
    private PreparedStatement stmt;


    public ReplyHandler(Connection con, Scanner in) {
        ReplyHandler.con = con;
        ReplyHandler.in = in;
    }

    public void handleReply() {
        System.out.println("Operation: [1]reply a post\t[2]reply a reply");
        System.out.println("--------------------------------------------------------------");
        // current operation code
        int opcode = readNum();
        System.out.println("Do you want to reply anonymously? [y/n]");
        System.out.println("---------------------------------------");
        String anonymous = in.next();
        boolean isAnonymous = anonymous.equals("y");
        switch (opcode) {
            case 1:
                replyPost(isAnonymous);
                break;
            case 2:
                replyReply(isAnonymous);
                break;
            default:
                System.err.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                break;
        }
    }

    private void replyReply(boolean isAnonymous) {
        System.out.println("Please enter the first_id you want to reply:");
        System.out.println("--------------------------------------------");
        int first_id = readNum();
        System.out.println("Please enter the content of your reply:");
        System.out.println("---------------------------------------");
        in.nextLine();
        String content = in.nextLine();
        try {
            String sql = "insert into second_replies (first_id, second_author, second_content,second_stars) values (?, ?, ?, 0);";
            doReply(first_id, content, sql, isAnonymous); // insert into second_replies
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
            System.err.println("Reply failed, please try again.");
            System.out.println("-------------------------------");
        }

    }

    private void replyPost(boolean isAnonymous) {
        System.out.println("Please enter the post_id you want to reply:");
        System.out.println("--------------------------------------------");
        int post_id = readNum();
        System.out.println("Please enter the content of your reply:");
        System.out.println("---------------------------------------");
        in.nextLine();
        String content = in.nextLine();
        try {
            String sql = "insert into first_replies (post_id, first_author, first_content,first_stars) values (?, ?, ?, 0);";
            doReply(post_id, content, sql, isAnonymous);// insert into first_replies
        } catch (SQLException e) {
            System.err.println("" + e.getMessage());
            System.err.println("Failed to reply.");
            System.out.println("----------------");
        }
    }

    private void doReply(int id, String content, String sql, boolean isAnonymous) throws SQLException {
        stmt = con.prepareStatement(sql);
        stmt.setInt(1, id);
        stmt.setString(2, isAnonymous ? "anonymous" : AccountHandler.getUser());
        stmt.setString(3, content);
        stmt.executeUpdate();
        System.out.println("Reply successfully!");
        System.out.println("-------------------");
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

    private boolean isNum(String s) {
        Pattern pattern = Pattern.compile("^[-\\+]?\\d*$"); // match integers
        return pattern.matcher(s).matches();
    }

    public void close() {
        try {
            if (stmt != null) stmt.close();
        } catch (SQLException e) {
            System.err.println("Close failed");
            System.err.println(e.getMessage());
        }
    }
}
