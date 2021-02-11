package edu.northsouth.plasmabank.models;

public class Post {
    private String id;
    private String author;
    private String location;
    private String postDetails;
    private String contact;
    private String date;
    private String blood;

    private boolean isManaged;

    private int avatar;

    public Post() {
        //  empty constructor is required for firebase
    }

    public Post(int avatar, String author, String location, String postDetails, String contact, String date, String blood) {
        this.author = author;
        this.location = location;
        this.postDetails = postDetails;
        this.contact = contact;
        this.date = date;
        this.blood = blood;
        this.avatar = avatar;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getAvatar() {
        return avatar;
    }

    public void setAvatar(int avatar) {
        this.avatar = avatar;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBlood() {
        return blood;
    }

    public void setBlood(String blood) {
        this.blood = blood;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPostDetails() {
        return postDetails;
    }

    public void setPostDetails(String postDetails) {
        this.postDetails = postDetails;
    }

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = contact;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isManaged() {
        return isManaged;
    }

    public void setManaged(boolean managed) {
        isManaged = managed;
    }
}
