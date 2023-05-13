import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;

public class BrowseHandler {
    /**
     * Browse handler
     * 1. browse posts
     * 2. browse first replies
     * 3. browse second replies
     */
    private static Connection con;
    private static PreparedStatement stmt;
    private static Scanner in;
    private static Printer printer;//print the result of posts

    public BrowseHandler(Connection con, Scanner in) {
        BrowseHandler.con = con;
        BrowseHandler.in = in;
        printer = new Printer();
    }

    public void handleBrowse() {
        System.out.println("Operation: [1]just posts\t[2]with first replies\t[3]with second replies");
        System.out.println("------------------------------------------------------------------------------------");
        // current operation code
        int opcode = readNum();
        switch (opcode) {
            case 1 -> browsePost(1);
            case 2 -> browsePost(2);
            case 3 -> browsePost(3);
            default -> {
                System.out.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
            }
        }
    }

    public void browsePost(int opcode) {
        //multiple parameters
        System.out.println("Please input some parameters to browse posts:");
        System.out.println("You can enter the following parameters separated by \",\":");
        System.out.println("[1]author_name\t[2]keyword\t[3]category\t[4]from_date\t[5]to_date");
        System.out.println("[6]reply_name\t[7]post_id");
        System.out.println("-----------------------------------------------------------------");
        in.nextLine(); // clear the input buffer !!!!
        String s = in.nextLine();
        String[] codes = s.split(",");
        // check if the input is valid
        for (String code : codes) {
            if (!isNum(code)) {
                System.out.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                return;
            }
        }
        System.out.println("Please input the values of the parameters, separated by \",\":");
        System.out.println("For example: Cooper,CS.");
        String v = in.nextLine();
        String[] values = v.split(",");
        if (codes.length != values.length) {
            System.out.println("Number of parameters do not match, please try again.");
            System.out.println("----------------------------------------------------");
            return;
        }
        multiSearch(codes, values, opcode);
    }

    //browse with post_id

    /**
     * multi-value search for posts
     *
     * @param codes  the codes of the parameters
     * @param values the values of the parameters
     * @param opcode 1 print posts, 2 print first replies, 3 print second replies
     */
    public void multiSearch(String[] codes, String[] values, int opcode) {
        //get blocked user
//        String blocked = "select blocked_author from block where author = ?";
//        try {
//            stmt = con.prepareStatement(blocked, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            stmt.setString(1, AccountHandler.getUser());
//            ResultSet rs = stmt.executeQuery();
//            HashSet<String> blockedUsers = new HashSet<>();
//            if (rs.next()) {
//                rs.first();
//                do {
//                    blockedUsers.add(rs.getString("blocked_author"));
//                } while (rs.next());
//            }
//        } catch (SQLException e) {
//            System.err.println(e.getMessage());
//            return;
//        }
        //multi-value search
        StringBuilder sql;
        ArrayList<String> params = new ArrayList<>();
        switch (opcode) {
            //functions in database, parameter is the username to find users been blocked
            case 1 -> sql = new StringBuilder("select * from get_posts(?) where 1=1");
            case 2 -> sql = new StringBuilder("select * from get_first_replies(?) where 1=1");
            case 3 -> sql = new StringBuilder("select * from get_second_replies(?) where 1=1");
            default -> {
                System.out.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                return;
            }
            //use left join to preserve posts without replies
        }
        params.add(AccountHandler.getUser());
        for (int i = 0; i < codes.length; i++) {
            switch (codes[i]) {
                case "1" -> {// author_name
                    sql.append(" and author_name ilike ?");
                    params.add(values[i]);
                }
                case "2" -> { // keyword
                    sql.append(" and (content ilike ? or title ilike ?)");
                    String t = "%" + values[i] + "%";
                    params.add(t);
                    params.add(t);
                }
                case "3" -> { // category
                    sql.append(" and category_name = ?");
                    params.add(values[i]);
                }
                case "4" -> { // from_time
                    sql.append(" and post_time >= to_date(?,'yyyy-mm-dd')");
                    params.add(values[i]);
                }
                case "5" -> { // to_time
                    sql.append(" and post_time <= to_date(?,'yyyy-mm-dd')");
                    params.add(values[i]);
                }
                case "6" -> { // reply_name
                    sql.append(" and (first_author ilike ? or second_author ilike ?)");
                    params.add(values[i]);
                    params.add(values[i]);
                }
                case "7" -> { // post_id
                    sql.append(" and post_id = ?");
                    params.add(values[i]);
                }
                default -> { // invalid code
                    System.out.println("Invalid parameter code, please try again.");
                    System.out.println("-----------------------------------------");
                    return;
                }
            }
        }
        switch (opcode) {
            case 1 -> sql.append(" order by post_id;");
            case 2 -> sql.append(" order by post_id, first_id;");
            case 3 -> sql.append(" order by post_id, first_id, second_id;");
            default -> {
            }
        }
        try {
            System.out.println(sql);
            stmt = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);// enable rolling
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            switch (opcode) {
                case 1 -> printer.printPost(rs);
                case 2 -> printer.printFirstReply(rs);
                case 3 -> printer.printSecondReply(rs);
                default -> {
                }
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Search failed, please try again.");
            System.out.println("--------------------------------");
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

    private void browseFirstReplies() {
        System.out.println("Please enter the post_id you want to browse:");
        System.out.println("---------------------------------------------");
        int post_id = readNum();
        try {
            stmt = con.prepareStatement("select * from posts where post_id = ?;",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, post_id);
            ResultSet rs = stmt.executeQuery();
            printer.printPost(rs);
            System.out.println("The first replies:");
            stmt = con.prepareStatement("select * from first_replies where post_id = ?;",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, post_id);
            rs = stmt.executeQuery();
            printer.printFirstReply(rs);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Search failed, please try again.");
            System.out.println("--------------------------------");
        }
    }

    //browse with post_id
    private void browseAllReplies() {
        System.out.println("Please enter the post_id you want to browse:");
        System.out.println("---------------------------------------------");
        int post_id = readNum();
        try {
            stmt = con.prepareStatement("select * from posts where post_id = ?;", ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, post_id);
            ResultSet rs = stmt.executeQuery();
            printer.printPost(rs);
            stmt = con.prepareStatement("select * from first_replies fr left join second_replies sr on fr.first_id = sr.first_id " +
                    "where fr.post_id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            //left join to preserve first replies without second replies
            stmt.setInt(1, post_id);
            rs = stmt.executeQuery();
            printer.printSecondReply(rs);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Search failed, please try again.");
            System.out.println("--------------------------------");
        }

    }
}
