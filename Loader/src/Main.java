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
    private static PreparedStatement stmt11 = null;
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

        int cnt1 = 0;
        int cnt2 = 0;
        long start = System.currentTimeMillis();
        openDB(prop);

        prepareStatement();
        for (Post post : posts) {
            //here post is an object with all the attributes.
            loadPost(post);
            cnt1++;
//            if (cnt % 1000 == 0) {
//                System.out.println("insert " + 1000 + " data successfully!");
//            }
        }

        for (Replies reply : replies) {
            //here reply is an object with all the attributes.
            loadReply(reply);
            cnt2++;
//            if (cnt2 % 1000 == 0) {
//                System.out.println("insert " + 1000 + " data successfully!");
//            }
        }

        try {
            con.commit();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
        closeDB();
        long end = System.currentTimeMillis();
        System.out.println(cnt1 + cnt2 + " records successfully loaded");
        System.out.println("Loading speed : " + ((cnt1 + cnt2) * 1000L) / (end - start) + " records/s");
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

            stmt4 = con.prepareStatement("insert into public.post_category(post_id,category_id)" +
                    " values (?,(select categories.category_id from categories where category_name = ?)) " +
                    "on conflict(post_id,category_id) do nothing;");

            stmt5 = con.prepareStatement("insert into public.author_followers(author_name, follower_name)" +
                    " values (?,?) on conflict(author_name,follower_name) do nothing;");

            stmt6 = con.prepareStatement("insert into public.post_favorites(post_id, favorite_author_name)" +
                    " values (?,?) on conflict(post_id,favorite_author_name) do nothing;");

            stmt7 = con.prepareStatement("insert into public.author_shared_posts(post_id, shared_author_name)" +
                    " values (?,?) on conflict(post_id,shared_author_name) do nothing;");

            stmt8 = con.prepareStatement("insert into public.author_liked_posts(post_id, liked_author_name)" +
                    " values (?,?) on conflict(post_id,liked_author_name) do nothing;");

            stmt9 = con.prepareStatement("insert into public.first_replies(post_id, first_content, first_time, first_author)" +
                    " values (?,?,?,?);");
            // first_id is serial number, not included in the insert statement

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

                //authors
                stmt0.executeUpdate("drop table authors cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists authors(\n" +
                        "author_id SERIAL\n," +
                        "author_name              text not null unique primary key," +
                        "author_registration_time TIMESTAMP," +
                        "author_phone             text," +
                        "author_id_card           text" +
                        ");");
                con.commit();

                //posts
                stmt0.executeUpdate("drop table posts cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists posts(\n" +
                        "post_id SERIAL primary key," +
                        "title              text not null," +
                        "content            text not null," +
                        "post_time               TIMESTAMP," +
                        "post_city               text," +
                        "author_name text references authors (author_name) not null" +
                        ");");
                con.commit();

                // categories
                stmt0.executeUpdate("drop table categories cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists categories(\n" +
                        "category_id SERIAL primary key," +
                        "category_name              text not null unique" +
                        ");");
                con.commit();

                //post_category (relation table)
                stmt0.executeUpdate("drop table post_category cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists post_category(\n" +
                        "post_id INTEGER references posts (post_id) not null," +
                        "category_id INTEGER references categories (category_id) not null," +
                        "primary key (post_id,category_id)" +
                        ");");
                con.commit();

                //author_followers (relation table)
                stmt0.executeUpdate("drop table author_followers cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists author_followers(\n" +
                        "author_name text references authors (author_name) not null," +
                        "follower_name text references authors (author_name) not null," +
                        "primary key (author_name,follower_name)" +
                        ");");
                con.commit();

                //post_favorite (relation table)
                stmt0.executeUpdate("drop table post_favorites cascade;");
                con.commit();

                stmt0.executeUpdate("create table if not exists post_favorites(\n" +
                        "post_id INTEGER references posts (post_id) not null," +
                        "favorite_author_name text references authors (author_name) not null," +
                        "primary key (post_id,favorite_author_name)" +
                        ");");
                con.commit();

                //author_shared_posts (relation table)
                stmt0.executeUpdate("drop table author_shared_posts cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists author_shared_posts(\n" +
                        "post_id   INTEGER references posts (post_id) not null," +
                        "shared_author_name text references authors (author_name) not null," +
                        "primary key (post_id,shared_author_name)" +
                        ");");
                con.commit();

                //author_liked_posts (relation table)
                stmt0.executeUpdate("drop table author_liked_posts cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists author_liked_posts(\n" +
                        "post_id   INTEGER references posts (post_id) not null," +
                        "liked_author_name text references authors (author_name) not null," +
                        "primary key (post_id,liked_author_name)" +
                        ");");
                con.commit();

                //first_replies (entity table)
                stmt0.executeUpdate("drop table first_replies cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists first_replies(\n" +
                        "first_id SERIAL primary key," +
                        "post_id   INTEGER references posts (post_id) not null," +
                        "first_content            text not null," +
                        "first_stars              INTEGER," +
                        "first_author text references authors (author_name) not null" +
                        ");");
                con.commit();

                //second_replies (entity table)
                stmt0.executeUpdate("drop table second_replies cascade;");
                con.commit();
                stmt0.executeUpdate("create table if not exists second_replies(\n" +
                        "second_id SERIAL primary key," +
                        "first_id   INTEGER references first_replies (first_id) not null," +
                        "second_content            text not null," +
                        "second_stars              INTEGER," +
                        "second_author text references authors (author_name) not null" +
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
        Timestamp authorRegistrationTime = post.getAuthorRegistrationTime();
        String authorID = post.getAuthorID();//id card number
        String authorPhone = post.getAuthorPhone();
        List<String> authorsFollowedBy = post.getAuthorsFollowedBy();
        List<String> authorFavorite = post.getAuthorFavorite();
        List<String> authorShared = post.getAuthorShared();
        List<String> authorLiked = post.getAuthorLiked();

        if (con != null) {
            try {
                //pass in attributes
                //post authors
                stmt.setString(1, authorName);
                stmt.setTimestamp(2, authorRegistrationTime);
                stmt.setString(3, authorPhone);
                stmt.setString(4, authorID);
                stmt.executeUpdate();

                //authors in followed list
                for (String followedAuthor : authorsFollowedBy) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    stmt1.setString(1, followedAuthor);
                    stmt1.setTimestamp(2, ts);
                    stmt1.executeUpdate();
                }
                //authors in favorite list
                for (String favoriteAuthor : authorFavorite) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    stmt1.setString(1, favoriteAuthor);
                    stmt1.setTimestamp(2, ts);
                    stmt1.executeUpdate();
                }
                //authors in shared list
                for (String sharedAuthor : authorShared) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    stmt1.setString(1, sharedAuthor);
                    stmt1.setTimestamp(2, ts);
                    stmt1.executeUpdate();
                }
                //authors in liked list
                for (String likedAuthor : authorLiked) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    stmt1.setString(1, likedAuthor);
                    stmt1.setTimestamp(2, ts);
                    stmt1.executeUpdate();
                }

                //load post info
                stmt2.setInt(1, postID);
                stmt2.setString(2, title);
                stmt2.setString(3, content);
                stmt2.setTimestamp(4, Timestamp.valueOf(postingTime));
                stmt2.setString(5, postingCity);
                stmt2.setString(6, authorName);
                stmt2.executeUpdate();

                //load category info
                for (String category1 : category) {
                    stmt3.setString(1, category1);
                    stmt3.executeUpdate();
                }

                //load post_category relation table
                for (String category1 : category) {
                    stmt4.setInt(1, postID);
                    stmt4.setString(2, category1);
                    stmt4.executeUpdate();
                }

                //load author_followers relation table
                for (String followedAuthor : authorsFollowedBy) {
                    stmt5.setString(1, authorName);
                    stmt5.setString(2, followedAuthor);
                    stmt5.executeUpdate();
                }

                //load post_favorites relation table
                for (String favoriteAuthor : authorFavorite) {
                    stmt6.setInt(1, postID);
                    stmt6.setString(2, favoriteAuthor);
                    stmt6.executeUpdate();
                }

                //load author_shared_posts relation table
                for (String sharedAuthor : authorShared) {
                    stmt7.setInt(1, postID);
                    stmt7.setString(2, sharedAuthor);
                    stmt7.executeUpdate();
                }

                //load author_liked_posts relation table
                for (String likedAuthor : authorLiked) {
                    stmt8.setInt(1, postID);
                    stmt8.setString(2, likedAuthor);
                    stmt8.executeUpdate();
                }


//                stmt.addBatch();
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
                //first reply
                stmt9.setInt(1, postID);
                stmt9.setString(2, replyContent);
                stmt9.setInt(3, replyStars);
                stmt9.setString(4, replyAuthor);
                stmt9.executeUpdate();

                //second reply
                stmt10.setString(1, secondaryReplyContent);
                stmt10.setInt(2, secondaryReplyStars);
                stmt10.setString(3, secondaryReplyAuthor);
                stmt10.executeUpdate();


            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }


}