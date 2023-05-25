package Handler;import java.sql.*;import java.util.Scanner;import java.util.regex.Pattern;public class PostHandler implements Post {    /**     * Post handler     * 1. create a post     * 2. delete a post     */    private static Connection con;    private static Scanner in;    private PreparedStatement stmt;    private ResultSet rs;    public PostHandler(Connection con, Scanner in) {        PostHandler.con = con;        PostHandler.in = in;    }    public void handlePost() {        System.out.println("Operation: [1]create a post\t[2]delete a post");        System.out.println("--------------------------------------------------------------");        // current operation code        int opcode = readNum();        switch (opcode) {            case 1:                createPost();                break;            case 2:                deletePost();                break;            default:                System.err.println("Invalid, please input a valid number.");                System.out.println("-------------------------------------");                break;        }    }    public void deletePost() {        System.out.println("Please enter the post ID you want to delete:");        System.out.println("--------------------------------------------");        int post_id = readNum();        try {            //delete from post_category first, then delete replies, finally delete post            String sql = "select delete_post(?,?);";            stmt = con.prepareStatement(sql);            stmt.setInt(1, post_id);            stmt.setString(2, AccountHandler.getUser());            rs = stmt.executeQuery();            if (!rs.next()) {                System.err.println("Failed to delete post, please check the post ID.");                System.out.println("------------------------------------------------");            } else {                System.out.println("Post deleted successfully!");                System.out.println("--------------------------");            }        } catch (SQLException e) {            System.err.println("Failed to delete post.");            System.out.println("----------------------");            System.err.println("" + e.getMessage());        }    }    public void createPost() {        System.out.println("Please enter the title of your post:");        System.out.println("------------------------------------");        in.nextLine();        String title = in.nextLine();        System.out.println("Please enter the content of your post:");        System.out.println("--------------------------------------");        String content = in.nextLine();        System.out.println("Please enter the tags of your post, separated by comma:");        System.out.println("-------------------------------------------------------");        String tags = in.nextLine();        String[] categories = tags.split(",");        try {            String sql = "select * from create_post(?,?,?,?);";            stmt = con.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);// get the post_id just created            stmt.setString(1, AccountHandler.getUser());            stmt.setString(2, title);            stmt.setString(3, content);            stmt.setString(4, "Shenzhen, China");            rs = stmt.executeQuery();            int post_id = 0; // get the post_id of the post just created            if (rs.next()) {                rs.first();                post_id = rs.getInt(1);            }            sql = "select update_category_and_post_category(?,?);";            stmt = con.prepareStatement(sql);            stmt.setInt(1, post_id);            stmt.setArray(2, con.createArrayOf("text", categories));            stmt.executeQuery();            System.out.println("Post created successfully!");            System.out.println("--------------------------");        } catch (SQLException e) {            System.err.println("Failed to create post.");            System.out.println("----------------------");            System.err.println("" + e.getMessage());        }    }    private boolean isNum(String s) {        Pattern pattern = Pattern.compile("^[-\\+]?\\d*$"); // match integers        return pattern.matcher(s).matches();    }    private int readNum() {        String s = in.next();        if (!isNum(s)) {            System.out.println("Invalid input, please input a number.");            System.out.println("-------------------------------------");            return -1;        }        return Integer.parseInt(s);    }    public void close() {        try {            if (rs != null) rs.close();            if (stmt != null) stmt.close();        } catch (SQLException e) {            System.err.println("Failed to close statement.");            System.out.println("--------------------------");            System.err.println("" + e.getMessage());        }    }}