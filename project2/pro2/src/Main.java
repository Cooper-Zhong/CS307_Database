
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class Main {
    //效率：一条一条传输，还是一次性传输BatchSize条
    private static final int BATCH_SIZE = 2000;
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    private static PreparedStatement stmt1 = null;
    private static PreparedStatement stmt2 = null;
    private static PreparedStatement stmt3 = null;
    private static PreparedStatement stmt4 = null;
    private static PreparedStatement stmt5 = null;
    private static PreparedStatement stmt6 = null;
    private static PreparedStatement stmt7 = null;
    private static PreparedStatement stmt8 = null;
    private static PreparedStatement stmt9 = null;
    private static PreparedStatement stmt10 = null;
    static long cnt = 0;

    public static void main(String[] args) {
        Properties prop = loadDBUser();

        openDB(prop);


        closeDB();
    }


    private static void openDB(Properties prop) {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (Exception e) {
            System.err.println("Cannot find the Postgres driver. Check CLASSPATH.");
            System.exit(1);
        }
        String url = "jdbc:postgresql://" + prop.getProperty("host") + "/" + prop.getProperty("database");
        try {
            con = DriverManager.getConnection(url, prop);
            if (con != null) {
                System.out.println("Successfully connected to the database "
                        + prop.getProperty("database") + " as " + prop.getProperty("user"));
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed");
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    private static void closeDB() {
        if (con != null) {
            try {
                if (stmt != null) {
                    stmt.close();
                }
                con.close();
                con = null;
            } catch (Exception ignored) {
            }
        }
    }

    private static Properties loadDBUser() {
        Properties properties = new Properties();
        try {
            properties.load(new InputStreamReader(new FileInputStream("resources/dbUser.properties")));
            return properties;
        } catch (IOException e) {
            System.err.println("can not find db user file");
            throw new RuntimeException(e);
        }
    }


    public static void prepareStatement() {// authors in one post json
        try {
            stmt = con.prepareStatement("INSERT INTO public.authors (author_name, author_registration_time, author_phone,author_id_card) " +
                    "VALUES (?,?,?,?) on conflict(author_name) do nothing;");
            //the first serial number is not included in the insert statement.
            //if duplicate, do nothing

            stmt1 = con.prepareStatement("INSERT INTO public.authors (author_name, author_registration_time) " +
                    "VALUES (?,?) on conflict(author_name) do nothing;");
            //the first serial number is not included in the insert statement.
            // phone and id card are null,registration time is randomly generated

            stmt2 = con.prepareStatement("insert into public.posts(post_id,title,content,post_time,post_city, author_name)" +
                    " values (?,?,?,?,?,?) on conflict(post_id) do nothing;");

            stmt3 = con.prepareStatement("insert into public.categories(category_name)" +
                    " values (?) on conflict(category_name) do nothing;");

            stmt4 = con.prepareStatement("insert into public.post_category(post_id,category_name)" +
                    " values (?,?) on conflict(post_id,category_name) do nothing;");
            //the first serial number is not included in the insert statement.

            stmt5 = con.prepareStatement("insert into public.author_follow(author_name, followed_name)" +
                    " values (?,?) on conflict(author_name,followed_name) do nothing;");

            stmt6 = con.prepareStatement("insert into public.post_favorites(post_id, favorite_author_name)" +
                    " values (?,?) on conflict(post_id,favorite_author_name) do nothing;");

            stmt7 = con.prepareStatement("insert into public.author_shared_posts(post_id, shared_author_name)" +
                    " values (?,?) on conflict(post_id,shared_author_name) do nothing;");

            stmt8 = con.prepareStatement("insert into public.author_liked_posts(post_id, liked_author_name)" +
                    " values (?,?) on conflict(post_id,liked_author_name) do nothing;");

            stmt9 = con.prepareStatement("insert into public.first_replies(post_id, first_content, first_stars, first_author)" +
                    " values (?,?,?,?) on conflict(post_id,first_author,first_stars,first_content) do nothing;");
            // first_id is serial number, not included in the insert statement

            stmt10 = con.prepareStatement("insert into public.second_replies(first_id, second_content, second_stars, second_author)" +
                    " values ((select first_id from first_replies where post_id = ? and first_author = ? and first_stars = ? and first_content = ?),?,?,?);");
            // second_id is serial number, not included in the insert statement

        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }


}
