import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    public BrowseHandler(Connection con, Scanner in) {
        BrowseHandler.con = con;
        BrowseHandler.in = in;
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
        System.out.println("[6]reply_name");
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

    private void browseFirstReplies() {
        System.out.println("Please enter the post_id you want to browse:");
        System.out.println("---------------------------------------------");
        int post_id = readNum();
        try {
            stmt = con.prepareStatement("select * from posts where post_id = ?;",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, post_id);
            ResultSet rs = stmt.executeQuery();
            printPost(rs);
            System.out.println("The first replies:");
            stmt = con.prepareStatement("select * from first_replies where post_id = ?;",
                    ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, post_id);
            rs = stmt.executeQuery();
            printFirstReply(rs);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Search failed, please try again.");
            System.out.println("--------------------------------");
        }
    }

    private void browseAllReplies() {
        System.out.println("Please enter the post_id you want to browse:");
        System.out.println("---------------------------------------------");
        int post_id = readNum();
        try {
            stmt = con.prepareStatement("select * from posts where post_id = ?;", ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, post_id);
            ResultSet rs = stmt.executeQuery();
            printPost(rs);
            stmt = con.prepareStatement("select * from first_replies fr join second_replies sr on fr.first_id = sr.first_id " +
                    "where fr.post_id = ?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setInt(1, post_id);
            rs = stmt.executeQuery();
            printSecondReply(rs);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Search failed, please try again.");
            System.out.println("--------------------------------");
        }

    }


    /**
     * multi-value search for posts
     *
     * @param codes  the codes of the parameters
     * @param values the values of the parameters
     * @param opcode 1 print posts, 2 print first replies, 3 print second replies
     */
    public void multiSearch(String[] codes, String[] values, int opcode) {
        StringBuilder sql = new StringBuilder("select * " +
                "from posts p join post_category pc on p.post_id = pc.post_id " + "left join first_replies fr on p.post_id = fr.post_id " +
                "left join second_replies sr on fr.first_id = sr.first_id " +
                "where 1=1");
        //use left join to preserve posts without replies
        //avoid duplicate caused by multiple categories
        ArrayList<String> params = new ArrayList<>();
        boolean flag = false;//if the input is valid
        for (int i = 0; i < codes.length; i++) {
            switch (codes[i]) {
                case "1" -> {// author_name
                    sql.append(" and p.author_name ilike ?");
                    params.add(values[i]);
                }
                case "2" -> { // keyword
                    sql.append(" and (p.content ilike ? or p.title ilike ?)");
                    String t = "%" + values[i] + "%";
                    params.add(t);
                    params.add(t);
                }
                case "3" -> { // category
                    sql.append(" and pc.category_name = ?");
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
                    sql.append(" and (fr.first_author ilike ? or sr.second_author ilike ?)");
                    params.add(values[i]);
                    params.add(values[i]);
                }
                default -> { // invalid code
                    flag = true;
                    System.out.println("Invalid parameter code, please try again.");
                    System.out.println("-----------------------------------------");
                }
            }
        }
        if (flag) {
            System.out.println("Invalid input, please try again.");
            System.out.println("--------------------------------");
            return;
        }
        sql.append(" order by p.post_id, fr.first_id, sr.second_id;");
        try {
            System.out.println(sql);
            stmt = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);// enable rolling
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            switch (opcode) {
                case 1 -> printPost(rs);
                case 2 -> printFirstReply(rs);
                case 3 -> printSecondReply(rs);
                default -> {
                }
            }
            return;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.out.println("Search failed, please try again.");
            System.out.println("--------------------------------");
        }
    }

    private void printSecondReply(ResultSet rs) {
        try {
            int post_id = 0;
            int first_id = 0;
            int second_id = 0;
            if (rs.next()) {
                rs.first();
                do {
                    int cur_post_id = rs.getInt("post_id");
                    if (cur_post_id != post_id) {// new post
                        post_id = cur_post_id;
                        System.out.println("[ post_id ]: " + post_id);
                        System.out.println("[ author_name ]: " + rs.getString("author_name"));
                        System.out.println("[ title ]: " + rs.getString("title"));
                        System.out.println("[ content ]: " + rs.getString("content"));
                        System.out.println("[ post_time ]: " + rs.getDate("post_time"));
                        System.out.println("--------------------------------------------------------------------------------------------------------------------------------------");
                    }//then print first reply
                    int cur_first_id = rs.getInt("first_id");
                    if (cur_first_id != first_id && rs.getString("first_author") != null ) {// new first reply
                        first_id = cur_first_id;
                        System.out.println("[ first reply id ]: " + first_id);
                        System.out.println("[ first content ]: " + rs.getString("first_content"));
                        System.out.println("[ first author ]: " + rs.getString("first_author"));
                        System.out.println("----------------------------------------------------------------------------------");
                    }
                    //then print second reply
                    int cur_second_id = rs.getInt("second_id");
                    if (cur_second_id != second_id&& rs.getString("second_author") != null) {// new second reply
                        second_id = cur_second_id;
                        System.out.println("[ second reply id ]: " + rs.getInt("second_id"));
                        System.out.println("[ second content ]: " + rs.getString("second_content"));
                        System.out.println("[ second author ]: " + rs.getString("second_author"));
                        System.out.println("----------------------------------");
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

    private void printFirstReply(ResultSet rs) {
        try {
            int post_id = 0;
            int first_id = 0;
            if (rs.next()) {
                rs.first();
                do {// print post first
                    int cur_post_id = rs.getInt("post_id");
                    if (cur_post_id != post_id) {// new post
                        post_id = cur_post_id;
                        System.out.println("---------------------------------------------------------------------------------------------------------------------------------------------------------");
                        System.out.println("[ post_id ]: " + post_id);
                        System.out.println("[ author_name ]: " + rs.getString("author_name"));
                        System.out.println("[ title ]: " + rs.getString("title"));
                        System.out.println("[ content ]: " + rs.getString("content"));
                        System.out.println("[ post_time ]: " + rs.getDate("post_time"));
                        System.out.println("-------------------------------------------------------------------------------------------------");
                    }//then print first reply
                    int cur_first_id = rs.getInt("first_id");
                    if (cur_first_id != first_id && rs.getString("first_author") != null) {// new first reply
                        first_id = cur_first_id;
                        System.out.println("[ first_id ]: " + rs.getInt("first_id"));
                        System.out.println("[ first_stars ]: " + rs.getInt("first_stars"));
                        System.out.println("[ first_author ]: " + rs.getString("first_author"));
                        System.out.println("[ first_content ]: " + rs.getString("first_content"));
                        System.out.println("------------------------------------------------");
                    }

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

    private void printPost(ResultSet rs) throws SQLException {
        if (rs.next()) {// if there is result
            rs.first();// roll back to the first one
            int post_id = 0;
            do {
                int cur_post_id = rs.getInt("post_id");
                if (cur_post_id != post_id) {// new post
                    post_id = cur_post_id;
                    System.out.println("[ Post ID ]: " + rs.getInt("post_id"));
                    System.out.println("[ Title ]: " + rs.getString("title"));
                    System.out.println("[ Author ]: " + rs.getString("author_name"));
                    System.out.println("[ Content ]: " + rs.getString("content"));
                    System.out.println("[ Post time ]: " + rs.getTimestamp("post_time"));
                    System.out.println("-------------------------------------------------------------------------------------------------------------------");
                }

            } while (rs.next());
        } else {
            System.out.println("No post are found.");
            System.out.println("-------------------------------------------------------------------");
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
