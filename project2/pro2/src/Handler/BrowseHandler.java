package Handler;

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
    private static Scanner in;
    private PreparedStatement stmt;
    private Printer printer;//print the result of posts
    private ResultSet rs;

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
            case 1:
                browsePost(1);
                break;
            case 2:
                browsePost(2);
                break;
            case 3:
                browsePost(3);
                break;
            default:
                System.err.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                break;
        }
    }

    /**
     * called when a user search for a keyword/author/category
     */
    private void update_hot_search_list(String searchContent) {
        String sql = "select * from update_hot_search_list(?);";
        try {
            stmt = con.prepareStatement(sql);
            stmt.setString(1, searchContent);
            stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    public void showHotSearchList() {
        String sql = "select * from get_hot_search_list;";
        //order by the content's frequency, show top 20.
        try {
            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery();
            System.out.println("The top 20 hot search list:");
            System.out.println("---------------------------");
            if (rs.next()) {
                rs.first();
                int rank = 1;
                System.out.println("Rank\tContent\t\tFrequency");
                do {
                    System.out.printf("%-5d\t%-10s\t%-10d\n", rank++, rs.getString("search_content"), rs.getInt("frequency"));
                } while (rs.next());
            } else System.out.println("No history.");
            System.out.println("------------------------------");
        } catch (SQLException e) {
            e.printStackTrace();
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
                System.err.println("Invalid, please input a valid number.");
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
        //multi-value search
        StringBuilder sql;
        ArrayList<String> params = new ArrayList<>();
        switch (opcode) {
            //functions in database, parameter is the username to find users been blocked
            case 1:
                sql = new StringBuilder("select * from get_posts(?) where 1=1");
                break;
            case 2:
                sql = new StringBuilder("select * from get_posts_first_replies(?) where 1=1");
                break;
            case 3:
                sql = new StringBuilder("select * from get_posts_second_replies(?) where 1=1");
                break;
            default:
                System.err.println("Invalid, please input a valid number.");
                System.out.println("-------------------------------------");
                return;
            //use left join to preserve posts without replies
        }
        params.add(AccountHandler.getUser());
        for (int i = 0; i < codes.length; i++) {
            switch (codes[i]) {
                case "1": // author_name
                    sql.append(" and author_name ilike ?");
                    update_hot_search_list(values[i]);
                    params.add(values[i]);
                    break;
                case "2":  // keyword
                    sql.append(" and (content ilike ? or title ilike ?)");
                    String t = "%" + values[i] + "%";
                    update_hot_search_list(values[i]);
                    params.add(t);
                    params.add(t);
                    break;
                case "3": // category
                    sql.append(" and category_name ilike ?");
                    update_hot_search_list(values[i]);
                    params.add(values[i]);
                    break;
                case "4": // from_time
                    sql.append(" and post_time >= to_date(?,'yyyy-mm-dd')");
                    params.add(values[i]);
                    break;
                case "5":  // to_time
                    sql.append(" and post_time <= to_date(?,'yyyy-mm-dd')");
                    params.add(values[i]);
                    break;
                case "6": { // reply_name
                    switch (opcode) {
                        case 1:
                            System.err.println("just posts, cannot search by reply_name");
                            System.out.println("-----------------------------------------");
                            return;
                        case 2:
                            sql.append(" and first_author ilike ?");
                            params.add(values[i]);
                            break;
                        case 3:
                            sql.append(" and (first_author ilike ? or second_author ilike ?)");
                            params.add(values[i]);
                            params.add(values[i]);
                            break;
                        default:
                            // if just posts, ignore this parameter
                            break;
                    }
                    break;
                }
                case "7":  // post_id
                    sql.append(" and post_id = ?");
                    params.add(values[i]);
                    break;
                default:  // invalid code
                    System.err.println("Invalid parameter code, please try again.");
                    System.out.println("-----------------------------------------");
                    return;
            }
        }
        switch (opcode) {
            case 1:
                sql.append(" order by post_id;");
                break;
            case 2:
                sql.append(" order by post_id, first_id;");
                break;
            case 3:
                sql.append(" order by post_id, first_id, second_id;");
                break;
            default:
                break;
        }
        try {
//            System.out.println(sql);
            stmt = con.prepareStatement(sql.toString(), ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);// enable rolling
            for (int i = 0; i < params.size(); i++) {
                stmt.setString(i + 1, params.get(i));
            }
            rs = stmt.executeQuery();
            switch (opcode) {
                case 1:
                    printer.printPost(rs);
                    break;
                case 2:
                    printer.printFirstReply(rs);
                    break;
                case 3:
                    printer.printSecondReply(rs, false);
                    break;
                default:
                    break;
            }
        } catch (
                SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("Search failed, please try again.");
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
            System.err.println("Invalid input, please input a number.");
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
            System.err.println("Search failed, please try again.");
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
            printer.printSecondReply(rs, false);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            System.err.println("Search failed, please try again.");
            System.out.println("--------------------------------");
        }

    }

    public void close() {
        try {
            if (stmt != null) stmt.close();
            if (rs != null) rs.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
