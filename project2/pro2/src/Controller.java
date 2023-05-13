import java.sql.*;
import java.util.Scanner;
import java.util.regex.Pattern;

public class Controller {
    private static Connection con;
    private static Scanner in = new Scanner(System.in);
    private static int opcode; // current operation code

    static ActionHandler actionHandler;
    static AccountHandler accountHandler;
    static BrowseHandler browseHandler;
    static ReplyHandler replyHandler;
    static MeHandler meHandler;
    static PostHandler postHandler;


    public Controller(Connection con) {
        Controller.con = con;
        meHandler = new MeHandler(con, in);
        replyHandler = new ReplyHandler(con, in);
        browseHandler = new BrowseHandler(con, in);
        accountHandler = new AccountHandler(con, in);
        actionHandler = new ActionHandler(con, in);
        postHandler = new PostHandler(con, in);

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
        System.out.println("[6] logout"); // logout
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        opcode = readNum(); // read operation code
    }

    public void respond() {
        switch (opcode) {
            case 1 -> browseHandler.handleBrowse();
            case 2 -> actionHandler.handleQuadrant();
            case 3 -> meHandler.handleMe();
            case 4 -> postHandler.handlePost();
            case 5 -> replyHandler.handleReply();
            case 6 -> accountHandler.logout();
            default -> System.out.println("Invalid input, please try again.");
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

