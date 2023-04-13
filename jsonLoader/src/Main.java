import com.alibaba.fastjson.JSON;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class Main {
    private static Connection con = null;
    private static PreparedStatement stmt = null;
    static List<Post> posts;
    static List<Replies> replies;

    public static void main(String[] args) {
        Properties prop = loadDBUser();
        loadPostsFile();
        loadRepliesFile();
        // Empty target table
        openDB(prop);
        clearDataInTable();
        closeDB();

        int cnt = 0;

        long start = System.currentTimeMillis();
        openDB(prop);
        setPrepareStatement();
        for (Post post : posts) {
            //here post is an object with all the attributes.
            loadPost(post);
            cnt++;
            if (cnt % 1000 == 0) {
                System.out.println("insert " + 1000 + " data successfully!");
            }
        }
        for (Replies reply : replies) {
            //here reply is an object with all the attributes.
            loadReply(reply);
            cnt++;
            if (cnt % 1000 == 0) {
                System.out.println("insert " + 1000 + " data successfully!");
            }
        }

        try {
            con.commit();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        closeDB();
        long end = System.currentTimeMillis();
        System.out.println(cnt + " records successfully loaded");
        System.out.println("Loading speed : " + (cnt * 1000L) / (end - start) + " records/s");
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
                con.setAutoCommit(false);
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

    private static void loadPostsFile() {
        try {
            String jsonStrings = Files.readString(Path.of("posts.json"));
            posts = JSON.parseArray(jsonStrings, Post.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadRepliesFile() {
        try {
            String jsonStrings = Files.readString(Path.of("replies.json"));
            replies = JSON.parseArray(jsonStrings, Replies.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     * rewrite the insert statement later!
     */
    public static void setPrepareStatement() {
        try {
            stmt = con.prepareStatement("INSERT INTO public.movies (movieid, title, country, year_released, runtime) " +
                    "VALUES (?,?,?,?,?);");
        } catch (SQLException e) {
            System.err.println("Insert statement failed");
            System.err.println(e.getMessage());
            closeDB();
            System.exit(1);
        }
    }

    /**
     * clear data in table before import each time, to compare the speed of different import methods.
     */
    public static void clearDataInTable() {
        Statement stmt0;
        if (con != null) {
            try {//rewrite later
                stmt0 = con.createStatement();
                stmt0.executeUpdate("drop table movies;");
                con.commit();
                stmt0.executeUpdate("create table if not exists movies(\n" +
                        "movieid serial not null\n" +
                        "constraint movies_pkey\n" +
                        "primary key,\n" +
                        "title varchar(200) not null,\n" +
                        "country char(2) not null\n" +
                        "constraint movies_country_fkey\n" +
                        "references countries,\n" +
                        "year_released integer not null,\n" +
                        "runtime integer,\n" +
                        "constraint movies_title_country_year_released_key\n" +
                        "unique (title, country, year_released)\n" +
                        ");");
                con.commit();
                stmt0.close();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * Process one post/reply object
     *
     * @param post
     */
    private static void loadPost(Post post) {
        //access info through getter method.
        int postID = post.getPostID();
        String title = post.getTitle();
        List<String> category = post.getCategory();
        String content = post.getContent();
        String postingTime = post.getPostingTime();
        String postingCity = post.getPostingCity();
        String authorName = post.getAuthorName();
        String authorRegistrationTime = post.getAuthorRegistrationTime();
        String authorID = post.getAuthorID();
        String authorPhone = post.getAuthorPhone();
        List<String> authorsFollowedBy = post.getAuthorsFollowedBy();
        List<String> authorFavorite = post.getAuthorFavorite();
        List<String> authorShared = post.getAuthorShared();
        List<String> authorLiked = post.getAuthorLiked();

        if (con != null) {
            try {
                //pass in attributes
//                stmt.setInt(1, Integer.parseInt(lineData[0]));
//                stmt.setString(2, lineData[1]);
//                stmt.setString(3, lineData[2]);
//                stmt.setInt(4, Integer.parseInt(lineData[3]));
//                stmt.setInt(5, Integer.parseInt(lineData[4]));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private static void loadReply(Replies reply) {
        int postID = reply.getPostID();
        String replyContent = reply.getReplyContent();
        int replyStars = reply.getReplyStars();
        String replyAuthor = reply.getReplyAuthor();
        String secondaryReplyContent = reply.getSecondaryReplyContent();
        int secondaryReplyStars = reply.getSecondaryReplyStars();
        String secondaryReplyAuthor = reply.getSecondaryReplyAuthor();
        if (con != null) {
            try {
                //pass in attributes
//                stmt.setInt(1, Integer.parseInt(lineData[0]));
//                stmt.setString(2, lineData[1]);
//                stmt.setString(3, lineData[2]);
//                stmt.setInt(4, Integer.parseInt(lineData[3]));
//                stmt.setInt(5, Integer.parseInt(lineData[4]));
                stmt.executeUpdate();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


}
