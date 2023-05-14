package Loader;

import java.sql.Timestamp;
import java.util.List;

public class Post {
    private int postID;
    private String title;
    private List<String> category;
    private String content;
    private Timestamp postingTime;
    private String postingCity;
    private String authorName;
    private Timestamp authorRegistrationTime;
    private String authorID;//author id card number
    private String authorPhone;
    private List<String> authorsFollowedBy;
    private List<String> authorFavorite;
    private List<String> authorShared;
    private List<String> authorLiked;
    @Override
    public String toString() {
        return "loader.Post{" +
                "postID=" + postID +
                ", title='" + title + '\'' +
                ", category=" + category +
                ", content='" + content + '\'' +
                ", postingTime='" + postingTime + '\'' +
                ", postingCity='" + postingCity + '\'' +
                ", Author='" + authorName + '\'' +
                ", authorRegistrationTime='" + authorRegistrationTime + '\'' +
                ", authorID='" + authorID + '\'' +
                ", authorPhone='" + authorPhone + '\'' +
                ", authorFollowedBy=" + authorsFollowedBy +
                ", authorFavorite=" + authorFavorite +
                ", authorShared=" + authorShared +
                ", authorLiked=" + authorLiked +
                '}';
    }

    public int getPostID() {
        return postID;
    }

    public void setPostID(int postID) {
        this.postID = postID;
    }

    public void setPostingTime(Timestamp postingTime) {
        this.postingTime = postingTime;
    }

    public Timestamp getPostingTime() {
        return postingTime;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getCategory() {
        return category;
    }

    public void setCategory(List<String> category) {
        this.category = category;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


    public String getPostingCity() {
        return postingCity;
    }

    public void setPostingCity(String postingCity) {
        this.postingCity = postingCity;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public Timestamp getAuthorRegistrationTime() {
        return authorRegistrationTime;
    }

    public void setAuthorRegistrationTime(Timestamp authorRegistrationTime) {
        this.authorRegistrationTime = authorRegistrationTime;
    }

    public String getAuthorID() {
        return authorID;
    }

    public void setAuthorID(String authorID) {
        this.authorID = authorID;
    }

    public String getAuthorPhone() {
        return authorPhone;
    }

    public void setAuthorPhone(String authorPhone) {
        this.authorPhone = authorPhone;
    }

    public List<String> getAuthorsFollowedBy() {
        return authorsFollowedBy;
    }

    public void setAuthorsFollowedBy(List<String> authorsFollowedBy) {
        this.authorsFollowedBy = authorsFollowedBy;
    }

    public List<String> getAuthorFavorite() {
        return authorFavorite;
    }

    public void setAuthorFavorite(List<String> authorFavorite) {
        this.authorFavorite = authorFavorite;
    }

    public List<String> getAuthorShared() {
        return authorShared;
    }

    public void setAuthorShared(List<String> authorShared) {
        this.authorShared = authorShared;
    }

    public List<String> getAuthorLiked() {
        return authorLiked;
    }

    public void setAuthorLiked(List<String> authorLiked) {
        this.authorLiked = authorLiked;
    }
}
