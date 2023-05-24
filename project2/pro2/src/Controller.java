import ConnectionUtil.DBUtil;
import Handler.*;

import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Controller {
    private static Connection con;
    private static Scanner in = new Scanner(System.in);
    private int opcode; // current operation code
    private DBUtil util;

    static ActionHandler actionHandler;
    static AccountHandler accountHandler;
    static BrowseHandler browseHandler;
    static ReplyHandler replyHandler;
    static MeHandler meHandler;
    static PostHandler postHandler;


    public Controller(DBUtil util) {
        this.util = util;
    }

    public void getConnection() {
        con = this.util.getConnection();
        System.out.println("------Thread " + Thread.currentThread().getId() + " visiting DB!------");
        System.out.println(this.util.getConnectState());
        meHandler = new MeHandler(con, in);
        replyHandler = new ReplyHandler(con, in);
        browseHandler = new BrowseHandler(con, in);
        accountHandler = new AccountHandler(con, in);
        actionHandler = new ActionHandler(con, in);
        postHandler = new PostHandler(con, in);
    }

    public void closeConnection() {
        this.util.closeConnection(con);
        closeAll();
        System.out.println("------Thread " + Thread.currentThread().getId() + " close DB!------");
    }

    public void closeAll() {
        accountHandler.close();
        actionHandler.close();
        browseHandler.close();
        replyHandler.close();
        meHandler.close();
        postHandler.close();
    }

    public void handle() {
        System.out.println("Welcome to SUSTech CS307 Forum!");
        while (true) {
            welcome(); // login/register/exit
            prompt();
            respond();
        }
    }

    /**
     * welcome, handle login and register
     */
    public void welcome() {
        accountHandler.handleAccount(); // login/register/exit
    }

    public void prompt() {
        System.out.println("Select the operations you want, enter the corresponding number:");
        // try to be hierarchy.
        System.out.println("[1] Browse posts"); // browse
        System.out.println("[2] like/favorite/share/(un)follow/(un)block"); // quadrant
        System.out.println("[3] Me"); // me
        System.out.println("[4] To post"); // post
        System.out.println("[5] Reply"); // reply
        System.out.println("[6] Show hot trend"); // show hot posts
        System.out.println("[7] Logout"); // logout
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        opcode = readNum(); // read operation code
    }

    public void respond()   {
        switch (opcode) {
            case 1:
                browseHandler.handleBrowse();
                break;
            case 2:
                actionHandler.handleQuadrant();
                break;
            case 3:
                meHandler.handleMe();
                break;
            case 4:
                postHandler.handlePost();
                break;
            case 5:
                replyHandler.handleReply();
                break;
            case 6:
                browseHandler.showHotSearchList();
                break;
            case 7:
                accountHandler.logout();
                break;
            default:
                System.out.println("Invalid input, please try again.");
                break;
        }
    }

    public boolean isNum(String s) {
        Pattern pattern = Pattern.compile("^[-\\+]?\\d*$"); // match integers
        return pattern.matcher(s).matches();
    }

    public int readNum() {
        String s = in.next();
        if (!isNum(s)) {
            System.out.println("Invalid input, please input a number.");
            System.out.println("-------------------------------------");
            return -1;
        }
        return Integer.parseInt(s);
    }


}

