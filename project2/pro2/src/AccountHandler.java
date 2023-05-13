import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.regex.Pattern;

public class AccountHandler {
    /**
     * Account handler
     * 1. login
     * 2. register
     */
    private static Connection con;
    private static PreparedStatement stmt;
    private static Scanner in;

    private static String user; // current user name

    public static String getUser() {
        return user;
    }

    public AccountHandler(Connection con, Scanner in) {
        AccountHandler.con = con;
        AccountHandler.in = in;
    }

    public void handleAccount() {
        while (user == null) {
            System.out.println("Please input the corresponding number code to proceed:");
            System.out.println("Operation: [1]login\t[2]register\t[3]exit");
            System.out.println("---------------------------------------");
            int opcode = readNum();
            switch (opcode) {
                case 1 -> login();
                case 2 -> register();
                case 3 -> {
                    System.out.println("Bye!");
                    System.exit(0);
                }
                default -> {
                    System.out.println("Invalid, please input a valid number.");
                    System.out.println("-------------------------------------");
                }
            }
        }
    }

    public void logout() {
        System.out.println("See you next time [ " + user + " ]! :)");
        System.out.println("---------------------");
        user = null;
    } // public

    private void login() {
        System.out.println("Please input your user name:");
        String name = in.next();
        if (!nameIsIn(name)) {
            System.out.println("User name not found, please register first.");
            System.out.println("-------------------------------------------");
            return;
        }
        user = name;
        System.out.println("Login successfully! Welcome, [" + user + "]! :)");
        System.out.println("-----------------------------------------------");
    }

    /**
     * register a new user
     */
    private void register() {
        System.out.println("Please create an user name:");
        String name = in.next();
        if (nameIsIn(name)) {
            System.out.println("User name already exists, please choose another one.");
            System.out.println("----------------------------------------------------");
            return;
        }
        System.out.println("Please input your phone number:");
        String phone = in.next();
        if (!isNum(phone)) {
            System.out.println("Invalid phone number, only numbers are allowed.");
            System.out.println("-----------------------------------------------");
            return;
        }
        System.out.println("Please input your ID card number:");
        String id = in.next();
        try {
            stmt = con.prepareStatement("insert into authors(author_name, author_registration_time, author_phone, author_id_card)\n" +
                    "values (?, current_timestamp, ?,?) on duplicate key update nothing;");
            stmt.setString(1, name);
            stmt.setString(2, phone);
            stmt.setString(3, id);
            stmt.executeUpdate();
            System.out.println("Register successfully! Welcome, [" + name + "]! :)");
            System.out.println("--------------------------------------------");
            user = name;

        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("Query statement failed");
        }
    }

    /**
     * check if the name is in the authors table, return true if is in, false if not
     */
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
