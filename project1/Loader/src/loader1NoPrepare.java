import com.alibaba.fastjson.JSON;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;
import java.util.Properties;

public class loader1NoPrepare {
    private static Connection con = null;
    private static Statement stmt = null;
    static List<Post> posts;
    static List<Replies> replies;
    static long cnt = 0;

    public static void main(String[] args) {
        Properties prop = loadDBUser();
        loadPostsFile();
        loadRepliesFile();
        // Empty target table
        openDB(prop);
        clearDataInTable();
        closeDB();

        long start = System.currentTimeMillis();
        openDB(prop);

        for (Post post : posts) {
            //here post is an object with all the attributes.
            loadPost(post);
        }
        for (Replies reply : replies) {
            //here reply is an object with all the attributes.
            loadReply(reply);
        }

        closeDB();
        long end = System.currentTimeMillis();
        System.out.println(cnt + " records successfully inserted.");
        System.out.println("Insertion speed : " + ((cnt) * 1000L) / (end - start) + " insertions'/s'");
        System.out.println("Time spent: " + (end - start) + "ms");
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
//                con.setAutoCommit(false);
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
            String jsonStrings = Files.readString(Path.of("resources/posts.json"));
            posts = JSON.parseArray(jsonStrings, Post.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void loadRepliesFile() {
        try {
            String jsonStrings = Files.readString(Path.of("resources/replies.json"));
            replies = JSON.parseArray(jsonStrings, Replies.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
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
                stmt0.executeUpdate("create table if not exists authors(\n" +
                        "author_id SERIAL\n," +
                        "author_name              text not null unique primary key," +
                        "author_registration_time TIMESTAMP," +
                        "author_phone             text," +
                        "author_id_card           text" +
                        ");");

                //posts
                stmt0.executeUpdate("drop table posts cascade;");
                stmt0.executeUpdate("create table if not exists posts(\n" +
                        "post_id SERIAL primary key," +
                        "title              text not null," +
                        "content            text not null," +
                        "post_time               TIMESTAMP," +
                        "post_city               text," +
                        "author_name text references authors (author_name) not null" +
                        ");");

                // categories
                stmt0.executeUpdate("drop table categories cascade;");
                stmt0.executeUpdate("create table if not exists categories(\n" +
                        "category_id SERIAL primary key," +
                        "category_name              text not null unique" +
                        ");");


                //post_category (relation table)
                stmt0.executeUpdate("drop table post_category cascade;");
                stmt0.executeUpdate("create table if not exists post_category(\n" +
                        "post_id INTEGER references posts (post_id) not null," +
                        "category_name text references categories (category_name) not null," +
                        "primary key (post_id,category_name)" +
                        ");");

                //author_followers (relation table)
                stmt0.executeUpdate("drop table author_follow cascade;");
                stmt0.executeUpdate("create table if not exists author_follow(\n" +
                        "author_name text references authors (author_name) not null," +
                        "followed_name text references authors (author_name) not null," +
                        "primary key (author_name,followed_name)" +
                        ");");

                //post_favorite (relation table)
                stmt0.executeUpdate("drop table post_favorites cascade;");
                stmt0.executeUpdate("create table if not exists post_favorites(\n" +
                        "post_id INTEGER references posts (post_id) not null," +
                        "favorite_author_name text references authors (author_name) not null," +
                        "primary key (post_id,favorite_author_name)" +
                        ");");

                //author_shared_posts (relation table)
                stmt0.executeUpdate("drop table author_shared_posts cascade;");
                stmt0.executeUpdate("create table if not exists author_shared_posts(\n" +
                        "post_id   INTEGER references posts (post_id) not null," +
                        "shared_author_name text references authors (author_name) not null," +
                        "primary key (post_id,shared_author_name)" +
                        ");");

                //author_liked_posts (relation table)
                stmt0.executeUpdate("drop table author_liked_posts cascade;");
                stmt0.executeUpdate("create table if not exists author_liked_posts(\n" +
                        "post_id   INTEGER references posts (post_id) not null," +
                        "liked_author_name text references authors (author_name) not null," +
                        "primary key (post_id,liked_author_name)" +
                        ");");

                //first_replies (entity table)
                stmt0.executeUpdate("drop table first_replies cascade;");
                stmt0.executeUpdate("create table if not exists first_replies(\n" +
                        "post_id   INTEGER references posts (post_id) not null," +
                        "first_id SERIAL primary key," +
                        "first_content            text not null," +
                        "first_stars              INTEGER," +
                        "first_author text references authors (author_name) not null," +
                        "unique (post_id,first_content,first_stars,first_author)" +
                        ");");

                //second_replies (entity table) many to one
                stmt0.executeUpdate("drop table second_replies cascade;");
                stmt0.executeUpdate("create table if not exists second_replies(\n" +
                        "first_id INTEGER references first_replies (first_id) not null," +
                        "second_id SERIAL primary key," +
                        "second_content            text not null," +
                        "second_stars              INTEGER," +
                        "second_author text references authors (author_name) not null" +
                        ");");


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
        Timestamp postingTime = post.getPostingTime();
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
                String sql1 = String.format("INSERT INTO public.authors (author_name, author_registration_time, author_phone,author_id_card) " +
                        "VALUES ('%s','%s','%s','%s') on conflict(author_name) do nothing;",
                        authorName, authorRegistrationTime.toString(), authorPhone, authorID);

                stmt = con.createStatement();
                stmt.execute(sql1);


                //authors in followed list
                for (String followedAuthor : authorsFollowedBy) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    String sql2 = String.format("INSERT INTO public.authors (author_name, author_registration_time) " +
                            "VALUES ('%s','%s') on conflict(author_name) do nothing;", followedAuthor, ts);
                    stmt = con.createStatement();
                    stmt.execute(sql2);
                    cnt++;
                }
                //authors in favorite list
                for (String favoriteAuthor : authorFavorite) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    String sql2 = String.format("INSERT INTO public.authors (author_name, author_registration_time) " +
                            "VALUES ('%s','%s') on conflict(author_name) do nothing;", favoriteAuthor, ts);
                    cnt++;

                    stmt = con.createStatement();
                    stmt.execute(sql2);
                }
                //authors in shared list
                for (String sharedAuthor : authorShared) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    String sql2 = String.format("INSERT INTO public.authors (author_name, author_registration_time) " +
                            "VALUES ('%s','%s') on conflict(author_name) do nothing;", sharedAuthor, ts);

                    stmt = con.createStatement();
                    stmt.execute(sql2);
                    cnt++;
                }
                //authors in liked list
                for (String likedAuthor : authorLiked) {
                    Timestamp ts = new Timestamp(System.currentTimeMillis());
                    cnt++;
                    String sql2 = String.format("INSERT INTO public.authors (author_name, author_registration_time) " +
                            "VALUES ('%s','%s') on conflict(author_name) do nothing;", likedAuthor, ts);

                    stmt = con.createStatement();
                    stmt.execute(sql2);
                }

                //load post info
                sql1 = String.format("insert into public.posts(post_id,title,content,post_time,post_city, author_name)" +
                        " values (%d,'%s','%s','%s','%s','%s') on conflict(post_id) do nothing;", postID, title, content, postingTime, postingCity, authorName);
                cnt++;
                stmt = con.createStatement();
                stmt.execute(sql1);

                //load category info
                for (String category1 : category) {
                    sql1 = String.format("insert into public.categories(category_name) values ('%s') on conflict(category_name) do nothing;", category1);
                    cnt++;
                    stmt = con.createStatement();
                    stmt.execute(sql1);
                }

                //load post_category relation table
                for (String category1 : category) {
                    cnt++;
                    sql1 = String.format("insert into public.post_category(post_id,category_name) values (%d,'%s') on conflict(post_id,category_name) do nothing;", postID, category1);
                    stmt = con.createStatement();
                    stmt.execute(sql1);
                }

                //load author_followers relation table
                for (String followedAuthor : authorsFollowedBy) {
                    sql1 = String.format("insert into public.author_follow(author_name,followed_name) " +
                            "values ('%s','%s') on conflict(author_name,followed_name) do nothing;", authorName, followedAuthor);

                    cnt++;
                    stmt = con.createStatement();
                    stmt.execute(sql1);
                }

                //load post_favorites relation table
                for (String favoriteAuthor : authorFavorite) {
                    cnt++;
                    sql1 = String.format("insert into public.post_favorites(post_id,author_name) " +
                            "values (%d,'%s') on conflict(post_id,author_name) do nothing;", postID, favoriteAuthor);
                    stmt = con.createStatement();
                    stmt.execute(sql1);
                }

                //load author_shared_posts relation table
                for (String sharedAuthor : authorShared) {
                    cnt++;
                    sql1 = String.format("insert into public.author_shared_posts(author_name,post_id) " +
                            "values ('%s',%d) on conflict(author_name,post_id) do nothing;", sharedAuthor, postID);
                    stmt = con.createStatement();
                    stmt.execute(sql1);
                }

                //load author_liked_posts relation table
                for (String likedAuthor : authorLiked) {
                    cnt++;
                    sql1 = String.format("insert into public.author_liked_posts(author_name,post_id) " +
                            "values ('%s',%d) on conflict(author_name,post_id) do nothing;", likedAuthor, postID);
                    stmt = con.createStatement();
                    stmt.execute(sql1);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
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
                //authors!!!!!
                String sql1 = String.format("insert into public.authors(author_name,author_registration_time) " +
                        "values ('%s','%s') on conflict(author_name) do nothing;", replyAuthor, new Timestamp(System.currentTimeMillis()));
                cnt++;
                stmt = con.createStatement();
                stmt.execute(sql1);

                sql1 = String.format("insert into public.authors(author_name,author_registration_time) " +
                        "values ('%s','%s') on conflict(author_name) do nothing;", secondaryReplyAuthor, new Timestamp(System.currentTimeMillis()));
                cnt++;
                stmt = con.createStatement();
                stmt.execute(sql1);

                //first reply
                sql1 = String.format("insert into public.first_replies(post_id, first_content, first_stars, first_author)" +
                        " values (%d,'%s',%d,'%s') on conflict(post_id) do nothing;", postID, replyContent, replyStars, replyAuthor);

                cnt++;
                stmt = con.createStatement();
                stmt.execute(sql1);

                //second reply
                sql1 = String.format("insert into public.second_replies(first_id, second_content, second_stars, second_author)" +
                        " values ((select first_id from first_replies " +
                        "where post_id = %d and first_author = '%s' and first_stars = %d and first_content = '%s'),'%s',%d,'%s') " +
                        "on conflict(first_id) do nothing;", postID, replyAuthor, replyStars, replyContent, secondaryReplyContent, secondaryReplyStars, secondaryReplyAuthor);

                cnt++;
                stmt = con.createStatement();
                stmt.execute(sql1);

            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
